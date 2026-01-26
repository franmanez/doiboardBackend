# Funcionalidad IA: The Knowledge Gap of the Day

Esta herramienta implementa un **Análisis de Silencios Semánticos**, una técnica de pensamiento crítico asistida por IA que identifica áreas de investigación infra-representadas o puntos ciegos en la producción científica diaria.

## 1. Muestreo Transversal Aleatorio
Al igual que las otras métricas diarias, se apoya en la estrategia de muestreo aleatorio (`sample=100`) de CrossRef:
- **Trazabilidad de la Ausencia**: Al persistir la muestra en la base de datos local, podemos demostrar estadísticamente qué temas estaban presentes en el discurso científico de un día concreto frente a los hallazgos de omisión de la IA.
- **Optimización de Procesamiento**: El análisis se ejecuta sobre el mismo set de datos de la muestra diaria, eliminando la redundancia en el transporte de datos y maximizando la eficiencia de la infraestructura.

## 2. Inferencia de Gaps de Conocimiento
El motor de IA (Gemini) evalúa la muestra no por lo que contiene, sino aplicando una rejilla de **conocimiento experto estructural**.

### Categorías de Análisis de Gaps:
1. **Silencios Éticos y Sociales**: Identificación de desarrollos puramente tecnológicos donde se omite el análisis de impacto social, privacidad o ética (campos cada vez más exigidos por agencias de financiación como Horizonte Europa).
2. **Invisibilidad Geográfica/Económica**: Detección de sesgos donde soluciones globales se aplican solo a contextos del norte global, ignorando variables de países en desarrollo.
3. **Desconexión con Macro-Tendencias**: Evaluación de si la ciencia del día está respondiendo a retos globales actuales (ej: crisis climática, pandemias, transiciones energéticas) o si existe una desconexión entre la investigación básica y las urgencias sociales.

## 3. Generación de Informes Críticos (Markdown)
Para una comunicación efectiva en entornos académicos y de toma de decisiones (Policy Making), los resultados se entregan en **Markdown profesional**:
- **Estructura Dialéctica**: Se presenta el "Gap" (vacío), la observación crítica y el campo donde su resolución generaría más impacto.
- **Formato Enriquecido**: Uso de **negritas**, listas y bloques de texto para resaltar la urgencia o relevancia del vacío descubierto.
- **Persistencia**: Al igual que el contenido, este análisis queda archivado en la tabla `ai_analyses`, permitiendo crear series históricas de "puntos ciegos" en la ciencia.

## 4. Relevancia para la Estrategia Científica
Este informe es una herramienta valiosa para directores de centros de investigación y agencias de financiación, ya que ayuda a identificar nichos de oportunidad y a reorientar esfuerzos hacia áreas desatendidas pero críticas para el progreso equilibrado de la ciencia.
