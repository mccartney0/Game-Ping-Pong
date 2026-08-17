# Análise e plano de melhorias — Game-Ping-Pong

## Estado atual

O projeto é um jogo Java/AWT de Pong, sem `package.json` ou sistema de build Maven/Gradle. O loop principal roda em uma thread com atualização e renderização fixas em aproximadamente 60 FPS. A resolução lógica é `160x120`, ampliada três vezes em uma janela Swing.

## Principais pontos de melhoria encontrados

| Área | Problema observado | Direção adotada |
|---|---|---|
| Pontuação | A pontuação fica em campos estáticos da bola e é zerada em fluxos diferentes; não existe recorde nem pontuação máxima configurável. | Centralizar o estado da partida em `Game`, exibir placar e recorde e definir vitória por pontuação máxima. |
| Reinício | Ao marcar ponto, `new Game()` é instanciado, criando outro Canvas, listeners e objetos sem encerrar a instância anterior. | Reiniciar a rodada com métodos explícitos, preservando a janela e o estado da partida. |
| Colisão | A bola pode inverter a direção sem reposicionamento, favorecendo colisões repetidas; o ângulo não considera a região atingida da raquete. | Reposicionar a bola após a colisão e calcular o ricochete pelo ponto de contato, com aumento gradual de velocidade. |
| Controle | A raquete se move em passos fixos e o código usa `else if`, impedindo combinações futuras de entrada. | Usar velocidade configurável, aceleração suave e atualização independente dos estados esquerda/direita. |
| IA | O adversário segue a bola diretamente e a dificuldade usa um valor global pouco previsível. | Usar velocidade máxima, reação dependente do nível e erro controlado para tornar o adversário desafiador, porém jogável. |
| Estados | Estados são comparados com `==` em `String`, e a transição de nível mistura regras de pontuação com atualização da bola. | Usar constantes de estado e separar pontuação, rodada, vitória, derrota e progressão de nível. |
| Feedback visual | A quadra é preta, as raquetes são blocos sólidos e não há trilha, partículas, flash de colisão ou indicador de ponto. | Adicionar fundo com gradiente, linha central, brilho, partículas, trilha da bola, flash de impacto e animação de pontuação. |
| HUD | O HUD imprime a pontuação do jogador duas vezes e tem posicionamento fixo pouco claro. | Exibir nível, placar `jogador — máquina`, pontuação máxima e instruções de controle. |
| Loop | A execução usa `while (true)` e `Thread.sleep`, sem controle de encerramento ou compensação de variação de tempo. | Manter compatibilidade com o projeto e usar um delta de tempo limitado para atualizar a simulação de modo mais estável. |

## Critérios de aceitação

1. O jogo deve iniciar normalmente e aceitar setas ou `A/D`.
2. Cada ponto deve atualizar o placar sem criar uma nova janela ou instância do jogo.
3. A partida deve terminar quando um dos lados alcançar a pontuação máxima configurada.
4. O recorde deve permanecer disponível durante a execução e ser atualizado quando o jogador superar sua melhor pontuação.
5. A bola deve variar o ricochete de acordo com o local de contato e ganhar velocidade gradualmente.
6. O jogo deve mostrar efeitos visuais perceptíveis em colisões e marcação de pontos, sem dependências externas.
7. O código deve compilar com o nível Java 13 configurado no projeto.

## Expansão implementada

A versão expandida agora inclui menu inicial, seleção de modo, pausa, dificuldade, volume, som procedural, tela cheia, estatísticas locais, progressão por XP, quatro modos de jogo, habilidades com energia, combos, power-ups, arena dinâmica, desafios por sequência, chefe no modo sobrevivência, feedback de câmera e versus local.

### Controles

| Ação | Jogador 1 | Jogador 2 no versus |
|---|---|---|
| Mover | `A/D` ou setas | `J/L` |
| Ativar habilidade | `SPACE` | `I` |
| Trocar habilidade | `Q` | `O` |
| Pausar/continuar | `ESC` ou `P` | `ESC` ou `P` |
| Voltar ao menu | `M` | `M` |
| Tela cheia | `F11` | `F11` |

### Modos

O modo **Clássico** usa pontuação máxima; **Sobrevivência** usa três vidas, eventos de arena e chefe; **Turbo** reduz a duração da partida e acelera a bola; **Versus Local** coloca dois jogadores na mesma janela.
