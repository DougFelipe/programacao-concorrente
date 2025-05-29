package benchmarks;

import org.openjdk.jmh.annotations.*;
import threads.ParallelGeneticAlgorithm;
import threads.utils.Biomarcador;
import threads.utils.FitnessEvaluator;

import org.openjdk.jmh.infra.Blackhole;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkPlatform {

    private List<Biomarcador> dados;
    private ParallelGeneticAlgorithm ga;
    private List<BitSet> populacao;
    private BitSet individuoA;
    private BitSet individuoB;

    @Setup(Level.Invocation)
    public void setup() {
        dados = FitnessEvaluator.carregar("data/biomarcadores_1gb.txt");
        ga = new ParallelGeneticAlgorithm(dados, 20, 1, 0.9, 0.05);
        populacao = new ArrayList<>();
        int len = dados.size();
        for (int i = 0; i < 20; i++) {
            BitSet b = new BitSet(len);
            for (int j = 0; j < len; j++) {
                if (ThreadLocalRandom.current().nextBoolean()) b.set(j);
            }
            populacao.add(b);
        }
        individuoA = populacao.get(0);
        individuoB = populacao.get(1);
    }

    @Benchmark
    public void benchmarkExecutarCompleto() {
        ga.executar();
    }

    @Benchmark
    public void benchmarkGerarPopulacao() {
        List<BitSet> nova = Collections.synchronizedList(new ArrayList<>());
        int len = dados.size();
        for (int i = 0; i < 20; i++) {
            BitSet b = new BitSet(len);
            for (int j = 0; j < len; j++) {
                if (ThreadLocalRandom.current().nextBoolean()) b.set(j);
            }
            synchronized (nova) {
                nova.add(b);
            }
        }
    }

    @Benchmark
    public void benchmarkSelecaoTorneio(Blackhole bh) {
        BitSet a = populacao.get(ThreadLocalRandom.current().nextInt(populacao.size()));
        BitSet b = populacao.get(ThreadLocalRandom.current().nextInt(populacao.size()));
        bh.consume(FitnessEvaluator.avaliar(a, dados));
        bh.consume(FitnessEvaluator.avaliar(b, dados));
    }

    @Benchmark
    public void benchmarkCrossover1Ponto() {
        int len = dados.size();
        int ponto = ThreadLocalRandom.current().nextInt(len);
        BitSet f1 = new BitSet(len);
        BitSet f2 = new BitSet(len);
        for (int i = 0; i < len; i++) {
            if (i < ponto) {
                if (individuoA.get(i)) f1.set(i);
                if (individuoB.get(i)) f2.set(i);
            } else {
                if (individuoB.get(i)) f1.set(i);
                if (individuoA.get(i)) f2.set(i);
            }
        }
    }

    @Benchmark
    public void benchmarkMutacao() {
        BitSet clone = (BitSet) individuoA.clone();
        int len = dados.size();
        for (int i = 0; i < len; i++) {
            if (ThreadLocalRandom.current().nextDouble() < 0.05) {
                clone.flip(i);
            }
        }
    }

    @Benchmark
    public void benchmarkFitness(Blackhole bh) {
        bh.consume(FitnessEvaluator.avaliar(individuoA, dados));
    }
}
