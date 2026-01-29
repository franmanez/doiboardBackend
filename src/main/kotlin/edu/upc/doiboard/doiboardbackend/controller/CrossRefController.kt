package edu.upc.doiboard.doiboardbackend.controller

import edu.upc.doiboard.doiboardbackend.service.CrossRefService
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping("/")
class CrossRefController(private val crossRefService: CrossRefService) {

    @GetMapping("/META/alive")
    fun alive(): String {
        return "alive"
    }

    /** Radar de Innovación: Análisis de los trabajos más citados en un periodo. */
    @GetMapping("/radar")
    fun getRadar(): Any? {
        return crossRefService.getInnovationRadar()
    }

    /** Endpoint 2: Monthly Thematic Synthesis */
    @GetMapping("/analysis/monthly")
    fun getMonthlyAnalysis(
            @RequestParam year: Int,
            @RequestParam month: Int,
            @RequestParam(required = false, defaultValue = "es") lang: String
    ): Any? {
        return crossRefService.getMonthlyAnalysis(year, month, lang)
    }

    /** Endpoint 3: Quarterly Horizon Scanning */
    @GetMapping("/analysis/quarterly")
    fun getQuarterlyAnalysis(
            @RequestParam year: Int,
            @RequestParam quarter: Int,
            @RequestParam(required = false, defaultValue = "es") lang: String
    ): Any? {
        return crossRefService.getQuarterlyAnalysis(year, quarter, lang)
    }

    /** Compute monthly analysis */
    @GetMapping("/analysis/compute/monthly")
    fun computeMonthly(@RequestParam year: Int, @RequestParam month: Int): Any {
        return crossRefService.computeMonthlySynthesis(year, month)
    }

    /** Compute quarterly analysis */
    @GetMapping("/analysis/compute/quarterly")
    fun computeQuarterly(@RequestParam year: Int, @RequestParam quarter: Int): Any {
        return crossRefService.computeQuarterlyHorizon(year, quarter)
    }
}
