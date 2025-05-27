#!/bin/bash
set -e
cd "$(dirname "$0")"

echo "🔧 Baixando dependências JMH..."
mkdir -p lib
cd lib

# JMH 1.37 JARs
curl -LO https://repo1.maven.org/maven2/org/openjdk/jmh/jmh-core/1.37/jmh-core-1.37.jar
curl -LO https://repo1.maven.org/maven2/org/openjdk/jmh/jmh-generator-annprocess/1.37/jmh-generator-annprocess-1.37.jar
curl -LO https://repo1.maven.org/maven2/org/openjdk/jmh/jmh-generator-annprocess/1.37/jmh-generator-annprocess-1.37.jar
curl -LO https://repo1.maven.org/maven2/org/openjdk/jmh/jmh-generator-annprocess/1.37/jmh-generator-annprocess-1.37.jar
cd ..

echo "📦 Compilando projeto com JMH..."
mkdir -p out
javac -cp "lib/*:../src/main/java" -d out benchmark/src/main/java/benchmarks/BenchmarkSerial.java ../src/main/java/serial/**/*.java
