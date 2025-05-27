package benchmarks; 

import org.openjdk.jmh.annotations.*;
import serial.GeneticAlgorithm;
import serial.utils.Biomarcador;
import serial.utils.FitnessEvaluator;

import org.openjdk.jmh.infra.Blackhole;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Random;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkSerial {

    private List<Biomarcador> dados;
    private GeneticAlgorithm ga;
    private List<BitSet> populacao;
    private BitSet individuoA;
    private BitSet individuoB;
    private Random rand;

    @Setup(Level.Invocation)
    public void setup() {
        dados = FitnessEvaluator.carregar("data/biomarcadores_1gb.txt");  // arquivo leve
        ga = new GeneticAlgorithm(dados, 20, 1, 0.9, 0.05);
        populacao = new ArrayList<>();
        int len = dados.size();
        rand = new Random();
        for (int i = 0; i < 20; i++) {
            BitSet b = new BitSet(len);
            for (int j = 0; j < len; j++) {
                if (rand.nextBoolean()) b.set(j);
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
        List<BitSet> nova = new ArrayList<>(20);
        int len = dados.size();
        for (int i = 0; i < 20; i++) {
            BitSet b = new BitSet(len);
            for (int j = 0; j < len; j++) {
                if (rand.nextBoolean()) b.set(j);
            }
            nova.add(b);
        }
    }

    
    @Benchmark
        public void benchmarkSelecaoTorneio(Blackhole blackhole) {
            BitSet a = populacao.get(rand.nextInt(populacao.size()));
            BitSet b = populacao.get(rand.nextInt(populacao.size()));
            blackhole.consume(FitnessEvaluator.avaliar(a, dados));
            blackhole.consume(FitnessEvaluator.avaliar(b, dados));
        }


    @Benchmark
    public void benchmarkCrossover1Ponto() {
        int len = dados.size();
        int ponto = rand.nextInt(len);
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
            if (rand.nextDouble() < 0.05) {
                clone.flip(i);
            }
        }
    }

    @Benchmark
    public void benchmarkFitness(Blackhole blackhole) {
        blackhole.consume(FitnessEvaluator.avaliar(individuoA, dados));
    }
}
