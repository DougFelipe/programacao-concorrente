#!/bin/bash

echo "🛠️ Compilando o projeto com Maven..."
mvn clean install -DskipTests

# Caminho atualizado para refletir o nome correto
JAR_PATH="target/geneticoconcorrentejava-1.0-SNAPSHOT.jar"
RESULTS_DIR="results/jmh_serial"
mkdir -p "$RESULTS_DIR"

METHODS=(
  "benchmarkGerarPopulacao"
  "benchmarkSelecaoTorneio"
  "benchmarkCrossover1Ponto"
  "benchmarkMutacao"
  "benchmarkFitness"
)

for METHOD in "${METHODS[@]}"; do
  echo "🚀 Executando $METHOD..."
  java -Xmx16G -jar "$JAR_PATH" \
    -bm avgt \
    -tu ms \
    -wi 3 -i 5 -f 1 \
    -rf text \
    -rff "$RESULTS_DIR/${METHOD}.txt" \
    "benchmarks.BenchmarkSerial.$METHOD"
done

echo "✅ Todos os benchmarks foram executados e salvos em: $RESULTS_DIR"
