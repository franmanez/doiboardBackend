package edu.upc.doiboard.doiboardbackend.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "daily_samples")
class DailySample(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        val sampleDate: LocalDate,
        @OneToMany(mappedBy = "dailySample", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
        var works: List<CrossRefWork> = mutableListOf()
)

@Entity
@Table(name = "crossref_works")
class CrossRefWork(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        val doi: String,
        @Column(columnDefinition = "TEXT") val title: String?,
        @Column(columnDefinition = "TEXT") val containerTitle: String?,
        val type: String?,
        val isReferencedByCount: Int?,
        val issuedDate: String?,
        @Column(columnDefinition = "TEXT") val publisher: String?,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "daily_sample_id")
        var dailySample: DailySample? = null
)

@Entity
@Table(name = "ai_analyses")
class AIAnalysis(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        val analysisDate: LocalDate,
        val analysisType: String, // "CONNECTION", "FRONTIER", "GAP"
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
