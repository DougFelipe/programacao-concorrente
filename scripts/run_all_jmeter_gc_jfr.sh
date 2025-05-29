#!/bin/bash

# ========================
# CONFIGURAÇÃO INICIAL
# ========================
JAR_NAME="geneticoconcorrentejava-1.0-SNAPSHOT-shaded.jar"
JMX_FILE="scripts/jmeter_serial_test.jmx"
DATASET_PATH="data/biomarcadores_1gb.txt"
RESULTS_DIR="results/jmeter_gc_jfr"
JFR_DIR="$RESULTS_DIR/jfr"
MERGED_CSV="$RESULTS_DIR/todos_os_resultados.csv"

METHODS=(
  "benchmarkGerarPopulacao"
  "benchmarkSelecaoTorneio"
  "benchmarkCrossover1Ponto"
  "benchmarkMutacao"
  "benchmarkFitness"
)

GCS=("G1GC" "ZGC" "ParallelGC")

# ========================
# COMPILAÇÃO DO PROJETO
# ========================
echo "🛠️ Compilando o projeto com Maven..."
mvn clean package -DskipTests || { echo "❌ Falha na compilação"; exit 1; }

mkdir -p "$RESULTS_DIR"
mkdir -p "$JFR_DIR"

# ========================
# LOOP PRINCIPAL
# ========================
for METHOD in "${METHODS[@]}"; do
  for GC in "${GCS[@]}"; do
    echo "🚀 Executando $METHOD com $GC..."

    OUTPUT_NAME="${METHOD}_${GC}"
    JTL_FILE="$RESULTS_DIR/${OUTPUT_NAME}.jtl"
    JFR_FILE="$JFR_DIR/${OUTPUT_NAME}.jfr"

    # Executa JMeter com JFR ativo
    ./apache-jmeter-5.6.3/bin/jmeter \
      -n \
      -t "$JMX_FILE" \
      -Jthreads=1 \
      -Jgc="$GC" \
      -Jdataset="$DATASET_PATH" \
      -JjfrFile="$JFR_FILE" \
      -Jmethod="$METHOD" \
      -l "$JTL_FILE"

    # Verificação de sucesso
    if [ $? -eq 0 ]; then
      echo "✅ Concluído: $METHOD com $GC"
    else
      echo "⚠️ Erro em: $METHOD com $GC"
    fi
  done
done

# ========================
# JUNÇÃO DE TODOS OS JTLs
# ========================
echo "🧩 Unificando todos os resultados JMeter em $MERGED_CSV..."
echo "" > "$MERGED_CSV"
first=1
for f in "$RESULTS_DIR"/*.jtl; do
  if [ "$first" -eq 1 ]; then
    cat "$f" > "$MERGED_CSV"
    first=0
  else
    tail -n +2 "$f" >> "$MERGED_CSV"
  fi
done

echo "🎉 Finalizado. Resultados estão em: $RESULTS_DIR"
