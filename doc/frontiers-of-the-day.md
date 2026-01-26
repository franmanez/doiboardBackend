# Funcionalidad IA: Frontiers of the Day

**Frontiers of the Day** es un sistema de detección temprana de neologismos y conceptos disruptivos. Utiliza técnicas de Procesamiento de Lenguaje Natural (NLP) a través de LLMs para identificar términos que están expandiendo los límites del conocimiento científico convencional.

## 1. Detección de Señales Débiles (Weak Signals)
A través de una muestra aleatoria diaria (`sample=100`) persistida en la base de datos, el sistema busca anomalías en la nomenclatura científica:
- **Metodología**: El modelo compara la terminología detectada en los títulos con su base de conocimiento pre-entrenada para identificar términos con baja frecuencia histórica pero alta relevancia semántica actual.
- **Tipología de Hallazgos**:
    1. **Neologismos Técnicos**: Términos que proponen nuevas sub-disciplinas o híbridos conceptuales.
    2. **Acrónimos Emergentes**: Siglas que representan nuevos protocolos, materiales o algoritmos.
    3. **Conceptos de Alta Vanguardia**: Términos que aparecen exclusivamente en revistas de "Frontiers" u otras de alto impacto reciente.

## 2. Inferencia Contextualizada
Para evitar falsos positivos (términos ya conocidos pero poco frecuentes en el lenguaje común), el motor de IA realiza una **triangulación de metadatos**:
- Analiza el `container-title` (revista) para entender si un término es nuevo en ese campo específico o es una transferencia de otro dominio.
- Utiliza la información del `publisher` para ponderar la "seriedad" o validación del nuevo término.

## 3. Arquitectura de Presentación (Text-based Analysis)
Para maximizar la legibilidad y utilidad del hallazgo, el sistema genera la información en formato **Markdown estructurado**:
- **Títulos Atractivos (###)**: Cada término detectado se presenta como una entrada de boletín científico.
- **Contexto Expandido**: A diferencia de un JSON simple, el formato de texto permite a la IA explicar por qué el término es relevante y cuál es su potencial impacto.
- **Trazabilidad**: Incluye el DOI asociado como enlace de verificación inmediata.

## 4. Aplicaciones en Minería de Datos
Esta técnica es vital para investigadores y analistas de tendencias, ya que permite detectar el "nacimiento" de tendencias meses antes de que se vuelvan visibles mediante análisis de citación tradicionales (que requieren años de maduración).
