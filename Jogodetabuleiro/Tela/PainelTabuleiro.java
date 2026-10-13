package Jogodetabuleiro.Tela;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.*;

import Jogodetabuleiro.CasaGeral;
import Jogodetabuleiro.JogadorGeral;

/**
 * Desenha o tabuleiro em "zigue-zague" (como cobras e escadas) e os peões dos jogadores.
 * Também serve de editor na tela de configuração: clicar numa casa chama o callback.
 */
public class PainelTabuleiro extends JPanel {

    private List<CasaGeral> casas = new ArrayList<>();
    private List<JogadorGeral> jogadores = new ArrayList<>();
    private final Map<JogadorGeral, Color> cores = new HashMap<>();
    private final Map<JogadorGeral, Integer> posicaoExibida = new HashMap<>();
    private JogadorGeral jogadorDaVez;
    private IntConsumer aoClicarCasa;
    private int casaSobMouse = -1;

    private javax.swing.Timer animacao;

    // geometria calculada a cada pintura
    private int colunas, linhas, tamCasa, origemX, origemY;

    public PainelTabuleiro() {
        setOpaque(false);
        setPreferredSize(new Dimension(760, 560));
        ToolTipManager.sharedInstance().registerComponent(this);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = casaEm(e.getPoint());
                if (i != casaSobMouse) {
                    casaSobMouse = i;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                casaSobMouse = -1;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int i = casaEm(e.getPoint());
                if (i >= 0 && aoClicarCasa != null) aoClicarCasa.accept(i);
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setCasas(List<CasaGeral> casas) {
        this.casas = casas;
        repaint();
    }

    public void setJogadores(List<JogadorGeral> jogadores) {
        this.jogadores = jogadores;
        cores.clear();
        posicaoExibida.clear();
        for (int i = 0; i < jogadores.size(); i++) {
            cores.put(jogadores.get(i), Estilo.CORES_JOGADORES[i % Estilo.CORES_JOGADORES.length]);
            posicaoExibida.put(jogadores.get(i), jogadores.get(i).posicao);
        }
        repaint();
    }

    public Color corDe(JogadorGeral j) {
        return cores.getOrDefault(j, Color.GRAY);
    }

    public void setJogadorDaVez(JogadorGeral j) {
        this.jogadorDaVez = j;
        repaint();
    }

    /** Modo editor: clicar numa casa chama este callback com o número da casa. */
    public void setAoClicarCasa(IntConsumer callback) {
        this.aoClicarCasa = callback;
        setCursor(callback != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    /** Anda com os peões casa a casa até a posição real de cada jogador. */
    public void animarAtePosicoes(Runnable aoTerminar) {
        if (animacao != null) animacao.stop();
        animacao = new javax.swing.Timer(130, null);
        animacao.addActionListener(e -> {
            boolean mexeu = false;
            int ultima = Math.max(0, casas.size() - 1);
            for (JogadorGeral j : jogadores) {
                int alvo = Math.min(Math.max(j.posicao, 0), ultima);
                int atual = posicaoExibida.getOrDefault(j, 0);
                if (atual != alvo) {
                    int dist = Math.abs(alvo - atual);
                    int passo = Math.max(1, dist / 10);
                    posicaoExibida.put(j, atual + Integer.signum(alvo - atual) * passo);
                    mexeu = true;
                }
            }
            repaint();
            if (!mexeu) {
                animacao.stop();
                if (aoTerminar != null) aoTerminar.run();
            }
        });
        animacao.start();
    }

    // ---------------------------------------------------------------- geometria

    private void calcularGeometria() {
        int n = Math.max(1, casas.size());
        int margem = 16;
        int larg = Math.max(100, getWidth() - margem * 2);
        int alt = Math.max(100, getHeight() - margem * 2);

        // escolhe o número de colunas que deixa as casas maiores
        int melhorTam = 0, melhorCols = 1;
        for (int c = 3; c <= Math.min(n, 16); c++) {
            int l = (int) Math.ceil(n / (double) c);
            int t = Math.min(larg / c, alt / l);
            if (t > melhorTam) {
                melhorTam = t;
                melhorCols = c;
            }
        }
        if (n < 3) melhorCols = n;
        colunas = melhorCols;
        linhas = (int) Math.ceil(n / (double) colunas);
        tamCasa = Math.min(140, Math.min(larg / colunas, alt / linhas));
        origemX = (getWidth() - tamCasa * colunas) / 2;
        origemY = (getHeight() - tamCasa * linhas) / 2;
    }

    private Rectangle retanguloDaCasa(int i) {
        int linha = i / colunas;
        int col = i % colunas;
        if (linha % 2 == 1) col = colunas - 1 - col;
        return new Rectangle(origemX + col * tamCasa, origemY + linha * tamCasa, tamCasa, tamCasa);
    }

    private int casaEm(Point p) {
        if (casas.isEmpty()) return -1;
        calcularGeometria();
        int col = (p.x - origemX) / tamCasa;
        int linha = (p.y - origemY) / tamCasa;
        if (p.x < origemX || p.y < origemY || col >= colunas || linha >= linhas) return -1;
        if (linha % 2 == 1) col = colunas - 1 - col;
        int i = linha * colunas + col;
        return i < casas.size() ? i : -1;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int i = casaEm(e.getPoint());
        if (i < 0) return null;
        TipoCasa t = TipoCasa.de(casas.get(i));
        String extra = i == 0 ? " · Início" : (i == casas.size() - 1 ? " · Chegada" : "");
        return "<html><b>Casa " + i + extra + "</b><br>" + t.nome + ": " + t.descricao
            + (aoClicarCasa != null ? "<br><i>Clique para mudar o tipo</i>" : "") + "</html>";
    }

    // ---------------------------------------------------------------- pintura

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (casas.isEmpty()) return;
        calcularGeometria();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        desenharCaminho(g2);
        for (int i = 0; i < casas.size(); i++) desenharCasa(g2, i);
        desenharPeoes(g2);

        g2.dispose();
    }

    private void desenharCaminho(Graphics2D g2) {
        Path2D caminho = new Path2D.Double();
        for (int i = 0; i < casas.size(); i++) {
            Rectangle r = retanguloDaCasa(i);
            if (i == 0) caminho.moveTo(r.getCenterX(), r.getCenterY());
            else caminho.lineTo(r.getCenterX(), r.getCenterY());
        }
        g2.setColor(new Color(0xD8CDB8));
        g2.setStroke(new BasicStroke(tamCasa * 0.22f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(caminho);
    }

    private void desenharCasa(Graphics2D g2, int i) {
        Rectangle r = retanguloDaCasa(i);
        int folga = Math.max(3, tamCasa / 16);
        int x = r.x + folga, y = r.y + folga, w = r.width - folga * 2, h = r.height - folga * 2;
        int arco = Math.max(8, tamCasa / 5);

        TipoCasa tipo = TipoCasa.de(casas.get(i));
        boolean inicio = i == 0;
        boolean chegada = i == casas.size() - 1;

        // sombra
        g2.setColor(new Color(0, 0, 0, 28));
        g2.fillRoundRect(x + 1, y + 3, w, h, arco, arco);

        g2.setColor(tipo.fundo);
        g2.fillRoundRect(x, y, w, h, arco, arco);

        Color borda = tipo == TipoCasa.SIMPLES ? Estilo.BORDA : tipo.fundo.darker();
        float espessura = 1.5f;
        if (inicio || chegada) {
            borda = Estilo.OURO;
            espessura = 3f;
        }
        if (i == casaSobMouse && aoClicarCasa != null) {
            borda = Estilo.DESTAQUE;
            espessura = 3f;
        }
        g2.setColor(borda);
        g2.setStroke(new BasicStroke(espessura));
        g2.drawRoundRect(x, y, w, h, arco, arco);

        // número
        g2.setColor(tipo.texto);
        g2.setFont(Estilo.fonte(Font.BOLD, Math.max(10, tamCasa * 0.2f)));
        g2.drawString(String.valueOf(i), x + w / 9, y + g2.getFontMetrics().getAscent() + h / 14);

        // rótulo
        String rotulo = inicio ? "INÍCIO" : (chegada ? "CHEGADA" : tipo.rotulo);
        if (!rotulo.isEmpty() && tamCasa >= 34) {
            float tam = Math.max(8, tamCasa * 0.12f);
            g2.setFont(Estilo.fonte(Font.BOLD, tam));
            FontMetrics fm = g2.getFontMetrics();
            while (fm.stringWidth(rotulo) > w - 6 && tam > 7) {
                tam -= 0.5f;
                g2.setFont(Estilo.fonte(Font.BOLD, tam));
                fm = g2.getFontMetrics();
            }
            g2.setColor((inicio || chegada) && tipo == TipoCasa.SIMPLES ? new Color(0x8A6514) : tipo.texto);
            g2.drawString(rotulo, x + (w - fm.stringWidth(rotulo)) / 2, y + h - fm.getDescent() - h / 12);
        }
    }

    private void desenharPeoes(Graphics2D g2) {
        if (jogadores.isEmpty()) return;
        int ultima = casas.size() - 1;

        Map<Integer, List<JogadorGeral>> porCasa = new TreeMap<>();
        for (JogadorGeral j : jogadores) {
            int pos = Math.min(Math.max(posicaoExibida.getOrDefault(j, 0), 0), ultima);
            porCasa.computeIfAbsent(pos, k -> new ArrayList<>()).add(j);
        }

        int raio = Math.max(6, (int) (tamCasa * 0.15));
        for (Map.Entry<Integer, List<JogadorGeral>> e : porCasa.entrySet()) {
            Rectangle r = retanguloDaCasa(e.getKey());
            List<JogadorGeral> lista = e.getValue();
            int qtd = lista.size();
            for (int k = 0; k < qtd; k++) {
                double cx, cy;
                if (qtd == 1) {
                    cx = r.getCenterX();
                    cy = r.getCenterY() - tamCasa * 0.04;
                } else {
                    // espalha os peões em um círculo pequeno dentro da casa
                    double ang = -Math.PI / 2 + k * 2 * Math.PI / qtd;
                    double d = tamCasa * 0.2;
                    cx = r.getCenterX() + Math.cos(ang) * d;
                    cy = r.getCenterY() - tamCasa * 0.04 + Math.sin(ang) * d;
                }
                desenharPeao(g2, lista.get(k), (int) cx, (int) cy, qtd > 2 ? (int) (raio * 0.85) : raio);
            }
        }
    }

    private void desenharPeao(Graphics2D g2, JogadorGeral j, int cx, int cy, int raio) {
        Color cor = corDe(j);
        Composite antigo = g2.getComposite();
        if (j.derrotado) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));

        if (j == jogadorDaVez) {
            g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 70));
            int halo = raio + Math.max(4, raio / 2);
            g2.fillOval(cx - halo, cy - halo, halo * 2, halo * 2);
        }

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(cx - raio + 1, cy - raio + 3, raio * 2, raio * 2);
        g2.setColor(cor);
        g2.fillOval(cx - raio, cy - raio, raio * 2, raio * 2);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(Math.max(1.5f, raio / 6f)));
        g2.drawOval(cx - raio, cy - raio, raio * 2, raio * 2);

        String inicial = j.nome.isBlank() ? "?" : j.nome.substring(0, 1).toUpperCase();
        g2.setFont(Estilo.fonte(Font.BOLD, raio * 1.1f));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(inicial, cx - fm.stringWidth(inicial) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);

        if (j.derrotado) {
            g2.setComposite(antigo);
            g2.setColor(new Color(0x34302B));
            g2.setStroke(new BasicStroke(2.5f));
            int d = (int) (raio * 0.8);
            g2.drawLine(cx - d, cy - d, cx + d, cy + d);
            g2.drawLine(cx - d, cy + d, cx + d, cy - d);
        }
        g2.setComposite(antigo);
    }
}
