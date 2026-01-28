package edu.upc.doiboard.doiboardbackend.repository

import edu.upc.doiboard.doiboardbackend.model.AIAnalysis
import edu.upc.doiboard.doiboardbackend.model.AIRadar
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AIAnalysisRepository : JpaRepository<AIAnalysis, Long> {
    fun findTopByAnalysisDateAndAnalysisTypeOrderByIdDesc(
            analysisDate: LocalDate,
            analysisType: String
    ): AIAnalysis?

    fun findTopByPeriodAndYearAndMonthAndAnalysisTypeOrderByIdDesc(
            period: String,
            year: Int,
            month: Int,
            analysisType: String
    ): AIAnalysis?

    fun findTopByPeriodAndYearAndQuarterAndAnalysisTypeOrderByIdDesc(
            period: String,
            year: Int,
            quarter: Int,
            analysisType: String
    ): AIAnalysis?
}

@Repository
interface AIRadarRepository : JpaRepository<AIRadar, Long> {
    fun findTopByOrderByIdDesc(): AIRadar?
}
