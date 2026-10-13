# Interface gráfica do Tabuleiro 2.0

Esta pasta (`Jogodetabuleiro/Tela`) é o frontend em Java Swing. Ela usa as suas classes
(`JogadorGeral`, `CasaGeral`, `Tabuleiro`, as Estratégias e as Fábricas) sem alterá-las.
A versão de console (`Jogodetabuleiro.Main`) continua funcionando.

## Onde colocar

Copie a pasta `Tela` para dentro de `Jogodetabuleiro/`, ao lado de `Estrategia/` e `Fabrica/`.

## Como rodar

**No VS Code / IntelliJ / Eclipse:** abra o projeto e execute a classe
`Jogodetabuleiro.Tela.AppGrafico` (botão "Run" no método `main`).

**Pelo terminal**, a partir da pasta raiz do repositório (a que contém `Jogodetabuleiro/`):

```
javac -encoding UTF-8 -d out Jogodetabuleiro/*.java Jogodetabuleiro/Estrategia/*.java Jogodetabuleiro/Fabrica/*.java Jogodetabuleiro/Tela/*.java
java -cp out Jogodetabuleiro.Tela.AppGrafico
```

Precisa de Java 17 ou mais novo (o seu código já usa `switch` com `->` e `instanceof` com variável).

## Arquivos

| Arquivo | O que faz |
|---|---|
| `AppGrafico.java` | `main` da versão gráfica |
| `JanelaPrincipal.java` | Janela que troca entre configuração e jogo |
| `TelaConfiguracao.java` | Nº de casas, casas especiais (clique na casa), jogadores e modo DEBUG |
| `TelaJogo.java` | Partida: tabuleiro, dados, placar e histórico |
| `PainelTabuleiro.java` | Desenha as casas em zigue-zague e os peões, com animação |
| `PainelDados.java` | Desenha e "rola" os dois dados |
| `MotorJogo.java` | Faz uma jogada por clique (o loop de `Tabuleiro.iniciar()` em passos) |
| `TipoCasa.java` | Nome, cor e descrição de cada tipo de casa |
| `Estilo.java` | Cores, fontes e botões |

## Como as regras foram ligadas

O `MotorJogo` chama `jogarDados()` de cada jogador e `aplicarRegra()` de cada casa, e
mostra no histórico as mensagens que as Estratégias imprimem. Algumas bandeiras que o
loop do console não lia passaram a funcionar na interface:

- **Prisão** marca `pularRodada`; o motor converte em `devePularProximaRodada`, então o jogador perde a vez.
- **Joga de novo** marca `temNovaJogada`; o motor dá a jogada extra.
- **Reversa** marca `reverso`; o jogador passa a andar para trás. Se chegar à casa 0 andando
  para trás, volta a andar para frente (senão ficaria preso no início para sempre).
