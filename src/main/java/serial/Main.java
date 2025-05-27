package serial;

import serial.utils.Biomarcador;
import serial.utils.FitnessEvaluator;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java -Xmx16G -cp out serial.Main <caminho_dataset>");
            System.exit(1);
        }

        String caminhoArquivo = args[0];

        // 🔧 Parâmetros do Algoritmo Genético
        int tamanhoPopulacao = 20;
        int numGeracoes = 1;
        double taxaCrossover = 0.9;
        double taxaMutacao = 0.05;

        // 📥 Leitura do dataset
        List<Biomarcador> dados = FitnessEvaluator.carregar(caminhoArquivo);
        if (dados.isEmpty()) {
            System.err.println("❌ Nenhum biomarcador foi carregado.");
            System.exit(2);
        }

        // 🧬 Execução
        GeneticAlgorithm ga = new GeneticAlgorithm(
                dados,
                tamanhoPopulacao,
                numGeracoes,
                taxaCrossover,
                taxaMutacao
        );

        ga.executar();  // Executa o algoritmo serial otimizado
    }
}
