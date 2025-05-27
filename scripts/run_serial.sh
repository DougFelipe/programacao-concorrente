#!/bin/bash

# Navega até a raiz do projeto
cd "$(dirname "$0")/.."

echo "🔧 Compilando código-fonte Java..."
mkdir -p out

# Compila todos os arquivos .java recursivamente a partir de src/main/java
find src/main/java -name "*.java" > sources.txt
javac -d out @sources.txt
rm sources.txt

if [ $? -ne 0 ]; then
    echo "❌ Erro na compilação. Abortando."
    exit 1
fi

echo "✅ Compilação concluída."

# Caminho do dataset (ajuste se necessário)
DATASET="data/biomarcadores_1gb.txt"

if [ ! -f "$DATASET" ]; then
    echo "⚠️  Arquivo de dados não encontrado: $DATASET"
    exit 1
fi

echo "🚀 Executando algoritmo genético com 16GB de heap..."
time java -Xmx16G -cp out serial.Main "$DATASET"
