package edu.upc.doiboard.doiboardbackend.service

import edu.upc.doiboard.doiboardbackend.model.AIAnalysis
import edu.upc.doiboard.doiboardbackend.model.AIRadar
import edu.upc.doiboard.doiboardbackend.repository.AIAnalysisRepository
import edu.upc.doiboard.doiboardbackend.repository.AIRadarRepository
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
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

@Service
class CrossRefService(
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
        val existing = aiRadarRepository.findTopByOrderByIdDesc()
        if (existing != null) {
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
        val crossRefData = fetchTopWorks(startDate, endDate)
        val jsonString = objectMapper.writeValueAsString(crossRefData)

        val prompt = buildInnovationRadarPrompt(jsonString)
        val aiResponse = callGeminiApi(prompt)

        return try {
            val firstBrace = aiResponse.indexOf('[')
            val lastBrace = aiResponse.lastIndexOf(']')
            if (firstBrace == -1 || lastBrace == -1) throw RuntimeException("Invalid radar JSON")

            val cleanedJson = aiResponse.substring(firstBrace, lastBrace + 1)
            val jsonNode = objectMapper.readTree(cleanedJson)

            val radar = AIRadar(runDate = LocalDate.now(), resultJson = cleanedJson)
            aiRadarRepository.save(radar)

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
            logger.error("Error parsing radar response: ${e.message}")
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
            INPUT DATA (t=title, j=journal, d=DOI, c=citations): $crossRefJson

            YOUR OBJECTIVE: Generate data for an 'Innovation Radar' (Bubble Chart).
            
            STEP-BY-STEP PROCESS:
            1. Analyze the 500 scientific papers provided.
            2. Identify exactly 5 or 6 BROAD MACRO-DOMAINS that encompass all the research found (e.g., 'Biomedicine & Health', 'AI & Digital Transformation', 'Energy & Sustainability', etc.).
            3. Group all specific trends into these 5-6 broad domains. Avoid creating niche or very specific domains.
            4. For each trend, provide a descriptive name (tag), the number of papers associated with it within the sample (count), its lifecycle stage (rising, new, stable), and the broad domain it belongs to.

            OUTPUT FORMAT (PURE JSON ARRAY):
            [{"tag": "Specific Trend Name", "count": 10, "trend": "rising", "domain": "Broad Macro-Domain Name"}]
            
            CRITICAL: 
            - Use ONLY the 5-6 broad domains identified in step 2 for the "domain" field.
            - Do not include any text other than the JSON array.
        """.trimIndent()
    }

    fun getMonthlyAnalysis(year: Int, month: Int, lang: String = "es"): Any? {
        val existing =
                aiAnalysisRepository.findTopByPeriodAndYearAndMonthAndAnalysisTypeOrderByIdDesc(
                        "MONTHLY",
                        year,
                        month,
                        "MONTHLY_SYNTHESIS"
                )
        if (existing != null) {
            return when (lang) {
                "en" -> mapOf("content" to existing.resultEn)
                "ca" -> mapOf("content" to existing.resultCa)
                else -> mapOf("content" to existing.resultEs)
            }
        }
        return null
    }

    fun getQuarterlyAnalysis(year: Int, quarter: Int, lang: String = "es"): Any? {
        val existing =
                aiAnalysisRepository.findTopByPeriodAndYearAndQuarterAndAnalysisTypeOrderByIdDesc(
                        "QUARTERLY",
                        year,
                        quarter,
                        "QUARTERLY_HORIZON"
                )
        if (existing != null) {
            return when (lang) {
                "en" -> mapOf("content" to existing.resultEn)
                "ca" -> mapOf("content" to existing.resultCa)
                else -> mapOf("content" to existing.resultEs)
            }
        }
        return null
    }

    fun computeMonthlySynthesis(year: Int, month: Int): AIAnalysis {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        val crossRefData = fetchTopWorksForPeriod(startDate, endDate, 300)
        val jsonString = objectMapper.writeValueAsString(crossRefData)

        val prompt = buildMonthlySynthesisPrompt(jsonString, month, year)
        val aiRawResponse = callGeminiApi(prompt)
        val results = parseTaggedResponse(aiRawResponse)

        return aiAnalysisRepository.save(
                AIAnalysis(
                        analysisDate = startDate,
                        analysisType = "MONTHLY_SYNTHESIS",
                        period = "MONTHLY",
                        year = year,
                        month = month,
                        resultEs = results["es"] ?: "",
                        resultEn = results["en"] ?: "",
                        resultCa = results["ca"] ?: ""
                )
        )
    }

    fun computeQuarterlyHorizon(year: Int, quarter: Int): AIAnalysis {
        val startMonth = (quarter - 1) * 3 + 1
        val startDate = LocalDate.of(year, startMonth, 1)
        val endDate = startDate.plusMonths(3).minusDays(1)
        val prevStartDate = startDate.minusMonths(3)
        val prevEndDate = startDate.minusDays(1)

        val currentData = fetchTopWorksForPeriod(startDate, endDate, 250)
        val previousData = fetchTopWorksForPeriod(prevStartDate, prevEndDate, 250)

        val prompt =
                buildQuarterlyHorizonPrompt(
                        objectMapper.writeValueAsString(currentData),
                        objectMapper.writeValueAsString(previousData),
                        quarter,
                        year
                )

        val aiRawResponse = callGeminiApi(prompt)
        val results = parseTaggedResponse(aiRawResponse)

        return aiAnalysisRepository.save(
                AIAnalysis(
                        analysisDate = startDate,
                        analysisType = "QUARTERLY_HORIZON",
                        period = "QUARTERLY",
                        year = year,
                        quarter = quarter,
                        resultEs = results["es"] ?: "",
                        resultEn = results["en"] ?: "",
                        resultCa = results["ca"] ?: ""
                )
        )
    }

    private fun fetchTopWorksForPeriod(start: LocalDate, end: LocalDate, rows: Int): List<Any> {
        val startStr = start.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endStr = end.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val url =
                "https://api.crossref.org/works?filter=from-pub-date:$startStr,until-pub-date:$endStr&sort=is-referenced-by-count&order=desc&rows=$rows&select=title,DOI,type,is-referenced-by-count,issued,container-title,publisher"

        val response = restTemplate.getForObject(url, String::class.java) ?: return emptyList()
        val rootNode: JsonNode = objectMapper.readTree(response)
        val items: JsonNode = rootNode.path("message").path("items")

        val resultList = mutableListOf<Any>()
        if (items.isArray) {
            items.forEach { item ->
                resultList.add(
                        mapOf(
                                "t" to item.path("title").get(0)?.asText(),
                                "j" to item.path("container-title").get(0)?.asText(),
                                "d" to item.path("DOI").asText(),
                                "c" to item.path("is-referenced-by-count").asInt()
                        )
                )
            }
        }
        return resultList
    }

    private fun parseTaggedResponse(raw: String): Map<String, String> {
        fun extract(tag: String): String {
            val marker = "===$tag==="
            val startIndex = raw.indexOf(marker)
            if (startIndex == -1) return ""

            val contentStart = startIndex + marker.length
            // Buscamos el inicio del siguiente tag o el final de la cadena
            val nextMarkerIndex = raw.indexOf("===", contentStart)

            return if (nextMarkerIndex == -1) {
                raw.substring(contentStart).trim()
            } else {
                raw.substring(contentStart, nextMarkerIndex).trim()
            }
        }

        val es = extract("ES")
        val en = extract("EN")
        val ca = extract("CA")

        return if (es.isBlank() && en.isBlank() && ca.isBlank()) {
            logger.warn("No tags found in AI response. Falling back to raw response in ES.")
            mapOf("es" to raw, "en" to "Analysis not available", "ca" to "Anàlisi no disponible")
        } else {
            mapOf("es" to es, "en" to en, "ca" to ca)
        }
    }

    private fun buildMonthlySynthesisPrompt(json: String, month: Int, year: Int) =
            """
        Eres un experto en bibliometría y analítica de tendencias científicas.
        Analiza esta producción científica (t=título, j=revista, d=DOI, c=citas): $json
        
        OBJETIVO: Generar el informe "Núcleo Temático Mensual" para el Mes $month del Año $year.
        
        INSTRUCCIONES DE ESTRUCTURA (ESTRICTO):
        1. TÍTULO: "Núcleo Temático Mensual: [Mes] de [Año]" (en el idioma correspondiente).
        2. INTRODUCCIÓN: Analiza el panorama científico general del mes en 1-2 párrafos.
        3. IDENTIFICACIÓN DE NÚCLEOS: Una frase de transición mencionando que se han identificado 4 núcleos principales.
        4. DESARROLLO DE NÚCLEOS (EXACTAMENTE 4): Para cada núcleo usa este esquema:
           - Título numerado (Ej: "1. Nombre del Tema: Subtítulo descriptivo") usando <h3>.
           - Un párrafo narrativo profundo explicando el núcleo, su impacto y tendencias internas.
           - Sección "Ejemplos destacados:" con una lista (<ul>) de los artículos más relevantes indicando título y número de citas.
        5. RESUMEN FINAL: Un párrafo de conclusión que sintetice la convergencia de temas y el impacto global observado.

        FORMATO TÉCNICO:
        - Usa HTML limpio (<h3>, <p>, <strong>, <ul>, <li>).
        - NO USES JSON. 
        - Escribe el contenido directamente usando estos marcadores de idioma:
        
        ===ES===
        [Contenido en Español siguiendo la estructura]
        ===EN===
        [Contenido en Inglés siguiendo la estructura]
        ===CA===
        [Contenido en Catalán siguiendo la estructura]
    """.trimIndent()

    private fun buildQuarterlyHorizonPrompt(
            currentJson: String,
            prevJson: String,
            quarter: Int,
            year: Int
    ) =
            """
        Eres un experto en Horizon Scanning. Compara estos periodos (t=título, j=revista, d=DOI, c=citas):
        ACTUAL: $currentJson
        PREVIO: $prevJson
        
        OBJETIVO: Detectar desplazamientos de frontera científica entre el trimestre previo y el actual (T$quarter, Año $year).
        
        INSTRUCCIONES DE ESTRUCTURA (ESTRICTO):
        1. TÍTULO: "Detección de Desplazamientos de Frontera (T$quarter, Año $year)" (o traducción equivalente).
        2. INTRODUCCIÓN: Un párrafo breve analizando la madurez general de los temas observados.
        3. TRES CAMBIOS SIGNIFICATIVOS: Para cada uno de los 3 cambios identificados, usa exactamente este esquema:
           - Título del cambio (lo más descriptivo posible, use <h3>).
           - "Desplazamiento:": Explicación cualitativa de cómo ha evolucionado el foco.
           - "Evidencia en PREVIO:": Cita ejemplos o temas detectados anteriormente.
           - "Evidencia en ACTUAL:": Cita los nuevos DOIs o tendencias consolidadas ahora.
        4. RESUMEN FINAL: Un breve párrafo de conclusión.

        FORMATO TÉCNICO:
        - Usa HTML limpio (<h3>, <p>, <strong>, <ul>, <li>).
        - NO USES JSON. 
        - Escribe el contenido directamente usando estos marcadores de idioma:
        
        ===ES===
        [Contenido en Español siguiendo la estructura]
        ===EN===
        [Contenido en Inglés siguiendo la estructura]
        ===CA===
        [Contenido en Catalán siguiendo la estructura]
    """.trimIndent()

    private fun callGeminiApi(prompt: String): String {
        val url =
                "https://generativelanguage.googleapis.com/$geminiApiVersion/models/$geminiModel:generateContent?key=$geminiApiKey"
        val requestBody =
                mapOf("contents" to listOf(mapOf("parts" to listOf(mapOf("text" to prompt)))))
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = RequestEntity(requestBody, headers, HttpMethod.POST, URI(url))
        val response = restTemplate.exchange(request, String::class.java)
        return objectMapper
                .readTree(response.body ?: "")
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText()
                ?: ""
    }
}
