#!/bin/bash

echo "🚀 Executando benchmark com JMH..."
echo "⚙️ Warmup: 3 iterações | Execução: 5 iterações | Forks: 1"

java -jar target/geneticoconcorrentejava-1.0-SNAPSHOT.jar \
  -wi 3 -i 5 -f 1 -t 1 \
  -bm avgt -tu ms
