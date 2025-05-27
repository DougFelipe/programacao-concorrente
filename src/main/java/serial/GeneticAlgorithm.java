package serial;

import serial.utils.Biomarcador;
import serial.utils.FitnessEvaluator;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {

    private final List<Biomarcador> dados;
    private final int tamanhoPopulacao;
    private final int numGenerations;
    private final double taxaCrossover;
    private final double taxaMutacao;

    private final Random rand = new Random();

    private final List<BitSet> populacaoAtual;
    private final List<BitSet> populacaoProxima;

    // Reuso de objetos temporários para crossover
    private final BitSet temp1 = new BitSet();
    private final BitSet temp2 = new BitSet();

    public GeneticAlgorithm(List<Biomarcador> dados, int tamanhoPopulacao, int numGenerations, double taxaCrossover, double taxaMutacao) {
        this.dados = dados;
        this.tamanhoPopulacao = tamanhoPopulacao;
        this.numGenerations = numGenerations;
        this.taxaCrossover = taxaCrossover;
        this.taxaMutacao = taxaMutacao;

        this.populacaoAtual = new ArrayList<>(tamanhoPopulacao);
        this.populacaoProxima = new ArrayList<>(tamanhoPopulacao);
        gerarPopulacao(populacaoAtual);
    }

    public void executar() {
        BitSet melhorIndividuo = null;
        double melhorFitness = Double.NEGATIVE_INFINITY;

        for (int gen = 0; gen < numGenerations; gen++) {
            populacaoProxima.clear();

            while (populacaoProxima.size() < tamanhoPopulacao) {
                BitSet pai1 = selecaoTorneio(populacaoAtual);
                BitSet pai2 = selecaoTorneio(populacaoAtual);

                BitSet[] filhos = (rand.nextDouble() < taxaCrossover)
                        ? crossover1Ponto(pai1, pai2)
                        : new BitSet[]{copy(pai1), copy(pai2)};

                for (BitSet filho : filhos) {
                    mutacao(filho);

                    double fit = FitnessEvaluator.avaliar(filho, dados);
                    if (fit > melhorFitness) {
                        melhorFitness = fit;
                        melhorIndividuo = filho;  // Evita clone/cópia aqui
                    }

                    populacaoProxima.add(filho);
                    if (populacaoProxima.size() == tamanhoPopulacao) break;
                }
            }

            // Swap das populações
            List<BitSet> temp = populacaoAtual;
            populacaoAtual.clear();
            populacaoAtual.addAll(populacaoProxima);
            populacaoProxima.clear();
        }

        // Só clona o melhor indivíduo uma vez, no final
        BitSet melhorClone = (melhorIndividuo != null) ? copy(melhorIndividuo) : null;
        System.out.printf("Melhor fitness encontrado: %.2f\n", melhorFitness);
    }

    private void gerarPopulacao(List<BitSet> destino) {
        int len = dados.size();
        for (int i = 0; i < tamanhoPopulacao; i++) {
            BitSet b = new BitSet(len);
            for (int j = 0; j < len; j++) {
                if (rand.nextBoolean()) b.set(j);
            }
            destino.add(b);
        }
    }

    private BitSet selecaoTorneio(List<BitSet> pop) {
        BitSet a = pop.get(rand.nextInt(pop.size()));
        BitSet b = pop.get(rand.nextInt(pop.size()));

        double fa = FitnessEvaluator.avaliar(a, dados);
        double fb = FitnessEvaluator.avaliar(b, dados);

        return fa > fb ? a : b;
    }

    private BitSet[] crossover1Ponto(BitSet p1, BitSet p2) {
        temp1.clear();
        temp2.clear();

        int len = dados.size();
        int ponto = rand.nextInt(len);
        for (int i = 0; i < len; i++) {
            if (i < ponto) {
                if (p1.get(i)) temp1.set(i);
                if (p2.get(i)) temp2.set(i);
            } else {
                if (p2.get(i)) temp1.set(i);
                if (p1.get(i)) temp2.set(i);
            }
        }

        return new BitSet[]{copy(temp1), copy(temp2)}; // clone antes de retornar
    }

    private void mutacao(BitSet cromossomo) {
        for (int i = 0; i < dados.size(); i++) {
            if (rand.nextDouble() < taxaMutacao) {
                cromossomo.flip(i);
            }
        }
    }

    private BitSet copy(BitSet original) {
        return (BitSet) original.clone();
    }
}
