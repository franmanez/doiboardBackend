package edu.upc.doiboard.doiboardbackend.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "ai_analyses")
class AIAnalysis(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        val analysisDate: LocalDate,
        val analysisType: String, // "MONTHLY_SYNTHESIS", "QUARTERLY_HORIZON"
        val period: String, // "MONTHLY", "QUARTERLY"
        val year: Int,
        val month: Int? = null,
        val quarter: Int? = null,
        @Column(columnDefinition = "TEXT") val resultEs: String,
        @Column(columnDefinition = "TEXT") val resultEn: String,
        @Column(columnDefinition = "TEXT") val resultCa: String
)

@Entity
@Table(name = "ai_radar")
class AIRadar(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        val runDate: LocalDate,
        @Column(columnDefinition = "TEXT") val resultJson: String
)
