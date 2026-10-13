package Jogodetabuleiro.Tela;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Jogodetabuleiro.CasaGeral;
import Jogodetabuleiro.JogadorGeral;
import Jogodetabuleiro.Fabrica.FabricaCasa;
import Jogodetabuleiro.Fabrica.FabricaJogador;

/**
 * Substitui as perguntas do console (Main + Jogo.configTabuleiro + Jogo.config):
 * número de casas, casas especiais, jogadores e modo DEBUG.
 */
public class TelaConfiguracao extends JPanel {

    public interface AoIniciar {
        void iniciar(List<JogadorGeral> jogadores, List<CasaGeral> casas, boolean debug);
    }

    private static final String[] TIPOS_JOGADOR = {"Normal", "Sortudo", "Azarado"};
    private static final int MAX_JOGADORES = 6;

    private final List<CasaGeral> casas = new ArrayList<>();
    private final PainelTabuleiro editor = new PainelTabuleiro();
    private final JSpinner spinnerCasas = new JSpinner(new SpinnerNumberModel(30, 5, 120, 1));
    private final JPanel listaJogadores = new JPanel();
    private final List<LinhaJogador> linhas = new ArrayList<>();
    private final JCheckBox checkDebug = new JCheckBox("Modo DEBUG (escolher a casa em vez de jogar)");
    private final Estilo.Botao botaoAdicionar = Estilo.Botao.secundario("+ Adicionar jogador");
    private final AoIniciar aoIniciar;

    public TelaConfiguracao(AoIniciar aoIniciar) {
        this.aoIniciar = aoIniciar;
        setBackground(Estilo.FUNDO);
        setLayout(new BorderLayout(18, 18));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(cabecalho(), BorderLayout.NORTH);
        add(painelTabuleiro(), BorderLayout.CENTER);
        add(painelJogadores(), BorderLayout.EAST);

        ajustarQuantidadeCasas(30);
        sortearEspeciais();
        adicionarJogador("Jogador 1", 0);
        adicionarJogador("Jogador 2", 0);
    }

    // ---------------------------------------------------------------- montagem

    private JComponent cabecalho() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel titulo = Estilo.rotulo("Tabuleiro 2.0", Font.BOLD, 30, Estilo.TEXTO);
        JLabel sub = Estilo.rotulo("Monte o tabuleiro, escolha os jogadores e comece a partida.", Font.PLAIN, 14, Estilo.TEXTO_SUAVE);
        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(sub);
        p.add(textos, BorderLayout.WEST);
        return p;
    }

    private JComponent painelTabuleiro() {
        Estilo.Cartao cartao = new Estilo.Cartao(new BorderLayout(0, 10), 18);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barra.setOpaque(false);
        barra.add(Estilo.rotulo("Casas no tabuleiro:", Font.BOLD, 14, Estilo.TEXTO));
        spinnerCasas.setFont(Estilo.fonte(Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) spinnerCasas.getEditor()).getTextField().setColumns(3);
        spinnerCasas.addChangeListener(e -> ajustarQuantidadeCasas((Integer) spinnerCasas.getValue()));
        barra.add(spinnerCasas);

        Estilo.Botao sortear = Estilo.Botao.secundario("Sortear casas especiais");
        sortear.addActionListener(e -> sortearEspeciais());
        Estilo.Botao limpar = Estilo.Botao.secundario("Deixar todas simples");
        limpar.addActionListener(e -> limparEspeciais());
        barra.add(sortear);
        barra.add(limpar);

        JLabel dica = Estilo.rotulo("Clique em uma casa para escolher o tipo dela.", Font.ITALIC, 12, Estilo.TEXTO_SUAVE);

        JPanel topo = new JPanel(new BorderLayout(0, 6));
        topo.setOpaque(false);
        topo.add(barra, BorderLayout.NORTH);
        topo.add(dica, BorderLayout.SOUTH);

        editor.setCasas(casas);
        editor.setAoClicarCasa(this::escolherTipo);

        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(editor, BorderLayout.CENTER);
        cartao.add(Estilo.legenda(), BorderLayout.SOUTH);
        return cartao;
    }

    private JComponent painelJogadores() {
        Estilo.Cartao cartao = new Estilo.Cartao(new BorderLayout(0, 12), 18);
        cartao.setPreferredSize(new Dimension(380, 10));

        cartao.add(Estilo.rotulo("Jogadores", Font.BOLD, 18, Estilo.TEXTO), BorderLayout.NORTH);

        listaJogadores.setLayout(new BoxLayout(listaJogadores, BoxLayout.Y_AXIS));
        listaJogadores.setOpaque(false);

        JPanel meio = new JPanel(new BorderLayout(0, 10));
        meio.setOpaque(false);
        meio.add(listaJogadores, BorderLayout.NORTH);
        JPanel linhaBotao = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaBotao.setOpaque(false);
        botaoAdicionar.addActionListener(e -> adicionarJogador("Jogador " + (linhas.size() + 1), 0));
        linhaBotao.add(botaoAdicionar);
        meio.add(linhaBotao, BorderLayout.CENTER);

        JTextArea regras = new JTextArea(
            "Normal: soma dos dois dados.\n" +
            "Sortudo: nunca anda menos de 7; acima disso, +1.\n" +
            "Azarado: -1 na soma e não ganha nas casas de Sorte.\n" +
            "Dados iguais: joga de novo.");
        regras.setEditable(false);
        regras.setOpaque(false);
        regras.setFocusable(false);
        regras.setFont(Estilo.fonte(Font.PLAIN, 12));
        regras.setForeground(Estilo.TEXTO_SUAVE);
        regras.setBorder(new EmptyBorder(8, 0, 0, 0));
        meio.add(regras, BorderLayout.SOUTH);

        JScrollPane rolagem = new JScrollPane(meio);
        rolagem.setBorder(null);
        rolagem.setOpaque(false);
        rolagem.getViewport().setOpaque(false);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        cartao.add(rolagem, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(0, 12));
        rodape.setOpaque(false);
        checkDebug.setOpaque(false);
        checkDebug.setFont(Estilo.fonte(Font.PLAIN, 12));
        checkDebug.setForeground(Estilo.TEXTO);
        rodape.add(checkDebug, BorderLayout.NORTH);
        Estilo.Botao comecar = Estilo.Botao.primario("Começar jogo");
        comecar.setFont(Estilo.fonte(Font.BOLD, 16));
        comecar.setBorder(new EmptyBorder(13, 16, 13, 16));
        comecar.addActionListener(e -> comecar());
        rodape.add(comecar, BorderLayout.SOUTH);
        cartao.add(rodape, BorderLayout.SOUTH);
        return cartao;
    }

    // ---------------------------------------------------------------- casas

    private void ajustarQuantidadeCasas(int n) {
        while (casas.size() > n) casas.remove(casas.size() - 1);
        while (casas.size() < n) casas.add(FabricaCasa.criarCasa(casas.size(), 0));
        editor.repaint();
    }

    private void sortearEspeciais() {
        Random r = new Random();
        for (int i = 0; i < casas.size(); i++) {
            int tipo = 0;
            if (i > 0 && i < casas.size() - 1 && r.nextDouble() < 0.32) {
                // Derrota aparece menos vezes
                tipo = r.nextDouble() < 0.1 ? 7 : 1 + r.nextInt(6);
            }
            casas.set(i, FabricaCasa.criarCasa(i, tipo));
        }
        editor.repaint();
    }

    private void limparEspeciais() {
        for (int i = 0; i < casas.size(); i++) casas.set(i, FabricaCasa.criarCasa(i, 0));
        editor.repaint();
    }

    private void escolherTipo(int numero) {
        JPopupMenu menu = new JPopupMenu();
        JLabel titulo = Estilo.rotulo("  Casa " + numero, Font.BOLD, 13, Estilo.TEXTO);
        titulo.setBorder(new EmptyBorder(4, 4, 4, 4));
        menu.add(titulo);
        menu.addSeparator();
        TipoCasa atual = TipoCasa.de(casas.get(numero));
        for (TipoCasa t : TipoCasa.values()) {
            JMenuItem item = new JMenuItem(t.nome + "  —  " + t.descricao, Estilo.quadrado(t.fundo, 14));
            item.setFont(Estilo.fonte(t == atual ? Font.BOLD : Font.PLAIN, 13));
            item.addActionListener(e -> {
                casas.set(numero, FabricaCasa.criarCasa(numero, t.codigo));
                editor.repaint();
            });
            menu.add(item);
        }
        Point p = editor.getMousePosition();
        if (p == null) p = new Point(editor.getWidth() / 2, editor.getHeight() / 2);
        menu.show(editor, p.x, p.y);
    }

    // ---------------------------------------------------------------- jogadores

    private class LinhaJogador extends JPanel {
        final JTextField nome = new JTextField();
        final JComboBox<String> tipo = new JComboBox<>(TIPOS_JOGADOR);
        final JLabel cor = new JLabel();

        LinhaJogador(String nomeInicial, int tipoInicial) {
            super(new BorderLayout(8, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 8, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

            nome.setText(nomeInicial);
            nome.setFont(Estilo.fonte(Font.PLAIN, 14));
            nome.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDA), new EmptyBorder(5, 8, 5, 8)));
            tipo.setSelectedIndex(tipoInicial);
            tipo.setFont(Estilo.fonte(Font.PLAIN, 13));
            tipo.setPreferredSize(new Dimension(110, 32));

            Estilo.Botao remover = new Estilo.Botao("×", new Color(0xE6DDCB), Estilo.TEXTO);
            remover.setFont(Estilo.fonte(Font.BOLD, 16));
            remover.setBorder(new EmptyBorder(4, 10, 4, 10));
            remover.setToolTipText("Remover jogador");
            remover.addActionListener(e -> removerJogador(this));

            JPanel direita = new JPanel(new BorderLayout(6, 0));
            direita.setOpaque(false);
            direita.add(tipo, BorderLayout.CENTER);
            direita.add(remover, BorderLayout.EAST);

            add(cor, BorderLayout.WEST);
            add(nome, BorderLayout.CENTER);
            add(direita, BorderLayout.EAST);
        }
    }

    private void adicionarJogador(String nome, int tipo) {
        if (linhas.size() >= MAX_JOGADORES) return;
        LinhaJogador linha = new LinhaJogador(nome, tipo);
        linhas.add(linha);
        listaJogadores.add(linha);
        atualizarJogadores();
    }

    private void removerJogador(LinhaJogador linha) {
        if (linhas.size() <= 1) return;
        linhas.remove(linha);
        listaJogadores.remove(linha);
        atualizarJogadores();
    }

    private void atualizarJogadores() {
        for (int i = 0; i < linhas.size(); i++) {
            linhas.get(i).cor.setIcon(Estilo.bolinha(Estilo.CORES_JOGADORES[i], 18));
        }
        botaoAdicionar.setEnabled(linhas.size() < MAX_JOGADORES);
        listaJogadores.revalidate();
        listaJogadores.repaint();
    }

    // ---------------------------------------------------------------- iniciar

    private void comecar() {
        Set<String> nomes = new HashSet<>();
        List<JogadorGeral> jogadores = new ArrayList<>();
        for (LinhaJogador l : linhas) {
            String nome = l.nome.getText().trim();
            if (nome.isEmpty()) {
                aviso("Todos os jogadores precisam de um nome.");
                l.nome.requestFocus();
                return;
            }
            if (!nomes.add(nome.toLowerCase())) {
                aviso("Já existe um jogador chamado \"" + nome + "\". Use nomes diferentes.");
                l.nome.requestFocus();
                return;
            }
            // FabricaJogador usa 1 = Normal, 2 = Sortudo, 3 = Azarado
            jogadores.add(FabricaJogador.criarJogador(nome, l.tipo.getSelectedIndex() + 1));
        }

        List<CasaGeral> copia = new ArrayList<>();
        for (CasaGeral c : casas) copia.add(FabricaCasa.criarCasa(c.getNumero(), TipoCasa.de(c).codigo));

        aoIniciar.iniciar(jogadores, copia, checkDebug.isSelected());
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Tabuleiro 2.0", JOptionPane.WARNING_MESSAGE);
    }
}
