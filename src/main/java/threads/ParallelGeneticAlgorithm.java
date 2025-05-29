package threads;

import serial.utils.Biomarcador;
import serial.utils.FitnessEvaluator;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ParallelGeneticAlgorithm {

    private final List<Biomarcador> dados;
    private final int tamanhoPopulacao;
    private final int numGenerations;
    private final double taxaCrossover;
    private final double taxaMutacao;

    private final ExecutorService pool = Executors.newFixedThreadPool(6);

    private final List<BitSet> populacaoAtual = Collections.synchronizedList(new ArrayList<>());
    private final List<BitSet> populacaoProxima = Collections.synchronizedList(new ArrayList<>());

    public ParallelGeneticAlgorithm(List<Biomarcador> dados, int tamanhoPopulacao, int numGenerations, double taxaCrossover, double taxaMutacao) {
        this.dados = dados;
        this.tamanhoPopulacao = tamanhoPopulacao;
        this.numGenerations = numGenerations;
        this.taxaCrossover = taxaCrossover;
        this.taxaMutacao = taxaMutacao;

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

                BitSet[] filhos = (ThreadLocalRandom.current().nextDouble() < taxaCrossover)
                        ? crossover1Ponto(pai1, pai2)
                        : new BitSet[]{copy(pai1), copy(pai2)};

                for (BitSet filho : filhos) {
                    mutacao(filho);

                    double fit = FitnessEvaluator.avaliar(filho, dados);
                    synchronized (this) {
                        if (fit > melhorFitness) {
                            melhorFitness = fit;
                            melhorIndividuo = filho;
                        }
                    }

                    synchronized (populacaoProxima) {
                        populacaoProxima.add(filho);
                    }

                    if (populacaoProxima.size() == tamanhoPopulacao) break;
                }
            }

            // Swap
            synchronized (populacaoAtual) {
                populacaoAtual.clear();
                populacaoAtual.addAll(populacaoProxima);
            }
            populacaoProxima.clear();
        }

        BitSet melhorClone = (melhorIndividuo != null) ? copy(melhorIndividuo) : null;
        System.out.printf("Melhor fitness encontrado: %.2f\n", melhorFitness);

        pool.shutdown();
    }

    private void gerarPopulacao(List<BitSet> destino) {
        List<Callable<List<BitSet>>> tarefas = new ArrayList<>();
        int blocos = 6;
        int porBloco = tamanhoPopulacao / blocos;
        int sobra = tamanhoPopulacao % blocos;

        for (int i = 0; i < blocos; i++) {
            int finalCount = porBloco + (i < sobra ? 1 : 0);
            tarefas.add(() -> {
                List<BitSet> gerados = new ArrayList<>(finalCount);
                int len = dados.size();
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                for (int j = 0; j < finalCount; j++) {
                    BitSet b = new BitSet(len);
                    for (int k = 0; k < len; k++) {
                        if (rnd.nextBoolean()) b.set(k);
                    }
                    synchronized (destino) {
                        destino.add(b); // Para testes com JCStress
                    }
                }
                return gerados;
            });
        }

        try {
            pool.invokeAll(tarefas);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private BitSet selecaoTorneio(List<BitSet> pop) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        BitSet a = pop.get(rnd.nextInt(pop.size()));
        BitSet b = pop.get(rnd.nextInt(pop.size()));

        double fa = FitnessEvaluator.avaliar(a, dados);
        double fb = FitnessEvaluator.avaliar(b, dados);

        return fa > fb ? a : b;
    }

    private BitSet[] crossover1Ponto(BitSet p1, BitSet p2) {
        BitSet temp1 = new BitSet();
        BitSet temp2 = new BitSet();
        int len = dados.size();
        int ponto = ThreadLocalRandom.current().nextInt(len);
        for (int i = 0; i < len; i++) {
            if (i < ponto) {
                if (p1.get(i)) temp1.set(i);
                if (p2.get(i)) temp2.set(i);
            } else {
                if (p2.get(i)) temp1.set(i);
                if (p1.get(i)) temp2.set(i);
            }
        }
        return new BitSet[]{copy(temp1), copy(temp2)};
    }

    private void mutacao(BitSet cromossomo) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < dados.size(); i++) {
            if (rnd.nextDouble() < taxaMutacao) {
                cromossomo.flip(i);
            }
        }
    }

    private BitSet copy(BitSet original) {
        return (BitSet) original.clone();
    }
}
