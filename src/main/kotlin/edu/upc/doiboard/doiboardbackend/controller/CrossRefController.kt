package edu.upc.doiboard.doiboardbackend.controller

import edu.upc.doiboard.doiboardbackend.service.CrossRefService
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping("/api/crossref")
class CrossRefController(private val crossRefService: CrossRefService) {

    /** Radar de Innovación: Análisis de los trabajos más citados en un periodo. */
    @GetMapping("/radar")
    fun getRadar(): Any? {
        return crossRefService.getInnovationRadar()
    }

    /**
     * Endpoint 1: Recupera y almacena el sample diario (Estrategia B: 200 random) Si no se pasa
     * fecha, usa "ayer".
     */
    @GetMapping("/sample")
    fun storeSample(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            date: LocalDate?
    ): Any {
        val targetDate = date ?: LocalDate.now().minusDays(1)
        return crossRefService.fetchAndStoreDailySample(targetDate)
    }

    /** Endpoint 1b: Recupera el listado de trabajos del sample (sin disparar fetching externo). */
    @GetMapping("/daily-samples")
    fun getDailySamples(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): List<Any> {
        return crossRefService.getDailySamples(date).map { work ->
            mapOf(
                    "doi" to work.doi,
                    "title" to work.title,
                    "containerTitle" to work.containerTitle,
                    "type" to work.type,
                    "isReferencedByCount" to work.isReferencedByCount,
                    "issuedDate" to work.issuedDate,
                    "publisher" to work.publisher
            )
        }
    }

    /** Endpoint 2: The Daily Connection (Puentes Interdisciplinares) */
    @GetMapping("/analysis/connection")
    fun getConnection(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            date: LocalDate?,
            @RequestParam(required = false, defaultValue = "es") lang: String
    ): Any? {
        val targetDate = date ?: LocalDate.now().minusDays(1)
        return crossRefService.getAnalysis(targetDate, "CONNECTION", lang)
    }

    /** Endpoint 3: Frontiers of the Day (Términos raros/nuevos) */
    @GetMapping("/analysis/frontier")
    fun getFrontier(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            date: LocalDate?,
            @RequestParam(required = false, defaultValue = "es") lang: String
    ): Any? {
        val targetDate = date ?: LocalDate.now().minusDays(1)
        return crossRefService.getAnalysis(targetDate, "FRONTIER", lang)
    }

    @GetMapping("/analysis/gap")
    fun getGap(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            date: LocalDate?,
            @RequestParam(required = false, defaultValue = "es") lang: String
    ): Any? {
        val targetDate = date ?: LocalDate.now().minusDays(1)
        return crossRefService.getAnalysis(targetDate, "GAP", lang)
    }

    /**
     * Endpoint 5: PROCESAMIENTO EXPLÍCITO. Genera el análisis del día llamando al LLM. Pensado para
     * ser llamado por scripts.
     */
    @GetMapping("/analysis/compute")
    fun computeAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
            @RequestParam type: String // CONNECTION, FRONTIER, GAP
    ): Any {
        return crossRefService.computeAnalysis(date, type)
    }
}
