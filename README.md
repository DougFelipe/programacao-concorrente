# Projeto: Algoritmo Genético - Versão Serial (Java)

Este projeto implementa um algoritmo genético para análise de biomarcadores,
com suporte para datasets de até 1GB em formato CSV delimitado por ponto-e-vírgula.

## Estrutura
- `serial/`: Implementação sequencial do algoritmo
- `utils/`: Componentes utilitários (fitness, enums, modelagem)
- `data/`: Datasets usados na execução
- `results/`: Arquivos gerados (CSV de benchmark, HTML do profile)

## Execução
```bash
javac -d out src/main/java/serial/**/*.java
java -Xmx16G -cp out serial.Main data/biomarcadores_1gb.txt
```
