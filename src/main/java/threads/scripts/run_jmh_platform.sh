#!/bin/bash
set -e  # Para o script se qualquer comando falhar

# Ir até a raiz do projeto (voltar 4 níveis)
cd "$(dirname "$0")/../../../../../" || {
  echo "❌ Erro ao acessar a raiz do projeto."
  exit 1
}

echo "🛠️ Compilando o projeto com Maven (incluindo benchmarks)..."
if ! mvn clean install -DskipTests; then
  echo "❌ Falha na compilação com Maven."
  exit 1
fi

JAR_FILE="target/benchmarks.jar"

if [[ ! -f "$JAR_FILE" ]]; then
  echo "❌ Arquivo $JAR_FILE não encontrado. Verifique se o projeto está configurado corretamente."
  exit 1
fi

echo "🚀 Executando benchmark com JMH (Parallel Platform)..."
java -Xmx16G -jar "$JAR_FILE" benchmarks.BenchmarkPlatform \
  -wi 3 -i 5 -f1 -bm avgt -tu ms \
  -rf json -rff results/jmh_parallel.json

echo "✅ Benchmark finalizado. Resultados salvos em results/jmh_parallel.json"
