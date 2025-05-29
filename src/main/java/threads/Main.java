package threads;

import threads.utils.Biomarcador;
import threads.utils.FitnessEvaluator;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java -Xmx16G -cp out threads.Main <caminho_dataset> <nome_metodo>");
            System.exit(1);
        }

        String caminhoArquivo = args[0];
        String metodo = args[1];

        int tamanhoPopulacao = 20;
        int numGeracoes = 1;
        double taxaCrossover = 0.9;
        double taxaMutacao = 0.05;

        List<Biomarcador> dados = FitnessEvaluator.carregar(caminhoArquivo);
        if (dados.isEmpty()) {
            System.err.println("❌ Nenhum biomarcador foi carregado.");
            System.exit(2);
        }

        ParallelGeneticAlgorithm ga = new ParallelGeneticAlgorithm(
                dados,
                tamanhoPopulacao,
                numGeracoes,
                taxaCrossover,
                taxaMutacao
        );

        switch (metodo) {
            case "benchmarkGerarPopulacao" -> ga.benchmarkGerarPopulacao();
            case "benchmarkSelecaoTorneio" -> ga.benchmarkSelecaoTorneio();
            case "benchmarkCrossover1Ponto" -> ga.benchmarkCrossover1Ponto();
            case "benchmarkMutacao" -> ga.benchmarkMutacao();
            case "benchmarkFitness" -> ga.benchmarkFitness();
            case "executarAlgoritmoCompleto" -> ga.executar();
            default -> {
                System.err.println("❌ Método não reconhecido: " + metodo);
                System.exit(3);
            }
        }
    }
}
