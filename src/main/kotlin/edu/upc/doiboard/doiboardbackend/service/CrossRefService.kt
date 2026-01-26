package edu.upc.doiboard.doiboardbackend.service

import edu.upc.doiboard.doiboardbackend.model.AIAnalysis
import edu.upc.doiboard.doiboardbackend.model.AIRadar
import edu.upc.doiboard.doiboardbackend.model.CrossRefWork
import edu.upc.doiboard.doiboardbackend.model.DailySample
import edu.upc.doiboard.doiboardbackend.repository.AIAnalysisRepository
import edu.upc.doiboard.doiboardbackend.repository.AIRadarRepository
import edu.upc.doiboard.doiboardbackend.repository.DailySampleRepository
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.ObjectMapper

@Service
class CrossRefService(
        private val dailySampleRepository: DailySampleRepository,
        private val aiAnalysisRepository: AIAnalysisRepository,
        private val aiRadarRepository: AIRadarRepository,
        @Value("\${gemini.api.key}") private val geminiApiKey: String,
        @Value("\${gemini.api.model}") private val geminiModel: String,
        @Value("\${gemini.api.version}") private val geminiApiVersion: String
) {
    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()
    private val logger = LoggerFactory.getLogger(CrossRefService::class.java)

    fun getInnovationRadar(): Any? {
        // Strategy: Get the last radar run. If it's older than 1 week, creating a new one could be
        // an option,
        // but for now, let's just check if ANY exists.
        val existing = aiRadarRepository.findTopByOrderByIdDesc()
        if (existing != null) {
            // Return content + metadata
            val node = objectMapper.readTree(existing.resultJson)
            val runDate = existing.runDate
            return mapOf(
                    "data" to node,
                    "meta" to
                            mapOf(
                                    "runDate" to runDate,
                                    "startDate" to runDate.minusMonths(6),
                                    "endDate" to runDate
                            )
            )
        }

        val endDate = LocalDate.now()
        val startDate = endDate.minusMonths(6)

        // Fetch data for the last 6 months
        val crossRefData = fetchTopWorks(startDate, endDate)
        val jsonString = objectMapper.writeValueAsString(crossRefData)

        val prompt = buildInnovationRadarPrompt(jsonString)
        val aiResponse = callGeminiApi(prompt)

        return try {
            // Robust cleaning for potential markdown blocks
            val cleanedJson =
                    aiResponse
                            .trim()
                            .replace(Regex("^```json\\s*", RegexOption.IGNORE_CASE), "")
                            .replace(Regex("^```\\s*"), "")
                            .replace(Regex("\\s*```$"), "")
                            .trim()
            val jsonNode = objectMapper.readTree(cleanedJson)

            // Expected JSON structure: { "es": [...], "en": [...], "ca": [...] }
            // But Radar Prompt was previously returning just [...]. We need to update the prompt
            // below too.
            // Assuming prompt update to return multilingual object:

            // Persist the WHOLE multilingual JSON object in resultJson
            // Persist the WHOLE multilingual JSON object in resultJson
            val radar = AIRadar(runDate = LocalDate.now(), resultJson = cleanedJson)
            aiRadarRepository.save(radar)

            // Return the full JSON object + meta
            mapOf(
                    "data" to jsonNode,
                    "meta" to
                            mapOf(
                                    "runDate" to radar.runDate,
                                    "startDate" to startDate,
                                    "endDate" to endDate
                            )
            )
        } catch (e: Exception) {
            logger.error("Error parsing Gemini response to JSON: ${e.message}")
            mapOf("error" to "Failed to parse AI response", "rawResponse" to aiResponse)
        }
    }

    private fun fetchTopWorks(start: LocalDate, end: LocalDate): Any? {
        val startStr = start.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endStr = end.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val url =
                "https://api.crossref.org/works?filter=from-pub-date:$startStr,until-pub-date:$endStr&sort=is-referenced-by-count&order=desc&rows=500&select=title,DOI,type,is-referenced-by-count,issued,container-title,publisher"
        return restTemplate.getForObject(url, Any::class.java)
    }

    private fun buildInnovationRadarPrompt(crossRefJson: String): String {
        return """
            You are an expert in scientific data mining and trend analysis.
            INPUT DATA: 500 most influential articles from the last 6 months.
            $crossRefJson

            YOUR OBJECTIVE: Generate data for an 'Innovation Radar' (Bubble Chart).
            
            INSTRUCTIONS:
            1. Clustering: Identify 20-30 macro-trends (tags) in English. NO MORE to ensure a complete JSON response.
            2. Count: How many articles from the list belong to each tag.
            3. Context: Use 'container-title' to infer the 'domain' (also in English).
            
            OUTPUT FORMAT (PURE JSON):
            Respond ONLY with a valid JSON array of objects. 
            - NO Markdown code blocks (```json ... ```).
            - NO introductions or explanations.
            - The result must start with `[` and end with `]`.
            
            Mandatory structure:
            [
              {"tag": "Trend Name", "count": 10, "trend": "rising", "domain": "Scientific Domain"}
            ]

            Trend types: 'new', 'rising', 'stable'.
        """.trimIndent()
    }

    fun getDailySamples(date: LocalDate): List<CrossRefWork> {
        val sample = dailySampleRepository.findTopBySampleDateOrderByIdDesc(date)
        return sample?.works ?: emptyList()
    }

    /** Paso 1: Recupera 100 items aleatorios de una fecha y los guarda en la base de datos. */
    fun fetchAndStoreDailySample(date: LocalDate): DailySample {
        val existing = dailySampleRepository.findTopBySampleDateOrderByIdDesc(date)
        // Check if existing sample is valid (has works)
        if (existing != null && existing.works.isNotEmpty()) return existing

        // Reuse existing sample shell if present (to update it), or create new
        val sampleTarget = existing ?: DailySample(sampleDate = date)

        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val url =
                "https://api.crossref.org/works?filter=from-pub-date:$dateStr,until-pub-date:$dateStr&sample=100&select=title,DOI,type,is-referenced-by-count,issued,container-title,publisher"

        logger.info("Fetching sample from CrossRef for date: $dateStr")
        val response =
                restTemplate.getForObject(url, String::class.java)
                        ?: throw RuntimeException("Empty response from CrossRef")
        val rootNode = objectMapper.readTree(response)
        val items = rootNode.path("message").path("items")

        if (items.isMissingNode || items.isEmpty) {
            throw RuntimeException(
                    "No works found in CrossRef for date $dateStr. The index might not be updated yet."
            )
        }

        val worksList = mutableListOf<CrossRefWork>()

        items.forEach { item ->
            val work =
                    CrossRefWork(
                            doi = item.path("DOI").asText(),
                            title = item.path("title").get(0)?.asText(),
                            containerTitle = item.path("container-title").get(0)?.asText(),
                            type = item.path("type").asText(),
                            isReferencedByCount = item.path("is-referenced-by-count").asInt(),
                            issuedDate =
                                    run {
                                        val dp = item.path("issued").path("date-parts").get(0)
                                        val y = dp?.get(0)?.asInt()
                                        val m = dp?.get(1)?.asInt()
                                        val d = dp?.get(2)?.asInt()
                                        if (y != null && m != null && d != null) {
                                            String.format("%02d/%02d/%04d", d, m, y)
                                        } else if (y != null && m != null) {
                                            String.format("%02d/%04d", m, y)
                                        } else {
                                            y?.toString() ?: ""
                                        }
                                    },
                            publisher = item.path("publisher").asText(),
                            dailySample = sampleTarget
                    )
            worksList.add(work)
        }

        sampleTarget.works = worksList
        return dailySampleRepository.save(sampleTarget)
    }

    fun getAnalysis(date: LocalDate, type: String, lang: String = "es"): Any? {
        val existing =
                aiAnalysisRepository.findTopByAnalysisDateAndAnalysisTypeOrderByIdDesc(date, type)
        if (existing != null) {
            return when (lang) {
                "en" -> mapOf("content" to existing.resultEn)
                "ca" -> mapOf("content" to existing.resultCa)
                else -> mapOf("content" to existing.resultEs)
            }
        }
        return null // Strictly read-only now
    }

    /**
     * Generates a new analysis for a specific date and type using LLM. This is intended to be
     * called by a separate script or administrative action.
     */
    fun computeAnalysis(date: LocalDate, type: String): AIAnalysis {
        var sample = dailySampleRepository.findTopBySampleDateOrderByIdDesc(date)
        if (sample == null || sample.works.isEmpty()) {
            sample = fetchAndStoreDailySample(date)
        }
        val worksList =
                sample.works.map { work ->
                    mapOf(
                            "title" to work.title,
                            "container-title" to work.containerTitle,
                            "DOI" to work.doi,
                            "type" to work.type
                    )
                }
        val worksJson = objectMapper.writeValueAsString(worksList)

        val prompt =
                when (type) {
                    "CONNECTION" -> buildConnectionPrompt(worksJson)
                    "FRONTIER" -> buildFrontierPrompt(worksJson)
                    "GAP" -> buildGapPrompt(worksJson)
                    else -> throw IllegalArgumentException("Unknown analysis type")
                }

        val aiRawResponse = callGeminiApi(prompt)

        var resultEs = ""
        var resultEn = ""
        var resultCa = ""

        try {
            val cleanedJson = aiRawResponse.trim().removeSurrounding("```json", "```").trim()
            val jsonNode = objectMapper.readTree(cleanedJson)
            resultEs = jsonNode.path("es").asText()
            resultEn = jsonNode.path("en").asText()
            resultCa = jsonNode.path("ca").asText()
        } catch (e: Exception) {
            logger.error("Error parsing multilingual AI response: ${e.message}")
            resultEs = aiRawResponse
            resultEn = "Error parsing translation"
            resultCa = "Error parsing translation"
        }

        val analysis =
                AIAnalysis(
                        analysisDate = date,
                        analysisType = type,
                        resultEs = resultEs,
                        resultEn = resultEn,
                        resultCa = resultCa
                )
        return aiAnalysisRepository.save(analysis)
    }

    private fun buildConnectionPrompt(json: String) =
            """
        Eres un experto en análisis interdisciplinar profesional. Analiza estos 100 artículos científicos recientes:
        $json
        
        Tu objetivo: encontrar 3 "Daily Connections" (Puentes Interdisciplinares).
        Busca artículos de dominios distintos que traten problemas similares o puedan beneficiarse mutuamente.
        
        **FORMATO DE SALIDA (JSON):**
        Debes responder ÚNICAMENTE con un JSON válido que contenga la explicación en 3 idiomas (Español 'es', Inglés 'en', Catalán 'ca').
        Dentro de cada idioma, el contenido debe ser HTML LIMPIO (sin markdown, sin divs, solo h3, p, ul, li).
        
        Estructura requerida:
        {
          "es": "<h3>Conexión 1...</h3>...",
          "en": "<h3>Connection 1...</h3>...",
          "ca": "<h3>Connexió 1...</h3>..."
        }
        
        **Detalle del contenido HTML para cada idioma:**
        
        <h3>[Conexión X: Título]</h3>
        <p>[Explicación clara de la conexión]</p>
        <p><strong>[Evidencias]:</strong></p>
        <ul>
           <li>[Título del Artículo] <a href="https://doi.org/[DOI]" target="_blank">[DOI]</a></li>
           <!-- Incluye TODOS los artículos relevantes (3-6 si es posible) -->
        </ul>
        <br>
        
        Asegúrate de traducir Títulos (del análisis, no del paper), Explicaciones y Etiquetas (como "Evidencias", "Connection", "Connexió") correctamente a cada idioma.
        Los Títulos de los artículos MANTENLOS en su idioma original.
    """.trimIndent()

    private fun buildFrontierPrompt(json: String) =
            """
        Eres un experto en vanguardia científica. Analiza estos 100 artículos:
        $json
        
        Identifica los 5 términos, conceptos o acrónimos más **raros, nuevos o disruptivos**.
        
        **FORMATO DE SALIDA (JSON):**
        Responde ÚNICAMENTE con un JSON válido con llaves 'es', 'en', 'ca'.
        El valor es HTML LIMPIO.
        
        Estructura:
        {
          "es": "<h3>Término 1...</h3>...",
          "en": "<h3>Term 1...</h3>...",
          "ca": "<h3>Terme 1...</h3>..."
        }
        
        **Detalle del contenido HTML:**
        
        <h3>[Término X: Nombre]</h3>
        <p>[Explicación de por qué es nuevo]</p>
        <p><strong>[Fuente]:</strong> [Título Artículo] <a href="https://doi.org/[DOI]" target="_blank">[Ver DOI]</a></p>
        <br>
        
        Traduce todo excepto el Término en sí (si es técnico) y el título del artículo.
    """.trimIndent()

    private fun buildGapPrompt(json: String) =
            """
        Eres un analista crítico de la ciencia. Analiza estos 100 artículos:
        $json
        
        Identifica 3 **Knowledge Gaps** (Vacíos de Conocimiento) importantes hoy.
        
        **FORMATO DE SALIDA (JSON):**
        Responde ÚNICAMENTE con un JSON válido con llaves 'es', 'en', 'ca'.
        El valor es HTML LIMPIO.
        
        Estructura:
        {
          "es": "<h3>Vacío 1...</h3>...",
          "en": "<h3>Gap 1...</h3>...",
          "ca": "<h3>Buit 1...</h3>..."
        }
        
        **Detalle del contenido HTML:**
        
        <h3>[Vacío X: Tema]</h3>
        <p>[Explicación de la ausencia]</p>
        <p><strong>[Oportunidad]:</strong> [Sugerencia]</p>
        <br>
        
        Traduce explicaciones y etiquetas a cada idioma.
    """.trimIndent()

    private fun callGeminiApi(prompt: String): String {
        val url =
                "https://generativelanguage.googleapis.com/$geminiApiVersion/models/$geminiModel:generateContent?key=$geminiApiKey"
        val requestBody =
                mapOf("contents" to listOf(mapOf("parts" to listOf(mapOf("text" to prompt)))))
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = RequestEntity(requestBody, headers, HttpMethod.POST, URI(url))
        val response = restTemplate.exchange(request, String::class.java)
        val responseJson = objectMapper.readTree(response.body)
        return responseJson
                .path("candidates")
                .get(0)
                ?.path("content")
                ?.path("parts")
                ?.get(0)
                ?.path("text")
                ?.asText()
                ?: ""
    }
}
