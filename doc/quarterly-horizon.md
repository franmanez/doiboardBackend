# Funcionalidad IA: Prospectiva Trimestral (Quarterly Horizon Scanning)

## Marco Teórico
El **Horizon Scanning** es una metodología sistémica de examen de señales de cambio para detectar el nacimiento de tendencias antes de que sean visibles mediante métricas tradicionales. Esta funcionalidad aplica técnicas de **Prospectiva Basada en Evidencia** (*Evidence-based Foresight*) para cartografiar la evolución del interés científico global.

## 1. Metodología de Análisis Comparativo Masivo
La infraestructura técnica permite la comparación de flujos de datos longitudinales:
- **Dataset Dual de Alto Impacto**: El sistema captura un total de **500 DOIs únicos** por ejecución, segmentados en 250 ítems del trimestre objetivo ($Q_{n}$) y 250 del periodo de control previo ($Q_{n-1}$).
- **Estratificación del Muestreo**: Los datos se recuperan bajo el criterio de impacto acumulado (citas), lo que permite observar qué áreas de conocimiento están consolidando su hegemonía y cuáles están sufriendo una desaceleración en el interés de la comunidad investigadora.

## 2. Inferencia de Vanguardia y Detección de "Frontier Shift"
El procesamiento mediante LLM se enfoca en tres ejes analíticos críticos:
1.  **Indicadores de Consolidación**: Identificación de temas trans-periodo que han alcanzado una masa crítica de citación.
2.  **Mapeo de la Frontera**: Detección de desplazamientos cualitativos en la agenda científica (ej. el paso de la experimentación básica a la implementación sistémica o autónoma).
3.  **Análisis de Ruptura**: Identificación de hasta 3 cambios significativos en el foco de interés que denotan un cambio de paradigma o la aparición de tecnologías disruptivas.

## 3. Implementación y Estándares de Robustez
- **Motores de Limpieza Predictiva**: Dado que el volumen de datos (1000 registros enriquecidos) puede generar respuestas ruidosas en el modelo, se ha implementado un motor de extracción basado en expresiones regulares (Regex) que garantiza la integridad estructural del JSON multilingüe.
- **Tratamiento de Metadatos Críticos**: Se procesan recursivamente los campos `title`, `citations`, `DOI` y `container-title` para alimentar la inferencia, asegurando que el análisis no se base solo en palabras clave, sino en el contexto de publicación (prestigio de revista y volumen de citas).

## 4. Relevancia Estratégica Institucional
Esta herramienta trasciende el simple resumen y se convierte en un activo de **Planificación Estratégica**. Universidades e Institutos de Investigación pueden utilizar estos informes trimestrales para:
- Anticipar áreas de oportunidad para la obtención de fondos de investigación.
- Identificar sub-disciplinas saturadas vs. emergentes.
- Alinear la producción científica de la institución con las fronteras del conocimiento mundial.
