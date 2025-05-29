package threads;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.ZZ_Result;

import java.util.BitSet;

@JCStressTest
@Outcome(id = "true, true", expect = Expect.ACCEPTABLE, desc = "Ambas threads realizaram mutações.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Condição de corrida detectada.")
@State
public class ParallelMutationStressTest {

    private final BitSet compartilhado = new BitSet(64);

    @Actor
    public void actor1() {
        mutarCompartilhado();
    }

    @Actor
    public void actor2() {
        mutarCompartilhado();
    }

    @Arbiter
    public void verificar(ZZ_Result r) {
        r.r1 = compartilhado.length() > 0;
        r.r2 = compartilhado.cardinality() > 0;
    }

    private void mutarCompartilhado() {
        for (int i = 0; i < 64; i++) {
            if (Math.random() < 0.5) {
                synchronized (compartilhado) {  // Para comparar versão com/sem lock
                    compartilhado.flip(i);
                }
            }
        }
    }
}
