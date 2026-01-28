#!/bin/bash

# Script para generar la Prospectiva Trimestral (Horizon Scanning)
# Se recomienda ejecutarlo el día 1 de los meses: Enero (Q4), Abril (Q1), Julio (Q2), Octubre (Q3).
# Ejemplo: 0 4 1 1,4,7,10 * /path/to/doiboardbackend/scripts/compute_quarterly.sh

# Cálculo del trimestre anterior
# Si estamos en mes 1, 4, 7 o 10, queremos el trimestre que acaba de terminar.
MONTH=$(date +%m | sed 's/^0//')
YEAR=$(date +%Y)

if [ "$MONTH" -eq 1 ]; then
    PREV_QUARTER=4
    PREV_YEAR=$((YEAR - 1))
elif [ "$MONTH" -le 3 ]; then
    PREV_QUARTER=4
    PREV_YEAR=$((YEAR - 1))
elif [ "$MONTH" -le 6 ]; then
    PREV_QUARTER=1
    PREV_YEAR=$YEAR
elif [ "$MONTH" -le 9 ]; then
    PREV_QUARTER=2
    PREV_YEAR=$YEAR
else
    PREV_QUARTER=3
    PREV_YEAR=$YEAR
fi

echo "Generando Horizon Scanning para el periodo: Trimestre $PREV_QUARTER de $PREV_YEAR"

# Llamada al endpoint de procesamiento
# Nota: La entrada de datos (prompt) se construye automáticamente en el backend 
# enviando los 500 artículos con más citas del trimestre seleccionado Y los 500 del anterior para comparar.
curl -X GET "http://localhost:8006/api/crossref/analysis/compute/quarterly?year=$PREV_YEAR&quarter=$PREV_QUARTER"

echo -e "\nProceso finalizado."
