#!/bin/bash

# ========================
# CONFIGURAÇÃO INICIAL
# ========================
JAR_NAME="geneticoconcorrentejava-1.0-SNAPSHOT.jar"
MAIN_CLASS="serial.Main"
DATASET_PATH="data/biomarcadores_1gb.txt"
JFR_DIR="results/jfr_algoritmo_completo"
JAVA_OPTS="-Xmx16G -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints"
GCS=("G1GC" "ZGC" "ParallelGC")
METHOD_NAME="executarAlgoritmoCompleto"

# ========================
# COMPILAÇÃO DO PROJETO
# ========================
echo "🛠️ Compilando o projeto com Maven..."
mvn clean package -DskipTests || { echo "❌ Falha na compilação"; exit 1; }

if [ ! -f "target/$JAR_NAME" ]; then
  echo "❌ JAR não encontrado em target/$JAR_NAME"
  exit 1
fi

mkdir -p "$JFR_DIR"

# ========================
# EXECUÇÃO PRINCIPAL
# ========================
for GC in "${GCS[@]}"; do
  echo -e "\n🚀 Executando algoritmo completo com GC: $GC..."

  JFR_FILE="$JFR_DIR/algoritmoCompleto_${GC}.jfr"

  # Início da contagem de tempo
  START_TIME=$(date +%s%N)

  java $JAVA_OPTS \
    -XX:+Use${GC} \
    -XX:StartFlightRecording=filename=$JFR_FILE,settings=profile \
    -cp "target/$JAR_NAME" \
    $MAIN_CLASS "$DATASET_PATH" "$METHOD_NAME"

  # Fim da contagem de tempo
  END_TIME=$(date +%s%N)
  DURATION_NS=$((END_TIME - START_TIME))
  DURATION_S=$(awk "BEGIN {printf \"%.2f\", $DURATION_NS/1000000000}")

  if [ $? -eq 0 ]; then
    echo "✅ Execução concluída com $GC"
    echo "⏱️ Tempo de execução com $GC: ${DURATION_S} segundos"
  else
    echo "⚠️ Erro na execução com $GC"
  fi
done

echo -e "\n🏁 Finalizado. Os arquivos JFR estão em: $JFR_DIR"
