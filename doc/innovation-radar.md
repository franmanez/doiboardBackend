# Funcionalidad IA: Radar de Innovación

El **Radar de Innovación** es un motor de análisis bibliométrico avanzado que utiliza Modelos de Lenguaje de Gran Escala (LLM) para mapear el panorama tecnológico y científico global. Su objetivo es transformar metadatos crudos de publicaciones en una visualización de áreas de concentración de conocimiento.

## 1. Arquitectura de Muestreo por Impacto
A diferencia del análisis de flujo diario, el Radar se basa en una ventana de observación semestral para garantizar la madurez de las métricas de citación.

- **Ventana Temporal**: Análisis de periodos de 6 meses (ej. Julio-Diciembre). Este intervalo permite que el factor de transferencia de conocimiento y las citas acumuladas sean estadísticamente significativos.
- **Selección de Datos**: Recuperación de los **500 trabajos más citados** (`is-referenced-by-count` descendente) a través de la API de CrossRef.
- **Densidad de Información**: El aumento del muestreo a 500 registros busca reducir la dispersión de datos y aumentar la redundancia semántica, permitiendo a la IA detectar patrones de repetición que definen una tendencia.

## 2. Motor de Inferencia y Clustering Agresivo
El procesamiento se realiza mediante el modelo Google Gemini, configurado para realizar un **Clustering Semántico Agresivo**.

### Metodología de Procesamiento:
1. **Detección de Macrotendencias**: El sistema no se limita a extraer palabras clave. Realiza una síntesis conceptual para agrupar términos específicos (ej: "Perovskite Solar Cells", "Tandem Photovoltaics" y "Bifacial Panels") bajo un clúster unificado de mayor peso como **"Next-Gen Photovoltaic Technology"**.
2. **Contextualización Institucional y Editorial**: Utiliza el `publisher` y el `container-title` para ponderar la relevancia del término en función del prestigio del canal de comunicación científica.
3. **Métrica de Intensidad (`count`)**: Representa la frecuencia absoluta de artículos que se encuadran dentro de una macrotendencia dentro de la muestra de 500, proporcionando una medida de la "masa crítica" del área.

## 3. Taxonomía de Tendencias
Cada macrotendencia se clasifica en una de tres categorías de ciclo de vida:
- **`new` (Disruptivo)**: Tecnologías emergentes con baja frecuencia histórica pero aparición súbita en revistas de vanguardia.
- **`rising` (En Crecimiento)**: Áreas con alta aceleración de citas y proliferación de publicaciones relacionadas.
- **`stable` (Consolidado)**: Pilares fundamentales de la ciencia contemporánea con volumen de citas alto y constante.

## 4. Persistencia y Optimización de Capa de Datos
El sistema implementa una **Caché de Inteligencia** en la base de datos relacional:
- **Validación de Caché**: Antes de invocar al LLM, el sistema verifica en la tabla `ai_analyses` si ya existe un análisis procesado para la fecha de referencia y el tipo `RADAR`.
- **Formato de Persistencia**: Los resultados se almacenan en formato JSON dentro de una columna de tipo `TEXT` (o `JSON` nativo), optimizando el tiempo de respuesta de ~15 segundos (latencia de IA) a <10ms (consulta a BD).
- **Consumo**: El endpoint expone un JSON estructurado listo para ser inyectado en componentes de visualización dinámica como Gráficos de Burbujas o Mapas de Calor en el frontend (Vue.js).

```json
[
  {
    "tag": "Generative AI & LLMs",
    "count": 82,
    "trend": "rising",
    "domain": "Computer Science / AI"
  }
]
```
