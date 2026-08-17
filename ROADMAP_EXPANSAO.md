# Roadmap de expansão — Neon Ping Pong

## Objetivo

Transformar o Pong básico em um duelo de arena com identidade própria, feedback forte e conteúdo para várias partidas, sem introduzir dependências externas obrigatórias.

## Sistemas da expansão

| Sistema | Implementação planejada |
|---|---|
| Fluxo | Menu inicial, seleção de modo, pausa, reinício, tela final e configurações rápidas. |
| Modos | Clássico, sobrevivência, turbo e versus local. |
| Gameplay | Energia, habilidade de overdrive, combos, bola mutável e power-ups coletáveis. |
| Arena | Obstáculos e eventos dinâmicos com telemetria visual. |
| Conteúdo | Desafios, progressão local, estatísticas e rodada de chefe. |
| Feedback | Trilha, partículas, flash, tremor, mensagens, sons procedurais e HUD contextual. |
| Social | Segundo jogador local com controles independentes. |

## Regras de design

A dificuldade deve ser previsível e legível. Todo evento especial precisa ser anunciado visualmente antes de afetar a jogada. Poderes devem ter recarga ou custo de energia. O modo clássico deve continuar simples para quem só quer jogar, enquanto os outros modos introduzem complexidade gradualmente.

## Critérios de validação

1. O jogo deve compilar sem dependências de terceiros.
2. O menu deve permitir iniciar os quatro modos e pausar a partida.
3. Cada modo deve ter uma condição de vitória ou pontuação clara.
4. Habilidades e power-ups devem alterar a partida sem travar a simulação.
5. O modo versus deve permitir controles simultâneos para dois jogadores.
6. Estatísticas devem ser salvas localmente de forma tolerante a falhas.
7. O jogo deve continuar renderizando corretamente em resolução lógica `160x120` ampliada para `480x360`.
