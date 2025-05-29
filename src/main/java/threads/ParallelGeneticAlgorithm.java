package threads;

import threads.utils.Biomarcador;
import threads.utils.FitnessEvaluator;

import java.util.*;
import java.util.concurrent.*;

public class ParallelGeneticAlgorithm {

    private final List<Biomarcador> dados;
    private final int tamanhoPopulacao;
    private final int numGenerations;
    private final double taxaCrossover;
    private final double taxaMutacao;

    private final ExecutorService pool;

    private final List<BitSet> populacaoAtual;
    private final List<BitSet> populacaoProxima;

    public ParallelGeneticAlgorithm(List<Biomarcador> dados, int tamanhoPopulacao, int numGenerations,
                                    double taxaCrossover, double taxaMutacao) {
        this.dados = dados;
        this.tamanhoPopulacao = tamanhoPopulacao;
        this.numGenerations = numGenerations;
        this.taxaCrossover = taxaCrossover;
        this.taxaMutacao = taxaMutacao;

        this.pool = Executors.newFixedThreadPool(6);
        this.populacaoAtual = Collections.synchronizedList(new ArrayList<>());
        this.populacaoProxima = Collections.synchronizedList(new ArrayList<>());

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

            synchronized (populacaoAtual) {
                populacaoAtual.clear();
                populacaoAtual.addAll(populacaoProxima);
            }

            populacaoProxima.clear();
        }

        // Finaliza a pool apenas após a execução completa
        pool.shutdown();
    }
    
    public void benchmarkGerarPopulacao() {
        List<BitSet> nova = Collections.synchronizedList(new ArrayList<>());
        gerarPopulacao(nova);
    }

    public void benchmarkSelecaoTorneio() {
        selecaoTorneio(populacaoAtual);
    }

    public void benchmarkCrossover1Ponto() {
        if (populacaoAtual.size() >= 2) {
            BitSet p1 = populacaoAtual.get(0);
            BitSet p2 = populacaoAtual.get(1);
            crossover1Ponto(p1, p2);
        }
    }

    public void benchmarkMutacao() {
        if (!populacaoAtual.isEmpty()) {
            BitSet clone = copy(populacaoAtual.get(0));
            mutacao(clone);
        }
    }

    public void benchmarkFitness() {
        if (!populacaoAtual.isEmpty()) {
            BitSet individuo = populacaoAtual.get(0);
            FitnessEvaluator.avaliar(individuo, dados);
        }
    }

    private void gerarPopulacao(List<BitSet> destino) {
        List<Callable<Void>> tarefas = new ArrayList<>();
        int blocos = 6;
        int porBloco = tamanhoPopulacao / blocos;
        int sobra = tamanhoPopulacao % blocos;

        for (int i = 0; i < blocos; i++) {
            int count = porBloco + (i < sobra ? 1 : 0);
            tarefas.add(() -> {
                List<BitSet> locais = new ArrayList<>(count);
                int len = dados.size();
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                for (int j = 0; j < count; j++) {
                    BitSet b = new BitSet(len);
                    for (int k = 0; k < len; k++) {
                        if (rnd.nextBoolean()) b.set(k);
                    }
                    locais.add(b);
                }
                synchronized (destino) {
                    destino.addAll(locais);
                }
                return null;
            });
        }

        try {
            pool.invokeAll(tarefas);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao gerar população paralelamente", e);
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
