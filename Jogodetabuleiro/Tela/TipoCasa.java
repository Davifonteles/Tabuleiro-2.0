package Jogodetabuleiro.Tela;

import java.awt.Color;

import Jogodetabuleiro.CasaGeral;
import Jogodetabuleiro.Estrategia.*;

/**
 * Liga cada estratégia de casa a um nome, uma cor e uma descrição para a interface.
 * O "codigo" é o mesmo número usado na FabricaCasa.
 */
public enum TipoCasa {
    SIMPLES(0, "Simples", "", new Color(0xFFFDF8), new Color(0x6B6255), "Ganha 1 moeda"),
    SORTE(1, "Sorte", "SORTE", new Color(0x95D59A), new Color(0x1E4A22), "Avança 3 casas (azarados não)"),
    PRISAO(2, "Prisão", "PRISÃO", new Color(0xB4AEA4), new Color(0x2B2925), "Perde a próxima rodada"),
    SURPRESA(3, "Surpresa", "SURPRESA", new Color(0xC9B0F2), new Color(0x3D2470), "Anda de -3 a +3 casas"),
    REVERSA(4, "Reversa", "REVERSA", new Color(0xF7C873), new Color(0x5E3B00), "Inverte a direção"),
    AZAR(5, "Azar", "AZAR", new Color(0xF29A7E), new Color(0x5C1A07), "Volta 2 casas"),
    JOGA_DE_NOVO(6, "Joga de novo", "DE NOVO", new Color(0x8ACDE8), new Color(0x0C3F55), "Ganha uma jogada extra"),
    DERROTA(7, "Derrota", "DERROTA", new Color(0x34302B), new Color(0xF4EFE6), "Eliminado do jogo");

    public final int codigo;
    public final String nome;
    public final String rotulo;
    public final Color fundo;
    public final Color texto;
    public final String descricao;

    TipoCasa(int codigo, String nome, String rotulo, Color fundo, Color texto, String descricao) {
        this.codigo = codigo;
        this.nome = nome;
        this.rotulo = rotulo;
        this.fundo = fundo;
        this.texto = texto;
        this.descricao = descricao;
    }

    public static TipoCasa de(CasaGeral casa) {
        if (casa instanceof CasaComEstrategia c) {
            EstrategiaCasa e = c.getEstrategia();
            if (e instanceof EstrategiaSorte) return SORTE;
            if (e instanceof EstrategiaPrisao) return PRISAO;
            if (e instanceof EstrategiaSurpresa) return SURPRESA;
            if (e instanceof EstrategiaReversa) return REVERSA;
            if (e instanceof EstrategiaAzar) return AZAR;
            if (e instanceof EstrategiaJogaDeNovo) return JOGA_DE_NOVO;
            if (e instanceof EstrategiaDerrota) return DERROTA;
        }
        return SIMPLES;
    }
}
