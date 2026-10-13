package Jogodetabuleiro.Tela;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import Jogodetabuleiro.CasaGeral;
import Jogodetabuleiro.JogadorAzarado;
import Jogodetabuleiro.JogadorGeral;
import Jogodetabuleiro.JogadorSortudo;
import Jogodetabuleiro.Tabuleiro;

/**
 * Versão "passo a passo" do loop de Tabuleiro.iniciar(): em vez de rodar o jogo
 * inteiro no console, executa UMA jogada por vez, para a interface poder
 * desenhar cada movimento.
 *
 * As regras das casas continuam sendo as suas Estratégias; as mensagens que
 * elas imprimem com System.out são capturadas e enviadas para o histórico da tela.
 */
public class MotorJogo {

    /** O que aconteceu em uma jogada, para a tela animar. */
    public static class Resultado {
        public JogadorGeral jogador;
        public boolean usouDados;
        public int dado1;
        public int dado2;
        public int casaInicial;
        public int casaFinal;
        public boolean jogaDeNovo;
    }

    private final List<JogadorGeral> jogadores;
    private final List<CasaGeral> casas;
    private final Tabuleiro tabuleiro;
    private final Consumer<String> historico;

    private int vez = 0;
    private int rodada = 1;
    private boolean terminado = false;
    private JogadorGeral vencedor;

    public MotorJogo(List<JogadorGeral> jogadores, List<CasaGeral> casas, Consumer<String> historico) {
        this.jogadores = jogadores;
        this.casas = casas;
        this.historico = historico;
        this.tabuleiro = Tabuleiro.getInstance(jogadores, casas);
        prepararVez();
    }

    public List<JogadorGeral> getJogadores() { return jogadores; }
    public List<CasaGeral> getCasas() { return casas; }
    public int getRodada() { return rodada; }
    public boolean isTerminado() { return terminado; }
    public JogadorGeral getVencedor() { return vencedor; }
    public int getUltimaCasa() { return casas.size() - 1; }

    public JogadorGeral getJogadorDaVez() {
        return terminado ? null : jogadores.get(vez);
    }

    /**
     * Executa uma jogada do jogador da vez.
     * @param destinoDebug casa de destino no modo DEBUG, ou null para jogar os dados
     */
    public Resultado jogar(Integer destinoDebug) {
        if (terminado) return null;

        JogadorGeral j = jogadores.get(vez);
        Resultado r = new Resultado();
        r.jogador = j;
        r.casaInicial = j.posicao;

        int passos;
        boolean dadosIguais = false;

        if (destinoDebug != null) {
            passos = destinoDebug - j.posicao;
            historico.accept("[DEBUG] " + j.nome + " foi colocado na casa " + destinoDebug + ".");
        } else {
            int[] d = semImprimir(j::jogarDados);
            int soma = d[0];
            r.usouDados = true;
            r.dado1 = d[1];
            r.dado2 = d[2];
            dadosIguais = d[1] == d[2];

            String detalhe = "";
            int bruto = d[1] + d[2];
            if (j instanceof JogadorSortudo) {
                detalhe = bruto < 7 ? ", sortudo: ajustado para 7" : ", sortudo: +1 = " + soma;
            } else if (j instanceof JogadorAzarado) {
                detalhe = ", azarado: -1 = " + soma;
            }
            historico.accept(j.nome + " tirou " + d[1] + " e " + d[2] + " (total " + bruto + detalhe + ")");

            passos = j.reverso ? -soma : soma;
            if (j.reverso) {
                historico.accept(j.nome + " está na direção reversa e anda " + soma + " casas para trás.");
            }
        }

        j.posicao = Math.max(0, j.posicao + passos);
        j.jogadas++;

        // zera as "bandeiras" antes de aplicar a regra da casa
        j.temNovaJogada = false;
        j.jogadaExtra = false;
        j.pularRodada = false;

        CasaGeral casa = encontrarCasa(j.posicao);
        if (casa != null) {
            historico.accept(j.nome + " caiu na casa " + j.posicao + " (" + TipoCasa.de(casa).nome + ").");
            for (String msg : capturar(() -> casa.aplicarRegra(j, tabuleiro))) {
                historico.accept("   " + msg);
            }
        }
        j.posicao = Math.max(0, j.posicao);

        // quem anda para trás e chega ao início volta a andar para frente (senão ficaria preso na casa 0)
        if (j.reverso && j.posicao == 0 && !j.derrotado) {
            j.reverso = false;
            historico.accept("   " + j.nome + " voltou ao início e retoma a direção normal.");
        }

        // EstrategiaPrisao marca pularRodada -> vale para a próxima rodada
        if (j.pularRodada) {
            j.devePularProximaRodada = true;
            j.pularRodada = false;
        }

        boolean extra = !j.derrotado && (dadosIguais || j.temNovaJogada || j.jogadaExtra);
        if (dadosIguais && !j.derrotado) {
            historico.accept("   Dados iguais! " + j.nome + " joga de novo.");
        }
        j.temNovaJogada = false;
        j.jogadaExtra = false;

        r.casaFinal = j.posicao;
        r.jogaDeNovo = extra;

        if (j.posicao >= getUltimaCasa()) {
            vencedor = j;
            terminado = true;
            historico.accept("*** " + j.nome + " chegou à última casa e VENCEU o jogo! ***");
        } else if (todosEliminados()) {
            terminado = true;
            historico.accept("Todos os jogadores foram eliminados. Ninguém venceu.");
        } else if (!extra) {
            avancarVez();
            prepararVez();
        }
        return r;
    }

    private void avancarVez() {
        vez = (vez + 1) % jogadores.size();
        if (vez == 0) rodada++;
    }

    /** Pula jogadores eliminados ou presos até achar quem pode jogar. */
    private void prepararVez() {
        int limite = jogadores.size() * 2 + 1;
        for (int i = 0; i < limite; i++) {
            JogadorGeral j = jogadores.get(vez);
            if (j.derrotado) {
                avancarVez();
            } else if (j.devePularProximaRodada) {
                j.devePularProximaRodada = false;
                historico.accept(j.nome + " está preso e perde esta rodada.");
                avancarVez();
            } else {
                return;
            }
        }
        terminado = true;
        historico.accept("Todos os jogadores foram eliminados. Ninguém venceu.");
    }

    private boolean todosEliminados() {
        for (JogadorGeral j : jogadores) {
            if (!j.derrotado) return false;
        }
        return true;
    }

    private CasaGeral encontrarCasa(int numero) {
        for (CasaGeral c : casas) {
            if (c.getNumero() == numero) return c;
        }
        return null;
    }

    private static List<String> capturar(Runnable acao) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream temporario = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(temporario);
            acao.run();
        } finally {
            System.setOut(original);
        }
        List<String> linhas = new ArrayList<>();
        for (String linha : buffer.toString(StandardCharsets.UTF_8).split("\\R")) {
            if (!linha.isBlank()) linhas.add(linha.trim());
        }
        return linhas;
    }

    private static <T> T semImprimir(Supplier<T> acao) {
        PrintStream original = System.out;
        try (PrintStream nulo = new PrintStream(java.io.OutputStream.nullOutputStream())) {
            System.setOut(nulo);
            return acao.get();
        } finally {
            System.setOut(original);
        }
    }
}
