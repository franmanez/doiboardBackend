# Funcionalidad IA: The Daily Connection (Puentes Interdisciplinares)

Esta funcionalidad explora el potencial de los Large Language Models (LLMs) para actuar como catalizadores de la **serendipia científica**, identificando puntos de convergencia entre artículos de disciplinas aparentemente desconectadas que han sido publicados en la misma ventana temporal.

## 1. Muestreo Aleatorio Representativo (Estrategia B)
Para evitar los sesgos propios de los sistemas de recomendación basados en popularidad o cronología lineal, el sistema utiliza una metodología de muestreo estadístico:
- **Técnica de Muestreo**: Uso del parámetro `sample=100` de la API de CrossRef. Esto garantiza que la muestra analizada sea una representación aleatoria y no sesgada de la producción científica global de una fecha determinada.
- **Persistencia de la Muestra**: A diferencia de otras herramientas, el sistema persiste la muestra completa en la tabla `crossref_works`. Esto permite:
    1. Auditar los datos que originaron el análisis de IA.
    2. Realizar múltiples tipos de análisis (Connections, Frontiers, Gaps) sobre el mismo set de datos sin incurrir en nuevas cuotas de API externa.
- **Normalización de Datos**: Durante el almacenamiento, se limpian y formatan los metadatos (especialmente fechas en formato `dd/MM/yyyy` y títulos extensos en formato `TEXT`) para un procesamiento óptimo.

## 2. Metodología de Análisis Interdisciplinar
El LLM procesa los metadatos de los 100 artículos buscando **isomorfismos estructurales**: problemas en la disciplina A que comparten la misma estructura lógica que soluciones ya probadas en la disciplina B.

### Proceso de Inferencia:
1. **Abstracción**: El modelo abstrae el problema central de cada título de la muestra.
2. **Matching Semántico**: Busca coincidencias en métodos, algoritmos o retos logísticos/biológicos/físicos.
3. **Generación de Hipótesis**: Propone una conexión inédita (ej: aplicación de algoritmos de enrutamiento de red para la modelización de la propagación de enfermedades en tejidos vegetales).

## 3. Formato de Salida y Visualización Web
Para facilitar la integración en interfaces de usuario modernas (como Vue.js), los resultados se generan directamente en **Markdown**:
- **Estructura**: Uso de encabezados de nivel 3 (###) para cada conexión identificada.
- **Resaltado Semántico**: Uso de **negritas** para enfatizar conceptos técnicos y DOIs.
- **Accesibilidad**: Los resultados se almacenan en la tabla `ai_analyses`, permitiendo una carga instantánea y visualización HTML enriquecida mediante renderizadores de Markdown en el frontend.

## 4. Valor Científico y Académico
Esta funcionalidad demuestra cómo la IA puede mitigar la **hiperespecialización**, permitiendo a investigadores de diversas áreas encontrar colaboradores potenciales o nuevas metodologías fuera de su zona de confort habitual.
