package Jogodetabuleiro.Tela;

import java.awt.*;
import java.util.Random;
import javax.swing.*;

/** Mostra os dois dados, com uma pequena animação de rolagem. */
public class PainelDados extends JComponent {
    private int dado1 = 0;
    private int dado2 = 0;
    private final Random random = new Random();

    public PainelDados() {
        setPreferredSize(new Dimension(170, 76));
        setMinimumSize(new Dimension(170, 76));
    }

    public void mostrar(int d1, int d2) {
        dado1 = d1;
        dado2 = d2;
        repaint();
    }

    public void limpar() {
        mostrar(0, 0);
    }

    /** Sorteia faces aleatórias por alguns instantes e termina nos valores reais. */
    public void rolar(int d1, int d2, Runnable aoTerminar) {
        final int[] passos = {0};
        javax.swing.Timer t = new javax.swing.Timer(60, null);
        t.addActionListener(e -> {
            passos[0]++;
            if (passos[0] < 8) {
                mostrar(random.nextInt(6) + 1, random.nextInt(6) + 1);
            } else {
                t.stop();
                mostrar(d1, d2);
                if (aoTerminar != null) aoTerminar.run();
            }
        });
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int lado = Math.min(getHeight() - 8, 68);
        int espaco = 16;
        int x = (getWidth() - lado * 2 - espaco) / 2;
        int y = (getHeight() - lado) / 2;
        desenharDado(g2, x, y, lado, dado1, -6);
        desenharDado(g2, x + lado + espaco, y, lado, dado2, 5);
        g2.dispose();
    }

    private void desenharDado(Graphics2D g2, int x, int y, int lado, int valor, double inclinacao) {
        Graphics2D g = (Graphics2D) g2.create();
        g.rotate(Math.toRadians(valor == 0 ? 0 : inclinacao), x + lado / 2.0, y + lado / 2.0);

        g.setColor(new Color(0, 0, 0, 40));
        g.fillRoundRect(x + 2, y + 4, lado, lado, 16, 16);
        g.setColor(valor == 0 ? new Color(0xF3EEE4) : Color.WHITE);
        g.fillRoundRect(x, y, lado, lado, 16, 16);
        g.setColor(Estilo.BORDA.darker());
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, lado, lado, 16, 16);

        if (valor == 0) {
            g.setColor(Estilo.TEXTO_SUAVE);
            g.setFont(Estilo.fonte(Font.BOLD, lado * 0.42f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("?", x + (lado - fm.stringWidth("?")) / 2, y + (lado + fm.getAscent() - fm.getDescent()) / 2);
        } else {
            g.setColor(Estilo.TEXTO);
            int r = Math.max(4, lado / 9);
            int a = x + lado / 4, b = x + lado / 2, c = x + lado * 3 / 4;
            int p = y + lado / 4, q = y + lado / 2, s = y + lado * 3 / 4;
            int[][] pontos = switch (valor) {
                case 1 -> new int[][]{{b, q}};
                case 2 -> new int[][]{{a, p}, {c, s}};
                case 3 -> new int[][]{{a, p}, {b, q}, {c, s}};
                case 4 -> new int[][]{{a, p}, {c, p}, {a, s}, {c, s}};
                case 5 -> new int[][]{{a, p}, {c, p}, {b, q}, {a, s}, {c, s}};
                default -> new int[][]{{a, p}, {c, p}, {a, q}, {c, q}, {a, s}, {c, s}};
            };
            if (valor == 1) g.setColor(new Color(0xC23B3B));
            for (int[] pt : pontos) g.fillOval(pt[0] - r, pt[1] - r, r * 2, r * 2);
        }
        g.dispose();
    }
}
