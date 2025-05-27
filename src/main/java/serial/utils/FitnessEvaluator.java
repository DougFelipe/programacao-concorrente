package serial.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class FitnessEvaluator {

    /**
     * Carrega biomarcadores a partir de um arquivo no formato:
     * ID;Expressao_Tumoral;Conservacao;Similaridade_Humana;Localizacao(int)
     */
    public static List<Biomarcador> carregar(String caminho) {
        List<Biomarcador> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine(); // ignora cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length != 5) continue;

                String id = partes[0];
                double expressao = Double.parseDouble(partes[1]);
                int conservacao = Integer.parseInt(partes[2]);
                int similaridade = Integer.parseInt(partes[3]);
                int localizacaoCodigo = Integer.parseInt(partes[4]);

                Localizacao localizacao = Localizacao.from(localizacaoCodigo);
                Biomarcador b = new Biomarcador(id, expressao, conservacao, similaridade, localizacao);
                lista.add(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Avalia o fitness de um cromossomo usando BitSet.
     */
    public static double avaliar(BitSet cromossomo, List<Biomarcador> dados) {
        double fitness = 0.0;

        for (int i = cromossomo.nextSetBit(0); i >= 0; i = cromossomo.nextSetBit(i + 1)) {
            Biomarcador b = dados.get(i);

            fitness += b.expressao + b.conservacao;

            if (b.similaridadeHumana > 30) {
                fitness -= 50;
            }

            if (b.localizacao == Localizacao.MEMBRANA || b.localizacao == Localizacao.SECRECAO) {
                fitness += 20;
            }
        }

        return fitness;
    }
}
