package edu.upc.doiboard.doiboardbackend.repository

import edu.upc.doiboard.doiboardbackend.model.AIAnalysis
import edu.upc.doiboard.doiboardbackend.model.AIRadar
import edu.upc.doiboard.doiboardbackend.model.DailySample
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DailySampleRepository : JpaRepository<DailySample, Long> {
    fun findTopBySampleDateOrderByIdDesc(sampleDate: LocalDate): DailySample?
}

@Repository
interface AIAnalysisRepository : JpaRepository<AIAnalysis, Long> {
    fun findTopByAnalysisDateAndAnalysisTypeOrderByIdDesc(
            analysisDate: LocalDate,
            analysisType: String
    ): AIAnalysis?
}

@Repository
interface AIRadarRepository : JpaRepository<AIRadar, Long> {
    fun findTopByOrderByIdDesc(): AIRadar?
}
