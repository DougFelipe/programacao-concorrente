#!/bin/bash
cd "$(dirname "$0")"

echo "🚀 Executando benchmark com JMH..."
java -Xmx16G -cp "out:lib/*" org.openjdk.jmh.Main benchmarks.BenchmarkSerial
