package Jogodetabuleiro.Tela;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Jogodetabuleiro.CasaGeral;
import Jogodetabuleiro.JogadorGeral;

/** Tela da partida: tabuleiro no centro, dados, jogadores e histórico ao lado. */
public class TelaJogo extends JPanel {

    private final MotorJogo motor;
    private final boolean debug;
    private final Runnable aoNovoJogo;

    private final PainelTabuleiro tabuleiro = new PainelTabuleiro();
    private final PainelDados dados = new PainelDados();
    private final JLabel rotuloRodada = Estilo.rotulo("", Font.BOLD, 12, Estilo.TEXTO_SUAVE);
    private final JLabel rotuloVez = Estilo.rotulo("", Font.BOLD, 20, Estilo.TEXTO);
    private final JLabel rotuloTipoVez = Estilo.rotulo("", Font.PLAIN, 13, Estilo.TEXTO_SUAVE);
    private final Estilo.Botao botaoJogar = Estilo.Botao.primario("Jogar dados");
    private final JSpinner spinnerDestino = new JSpinner();
    private final JPanel listaJogadores = new JPanel();
    private final JTextArea historico = new JTextArea();
    private final List<CartaoJogador> cartoes = new ArrayList<>();

    public TelaJogo(List<JogadorGeral> jogadores, List<CasaGeral> casas, boolean debug, Runnable aoNovoJogo) {
        this.debug = debug;
        this.aoNovoJogo = aoNovoJogo;

        setBackground(Estilo.FUNDO);
        setLayout(new BorderLayout(18, 0));
        setBorder(new EmptyBorder(18, 20, 18, 20));

        historico.setEditable(false);
        historico.setLineWrap(true);
        historico.setWrapStyleWord(true);
        historico.setFont(Estilo.fonte(Font.PLAIN, 12));
        historico.setForeground(Estilo.TEXTO);
        historico.setBackground(Estilo.PAINEL);
        historico.setBorder(new EmptyBorder(6, 8, 6, 8));

        motor = new MotorJogo(jogadores, casas, this::registrar);

        tabuleiro.setCasas(casas);
        tabuleiro.setJogadores(jogadores);

        add(painelCentral(), BorderLayout.CENTER);
        add(painelLateral(), BorderLayout.EAST);

        registrar("Partida iniciada com " + jogadores.size() + " jogador(es) e " + casas.size()
            + " casas. Vence quem chegar à casa " + (casas.size() - 1) + ".");
        atualizar();
    }

    /** Botão que deve responder ao Enter. */
    public JButton getBotaoPrincipal() {
        return botaoJogar;
    }

    // ---------------------------------------------------------------- montagem

    private JComponent painelCentral() {
        Estilo.Cartao cartao = new Estilo.Cartao(new BorderLayout(0, 8), 18);
        cartao.add(tabuleiro, BorderLayout.CENTER);
        cartao.add(Estilo.legenda(), BorderLayout.SOUTH);
        return cartao;
    }

    private JComponent painelLateral() {
        JPanel lateral = new JPanel(new BorderLayout(0, 12));
        lateral.setOpaque(false);
        lateral.setPreferredSize(new Dimension(360, 10));

        // --- vez + dados + botão
        Estilo.Cartao vez = new Estilo.Cartao(new BorderLayout(0, 10), 18);
        JPanel textos = new JPanel(new GridLayout(3, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(rotuloRodada);
        textos.add(rotuloVez);
        textos.add(rotuloTipoVez);
        vez.add(textos, BorderLayout.NORTH);
        vez.add(dados, BorderLayout.CENTER);

        JPanel acoes = new JPanel(new BorderLayout(8, 0));
        acoes.setOpaque(false);
        if (debug) {
            int ultima = motor.getUltimaCasa();
            spinnerDestino.setModel(new SpinnerNumberModel(0, 0, ultima, 1));
            spinnerDestino.setFont(Estilo.fonte(Font.PLAIN, 14));
            ((JSpinner.DefaultEditor) spinnerDestino.getEditor()).getTextField().setColumns(3);
            JPanel linha = new JPanel(new BorderLayout(6, 0));
            linha.setOpaque(false);
            linha.add(Estilo.rotulo("DEBUG · casa:", Font.BOLD, 12, new Color(0xA0522D)), BorderLayout.WEST);
            linha.add(spinnerDestino, BorderLayout.CENTER);
            acoes.add(linha, BorderLayout.WEST);
            botaoJogar.setText("Mover");
        }
        botaoJogar.setFont(Estilo.fonte(Font.BOLD, 16));
        botaoJogar.setBorder(new EmptyBorder(12, 16, 12, 16));
        botaoJogar.addActionListener(e -> jogar());
        acoes.add(botaoJogar, BorderLayout.CENTER);
        vez.add(acoes, BorderLayout.SOUTH);

        // --- jogadores
        Estilo.Cartao cartaoJogadores = new Estilo.Cartao(new BorderLayout(0, 8), 18);
        cartaoJogadores.add(Estilo.rotulo("Placar", Font.BOLD, 15, Estilo.TEXTO), BorderLayout.NORTH);
        listaJogadores.setLayout(new BoxLayout(listaJogadores, BoxLayout.Y_AXIS));
        listaJogadores.setOpaque(false);
        for (JogadorGeral j : motor.getJogadores()) {
            CartaoJogador c = new CartaoJogador(j);
            cartoes.add(c);
            listaJogadores.add(c);
        }
        cartaoJogadores.add(listaJogadores, BorderLayout.CENTER);

        // --- histórico
        Estilo.Cartao cartaoHistorico = new Estilo.Cartao(new BorderLayout(0, 8), 18);
        cartaoHistorico.add(Estilo.rotulo("Histórico", Font.BOLD, 15, Estilo.TEXTO), BorderLayout.NORTH);
        JScrollPane rolagem = new JScrollPane(historico);
        rolagem.setBorder(BorderFactory.createLineBorder(Estilo.BORDA));
        rolagem.setPreferredSize(new Dimension(10, 150));
        cartaoHistorico.add(rolagem, BorderLayout.CENTER);
        Estilo.Botao novo = Estilo.Botao.secundario("Novo jogo");
        novo.addActionListener(e -> {
            if (motor.isTerminado() || JOptionPane.showConfirmDialog(this,
                    "Abandonar a partida atual e voltar à configuração?", "Novo jogo",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                aoNovoJogo.run();
            }
        });
        cartaoHistorico.add(novo, BorderLayout.SOUTH);

        JPanel topo = new JPanel(new BorderLayout(0, 12));
        topo.setOpaque(false);
        topo.add(vez, BorderLayout.NORTH);
        topo.add(cartaoJogadores, BorderLayout.CENTER);

        lateral.add(topo, BorderLayout.NORTH);
        lateral.add(cartaoHistorico, BorderLayout.CENTER);
        return lateral;
    }

    // ---------------------------------------------------------------- jogada

    private void jogar() {
        if (motor.isTerminado()) {
            aoNovoJogo.run();
            return;
        }
        botaoJogar.setEnabled(false);
        Integer destino = debug ? (Integer) spinnerDestino.getValue() : null;
        MotorJogo.Resultado r = motor.jogar(destino);
        if (r == null) {
            atualizar();
            return;
        }

        Runnable mover = () -> tabuleiro.animarAtePosicoes(() -> {
            atualizar();
            if (motor.isTerminado()) fimDeJogo();
        });

        if (r.usouDados) {
            dados.rolar(r.dado1, r.dado2, mover);
        } else {
            dados.limpar();
            mover.run();
        }
    }

    private void atualizar() {
        JogadorGeral daVez = motor.getJogadorDaVez();
        tabuleiro.setJogadorDaVez(daVez);
        rotuloRodada.setText("RODADA " + motor.getRodada());

        if (motor.isTerminado()) {
            JogadorGeral v = motor.getVencedor();
            rotuloRodada.setText("FIM DE JOGO");
            rotuloVez.setText(v != null ? v.nome + " venceu!" : "Sem vencedor");
            rotuloVez.setIcon(v != null ? Estilo.bolinha(tabuleiro.corDe(v), 18) : null);
            rotuloTipoVez.setText(v != null ? "Chegou à casa " + motor.getUltimaCasa() + " em " + v.jogadas + " jogadas" : "Todos foram eliminados");
            botaoJogar.setText("Jogar de novo");
        } else {
            rotuloVez.setText("Vez de " + daVez.nome);
            rotuloVez.setIcon(Estilo.bolinha(tabuleiro.corDe(daVez), 18));
            rotuloVez.setIconTextGap(8);
            String estado = tipoDe(daVez) + " · casa " + daVez.posicao;
            if (daVez.reverso) estado += " · andando para trás";
            rotuloTipoVez.setText(estado);
            if (debug) spinnerDestino.setValue(Math.min(daVez.posicao, motor.getUltimaCasa()));
        }
        botaoJogar.setEnabled(true);
        for (CartaoJogador c : cartoes) c.atualizar();
    }

    private void fimDeJogo() {
        StringBuilder sb = new StringBuilder("<html><body style='width:260px'>");
        JogadorGeral v = motor.getVencedor();
        sb.append(v != null ? "<h2>" + v.nome + " venceu!</h2>" : "<h2>Ninguém venceu</h2>");
        sb.append("<b>Resumo final</b><br>");
        for (JogadorGeral j : motor.getJogadores()) {
            sb.append("• ").append(j.nome).append(" (").append(tipoDe(j)).append("): casa ")
              .append(Math.min(j.posicao, motor.getUltimaCasa())).append(", ")
              .append(j.moedas).append(" moedas, ").append(j.jogadas).append(" jogadas")
              .append(j.derrotado ? " — eliminado" : "").append("<br>");
        }
        sb.append("</body></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Fim de jogo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void registrar(String linha) {
        if (historico.getDocument().getLength() > 0) historico.append("\n");
        historico.append(linha);
        historico.setCaretPosition(historico.getDocument().getLength());
    }

    private static String tipoDe(JogadorGeral j) {
        return j.getClass().getSimpleName().replace("Jogador", "");
    }

    // ---------------------------------------------------------------- placar

    private class CartaoJogador extends JPanel {
        private final JogadorGeral jogador;
        private final JLabel nome = Estilo.rotulo("", Font.BOLD, 14, Estilo.TEXTO);
        private final JLabel info = Estilo.rotulo("", Font.PLAIN, 12, Estilo.TEXTO_SUAVE);
        private final JLabel status = Estilo.rotulo("", Font.BOLD, 11, Color.WHITE);

        CartaoJogador(JogadorGeral jogador) {
            super(new BorderLayout(10, 0));
            this.jogador = jogador;
            setOpaque(false);
            setBorder(new EmptyBorder(6, 8, 6, 8));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

            JPanel textos = new JPanel(new GridLayout(2, 1));
            textos.setOpaque(false);
            textos.add(nome);
            textos.add(info);

            status.setOpaque(true);
            status.setBorder(new EmptyBorder(3, 8, 3, 8));

            add(new JLabel(Estilo.bolinha(tabuleiro.corDe(jogador), 22)), BorderLayout.WEST);
            add(textos, BorderLayout.CENTER);
            JPanel s = new JPanel(new GridBagLayout());
            s.setOpaque(false);
            s.add(status);
            add(s, BorderLayout.EAST);
        }

        void atualizar() {
            nome.setText(jogador.nome + "  (" + tipoDe(jogador) + ")");
            int casa = Math.min(jogador.posicao, motor.getUltimaCasa());
            info.setText("casa " + casa + " · " + jogador.moedas + " moedas · " + jogador.jogadas + " jogadas");

            String texto = null;
            Color cor = null;
            if (jogador == motor.getVencedor()) { texto = "VENCEDOR"; cor = Estilo.OURO.darker(); }
            else if (jogador.derrotado) { texto = "ELIMINADO"; cor = new Color(0x34302B); }
            else if (jogador.devePularProximaRodada) { texto = "PRESO"; cor = new Color(0x7D776E); }
            else if (jogador.reverso) { texto = "REVERSO"; cor = new Color(0xC07F12); }
            else if (jogador == motor.getJogadorDaVez()) { texto = "VEZ"; cor = Estilo.DESTAQUE; }

            status.setVisible(texto != null);
            if (texto != null) {
                status.setText(texto);
                status.setBackground(cor);
            }
            boolean daVez = jogador == motor.getJogadorDaVez();
            setBackground(daVez ? new Color(0xEFE7D6) : Estilo.PAINEL);
            setOpaque(daVez);
            repaint();
        }
    }
}
