# Funcionalidad IA: Radar de Innovación

El **Radar de Innovación** es un motor de análisis bibliométrico avanzado que utiliza Modelos de Lenguaje de Gran Escala (LLM) para mapear el panorama tecnológico y científico global. Su objetivo es transformar metadatos crudos de publicaciones en una visualización de áreas de concentración de conocimiento.

## 1. Arquitectura de Muestreo por Impacto
A diferencia del análisis de flujo diario, el Radar se basa en una ventana de observación semestral para garantizar la madurez de las métricas de citación.

- **Ventana Temporal**: Análisis de periodos de 6 meses (ej. Julio-Diciembre). Este intervalo permite que el factor de transferencia de conocimiento y las citas acumuladas sean estadísticamente significativos.
- **Selección de Datos**: Recuperación de los **500 trabajos más citados** (`is-referenced-by-count` descendente) a través de la API de CrossRef.
- **Densidad de Información**: El muestreo de 500 registros permite a la IA detectar conceptos clave y tecnologías emergentes mediante la normalización de títulos y metadatos asociados.

## 2. Motor de Inferencia y Normalización
El procesamiento se realiza mediante el modelo Google Gemini, configurado para analizar los títulos de las publicaciones y extraer temáticas relevantes.

### Metodología de Procesamiento:
1. **Extracción y Normalización**: La IA analiza los títulos para extraer hasta 3 tecnologías, métodos o conceptos clave por artículo. Aplica reglas de normalización para agrupar sinónimos (ej: "Machine Learning" y "ML") y términos canónicos.
2. **Evaluación de Tendencia**: Cada concepto es comparado con el contexto tecnológico actual para asignar una categoría de madurez (Novedad, Alza, Consolidado).
3. **Métrica de Intensidad (`count`)**: Representa la frecuencia absoluta de artículos que mencionan o se encuadran dentro de ese concepto en la muestra analizada.

## 3. Visualización y Taxonomía de Tendencias
El radar utiliza un enfoque de **visualización plana** para maximizar la claridad en el descubrimiento de tendencias:

1. **Gráfico de Burbujas Dinámico (Packed Bubble Chart)**: 
   - Las burbujas representan tendencias científicas o conceptos clave.
   - El **diámetro** de cada burbuja indica el volumen de publicaciones detectado en la muestra (intensidad).
   - El **color** indica el estado de cada temática.

2. **Categorización por Estado de Madurez e Impacto**:
   - Cada tendencia se clasifica en una de tres categorías de ciclo de vida:
     - **`new` (Novedades)**: Innovaciones científicas recientes que están empezando a aparecer muy recientemente.
     - **`rising` (En Alza)**: Temáticas o tecnologías en fuerte crecimiento y alta tracción.
     - **`stable` (Consolidados)**: Conceptos maduros que se han establecido como estándares en su campo.

## 4. Listado Detallado por Columnas
Bajo el gráfico, la interfaz presenta un listado estructurado en tres columnas (Consolidados, En Alza, Novedades) que permite una consulta rápida y categorizada de los resultados, eliminando la redundancia y enfocándose en la jerarquía de los datos detectados.

## 5. Persistencia y Optimización de Capa de Datos
El sistema implementa una **Caché de Inteligencia** en la base de datos relacional:
- **Validación de Caché**: Antes de invocar al LLM, el sistema verifica en el repositorio si ya existe un análisis procesado para el periodo de referencia.
- **Formato de Persistencia**: Los resultados se almacenan en formato JSON dentro de una columna de tipo `TEXT` en la tabla `ai_radar`.

```json
[
  {
    "tag": "Generative AI & LLMs",
    "count": 82,
    "trend": "rising"
  }
]
```
