package serial;

import serial.utils.Biomarcador;
import serial.utils.FitnessEvaluator;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java -Xmx16G -cp out serial.Main <caminho_dataset> <nome_metodo>");
            System.exit(1);
        }

        String caminhoArquivo = args[0];
        String metodo = args[1];

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

        GeneticAlgorithm ga = new GeneticAlgorithm(
                dados,
                tamanhoPopulacao,
                numGeracoes,
                taxaCrossover,
                taxaMutacao
        );

        // 🧪 Executa apenas o método solicitado
        switch (metodo) {
            case "benchmarkGerarPopulacao":
                ga.benchmarkGerarPopulacao();
                break;
            case "benchmarkSelecaoTorneio":
                ga.benchmarkSelecaoTorneio();
                break;
            case "benchmarkCrossover1Ponto":
                ga.benchmarkCrossover1Ponto();
                break;
            case "benchmarkMutacao":
                ga.benchmarkMutacao();
                break;
            case "benchmarkFitness":
                ga.benchmarkFitness();
                break;
            case "executarAlgoritmoCompleto":
                ga.executar(); // opcional se quiser chamar o completo
                break;
            default:
                System.err.println("❌ Método não reconhecido: " + metodo);
                System.exit(3);
        }
    }
}
