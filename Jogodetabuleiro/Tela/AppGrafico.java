package Jogodetabuleiro.Tela;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada da versão com interface gráfica.
 * (A versão de console continua em Jogodetabuleiro.Main.)
 */
public class AppGrafico {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("swing.boldMetal", Boolean.FALSE);
        } catch (Exception ignorado) {
            // usa o visual padrão
        }
        SwingUtilities.invokeLater(() -> new JanelaPrincipal().setVisible(true));
    }
}
