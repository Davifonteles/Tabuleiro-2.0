package Jogodetabuleiro.Tela;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Cores, fontes e componentes visuais usados em todas as telas. */
public final class Estilo {
    private Estilo() {}

    public static final Color FUNDO = new Color(0xEFE8DA);
    public static final Color PAINEL = new Color(0xFBF8F1);
    public static final Color BORDA = new Color(0xDDD3C0);
    public static final Color TEXTO = new Color(0x2B2620);
    public static final Color TEXTO_SUAVE = new Color(0x7A7064);
    public static final Color DESTAQUE = new Color(0x1F5C4A);
    public static final Color DESTAQUE_CLARO = new Color(0x2C7A63);
    public static final Color OURO = new Color(0xD9A637);

    /** Uma cor para cada jogador (até 6). */
    public static final Color[] CORES_JOGADORES = {
        new Color(0xD64545), new Color(0x2F6FD6), new Color(0x2E9E5B),
        new Color(0xE08A1E), new Color(0x8A4FD0), new Color(0x1BA3A3)
    };

    public static Font fonte(int estilo, float tamanho) {
        return new Font(Font.SANS_SERIF, estilo, Math.round(tamanho));
    }

    public static JLabel rotulo(String texto, int estilo, float tamanho, Color cor) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(estilo, tamanho));
        l.setForeground(cor);
        return l;
    }

    /** Painel claro com cantos arredondados. */
    public static class Cartao extends JPanel {
        private final int raio;

        public Cartao(LayoutManager layout, int raio) {
            super(layout);
            this.raio = raio;
            setOpaque(false);
            setBorder(new EmptyBorder(14, 16, 14, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PAINEL);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);
            g2.setColor(BORDA);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Botão arredondado com cor sólida. */
    public static class Botao extends JButton {
        private final Color cor;
        private final Color corTexto;
        private boolean sobre;

        public Botao(String texto, Color cor, Color corTexto) {
            super(texto);
            this.cor = cor;
            this.corTexto = corTexto;
            setFont(fonte(Font.BOLD, 14));
            setForeground(corTexto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 16, 9, 16));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { sobre = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { sobre = false; repaint(); }
            });
        }

        public static Botao primario(String texto) {
            return new Botao(texto, DESTAQUE, Color.WHITE);
        }

        public static Botao secundario(String texto) {
            return new Botao(texto, new Color(0xE6DDCB), TEXTO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = cor;
            if (!isEnabled()) {
                c = new Color(0xCFC7B8);
            } else if (getModel().isPressed()) {
                c = cor.darker();
            } else if (sobre) {
                c = misturar(cor, Color.WHITE, 0.12f);
            }
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            setForeground(isEnabled() ? corTexto : new Color(0x8C8478));
            super.paintComponent(g);
        }
    }

    public static Color misturar(Color a, Color b, float t) {
        return new Color(
            Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
            Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /** Ícone redondo de uma cor, usado em listas e menus. */
    public static Icon bolinha(Color cor, int tamanho) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cor);
                g2.fillOval(x, y, tamanho, tamanho);
                g2.setColor(cor.darker());
                g2.drawOval(x, y, tamanho - 1, tamanho - 1);
                g2.dispose();
            }
            @Override public int getIconWidth() { return tamanho; }
            @Override public int getIconHeight() { return tamanho; }
        };
    }

    /** Faixa com a legenda dos tipos de casa. */
    public static JPanel legenda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4));
        p.setOpaque(false);
        for (TipoCasa t : TipoCasa.values()) {
            JLabel l = rotulo(t.nome, Font.PLAIN, 12, TEXTO_SUAVE);
            l.setIcon(quadrado(t.fundo, 12));
            l.setIconTextGap(5);
            l.setToolTipText(t.descricao);
            p.add(l);
        }
        return p;
    }

    public static Icon quadrado(Color cor, int tamanho) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cor);
                g2.fillRoundRect(x, y, tamanho, tamanho, 4, 4);
                g2.setColor(BORDA.darker());
                g2.drawRoundRect(x, y, tamanho - 1, tamanho - 1, 4, 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return tamanho; }
            @Override public int getIconHeight() { return tamanho; }
        };
    }
}
