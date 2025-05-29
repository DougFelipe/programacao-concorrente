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
  echo "🚀 Executando algoritmo completo com GC: $GC..."

  JFR_FILE="$JFR_DIR/algoritmoCompleto_${GC}.jfr"

  java $JAVA_OPTS \
    -XX:+Use${GC} \
    -XX:StartFlightRecording=filename=$JFR_FILE,duration=90s,settings=profile \
    -cp "target/$JAR_NAME" \
    $MAIN_CLASS "$DATASET_PATH" "$METHOD_NAME"

  if [ $? -eq 0 ]; then
    echo "✅ Execução concluída com $GC"
  else
    echo "⚠️ Erro na execução com $GC"
  fi
done

echo -e "\n🏁 Finalizado. Os arquivos JFR estão em: $JFR_DIR"
