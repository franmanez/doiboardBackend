# Funcionalidad IA: Síntesis Temática Mensual (Monthly Synthesis)

## Resumen Ejecutivo
Esta funcionalidad implementa un sistema de **Curaduría Inteligente de Contenidos Históricos** (SICH), transformando el flujo masivo de metadatos de Crossref en conocimiento estructurado. El objetivo es mitigar la saturación de información (*information overload*) que enfrentan los investigadores contemporáneos mediante una síntesis mensual de los núcleos de investigación más influyentes.

## 1. Metodología de Muestreo Basada en Impacto
A diferencia de los enfoques cronológicos tradicionales, nuestro sistema utiliza un **Filtrado por Relevancia Bibliométrica**:
- **Dataset de Impacto**: Se extraen los **300 manuscritos con mayor índice de citación** (`is-referenced-by-count`) reportados en los últimos 30 días. Este umbral garantiza una muestra estadísticamente representativa y una alta estabilidad en la generación del análisis.
- **Normalización Temporal**: El algoritmo delimita con precisión el intervalo $[T_{start}, T_{end}]$ correspondiente al mes natural anterior, asegurando que los datos analizados sean definitivos y no preliminares.

## 2. Inferencia Mediante Aprendizaje Profundo (LLM)
El motor de análisis emplea un modelo de lenguaje de última generación (Gemini) configurado con un rol de **Analista Bibliométrico Experto**. El proceso de inferencia sigue tres etapas:
1.  **Clustering Semántico**: Identificación de patrones comunes en títulos y resúmenes de revistas.
2.  **Destilación de Núcleos**: Reducción de 500 ítems en 4 núcleos temáticos de alto impacto.
3.  **Generación de Narrativa Multilingüe**: Construcción de un informe estructurado en HTML que preserva la terminología técnica original de los artículos para evitar la pérdida de precisión semántica en la traducción.

## 3. Arquitectura del Sistema
- **Backend (Kotlin/Spring Boot)**: Gestiona la orquestación de llamadas asíncronas a la API REST de Crossref y la ingeniería de *prompts* masivos.
- **Persistencia Robusta**: Utiliza una base de datos regional que indexa los análisis por periodo temporal, permitiendo un acceso de latencia cero ($O(1)$) para los usuarios finales.
- **Interfaz de Usuario (Vue 3)**: Presenta los resultados mediante componentes reactivos, ofreciendo una navegación intuitiva por el histórico de síntesis.

## 4. Contribución a la Comunidad Científica
Esta herramienta proporciona un servicio de **Alerta Temática Consolidada**, permitiendo a las bibliotecas universitarias ofrecer informes de tendencias automáticos y a los investigadores identificar de un vistazo hacia donde se dirige la tracción intelectual de su área.
