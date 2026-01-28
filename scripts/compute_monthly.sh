#!/bin/bash

# Script para generar la Síntesis Temática Mensual
# Se recomienda ejecutarlo el día 1 de cada mes mediante un cron job.
# Ejemplo: 0 2 1 * * /path/to/doiboardbackend/scripts/compute_monthly.sh

# Cálculo del mes anterior y el año correspondiente
PREV_MONTH=$(date -d "last month" +%m | sed 's/^0//')
PREV_YEAR=$(date -d "last month" +%Y)

echo "Generando Síntesis Temática para el periodo: $PREV_YEAR-$PREV_MONTH"

# Llamada al endpoint de procesamiento
# Nota: La entrada de datos (prompt) se construye automáticamente en el backend 
# enviando los 500 artículos con más citas recuperados de Crossref para ese mes.
curl -X GET "http://localhost:8006/api/crossref/analysis/compute/monthly?year=$PREV_YEAR&month=$PREV_MONTH"

echo -e "\nProceso finalizado."
