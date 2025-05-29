#!/bin/bash

# ========================
# CONFIGURAÇÃO INICIAL
# ========================
JAR_NAME="geneticoconcorrentejava-1.0-SNAPSHOT.jar"
MAIN_CLASS="serial.Main"
DATASET_PATH="data/biomarcadores_1gb.txt"
JFR_DIR="results/jfr_gc_individual"
JAVA_OPTS="-Xmx16G -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints"

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

# Garantir que a pasta target exista e o JAR compilado também
if [ ! -f "target/$JAR_NAME" ]; then
  echo "❌ JAR não encontrado em target/$JAR_NAME"
  exit 1
fi

mkdir -p "$JFR_DIR"

# ========================
# LOOP PRINCIPAL
# ========================
for METHOD in "${METHODS[@]}"; do
  for GC in "${GCS[@]}"; do
    echo "📊 Rodando $METHOD com GC: $GC..."

    JFR_FILE="$JFR_DIR/${METHOD}_${GC}.jfr"

    java $JAVA_OPTS \
      -XX:+Use${GC} \
      -XX:StartFlightRecording=filename=$JFR_FILE,duration=60s,settings=profile \
      -cp "target/$JAR_NAME" \
      $MAIN_CLASS "$DATASET_PATH" "$METHOD"

    if [ $? -eq 0 ]; then
      echo "✅ JFR concluído: $METHOD com $GC"
    else
      echo "⚠️ Erro ao rodar: $METHOD com $GC"
    fi
  done
done

echo -e "\n🏁 Finalizado. Os arquivos .jfr estão disponíveis em: $JFR_DIR"
