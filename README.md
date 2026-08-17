# Neon Ping Pong

Uma versão expandida do Pong em Java/AWT com estética neon, modos de jogo, habilidades e arena dinâmica.

## Execução

O projeto é compatível com Java 13 ou superior e usa apenas bibliotecas da JDK. Pela IDE Eclipse, execute `pong.main.Game`. Pela linha de comando:

```bash
javac -d bin $(find src -name '*.java' | sort)
java -cp bin pong.main.Game
```

As estatísticas são salvas localmente em `pong-stats.properties`, que não é versionado.

## Modos

| Modo | Regra |
|---|---|
| Clássico | Primeiro jogador a alcançar a meta vence. |
| Sobrevivência | O jogador começa com três vidas; a arena evolui e surge um chefe após três pontos. |
| Turbo | Partida curta, com bola mais veloz e meta reduzida. |
| Versus local | Dois jogadores na mesma máquina; Jogador 1 usa `A/D` ou setas e Jogador 2 usa `J/L`. |

## Controles

`SPACE` ativa a habilidade do Jogador 1; `Q` troca entre Overdrive, Escudo e Raquete XL. No versus, o Jogador 2 usa `I` para ativar e `O` para trocar. `ESC` ou `P` pausa, `M` volta ao menu e `F11` alterna tela cheia.

## Sistemas de jogo

A partida conta com energia, combos, recorde, power-ups de energia, slow, split e multiplicador, obstáculos móveis, zonas turbo, gravidade zero, desafios por sequência, progressão local por XP, estatísticas, sons procedurais, partículas, trilha da bola, tremor de impacto e mensagens contextuais.

## Arena Mutante

No menu, escolha `EDITOR DE ARENA`. Use as setas para mover o cursor, `TAB` para trocar o elemento, `SPACE` para colocar, `X` para remover, `BACKSPACE` para limpar e `ENTER` para salvar. Os elementos disponíveis são bloco, turbo, slow, portal e gravidade. A arena salva é aplicada ao modo `ARENA MUTANTE` e pode ser compartilhada pelo código gerado no modelo `ArenaBlueprint`.

## Skins e itens

A opção `SKINS E ITENS` permite trocar skins de raquete, bola, arena e título. Os itens são liberados por XP e o equipamento é persistido em `pong-stats.properties`. As skins alteram cores, halos, gradientes e a identidade visual da partida.

## Campanha de bosses

A `CAMPANHA BOSS` apresenta quatro chefes: Volt, Mirror, Twin e Gravity. Cada chefe altera o alvo da IA ou a trajetória da bola de forma diferente. O jogador possui três vidas e precisa derrotar cada chefe por impactos sucessivos; derrotas e XP são registradas no save local.
