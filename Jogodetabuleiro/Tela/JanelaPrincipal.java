package Jogodetabuleiro.Tela;

import java.awt.*;
import java.util.List;
import javax.swing.*;

import Jogodetabuleiro.CasaGeral;
import Jogodetabuleiro.JogadorGeral;

/** Janela que alterna entre a tela de configuração e a tela do jogo. */
public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {
        super("Tabuleiro 2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1320, 840);
        setLocationRelativeTo(null);
        mostrarConfiguracao();
    }

    public void mostrarConfiguracao() {
        trocarTela(new TelaConfiguracao(this::mostrarJogo), null);
    }

    public void mostrarJogo(List<JogadorGeral> jogadores, List<CasaGeral> casas, boolean debug) {
        TelaJogo tela = new TelaJogo(jogadores, casas, debug, this::mostrarConfiguracao);
        trocarTela(tela, tela.getBotaoPrincipal());
    }

    private void trocarTela(JComponent tela, JButton botaoPadrao) {
        setContentPane(tela);
        getRootPane().setDefaultButton(botaoPadrao);
        revalidate();
        repaint();
    }
}
