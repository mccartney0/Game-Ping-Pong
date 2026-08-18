# A Chama do Último Reino — RPG Web

Este repositório contém um **vertical slice jogável de Ferrosul** para a adaptação de *A Chama do Último Reino — Livro I*. O foco é o primeiro arco: treino de Kael, a relação inicial com Dheren, tarefas de vila, a Raposa Vermelha, o ataque, o Rastreador de Sangue e a fuga em direção a Elwen.

> **Regra de nomenclatura:** todo o jogo usa exclusivamente **Dheren Varenn**.

## Executar localmente

```bash
pnpm install
pnpm dev
```

Abra o endereço informado pelo Vite. A rota `/?demo` inicia uma demonstração determinística do ataque em Ferrosul, útil para validar a cena sem controlar o personagem.

## Controles

| Controle | Ação |
|---|---|
| `WASD` | Movimento relativo à câmera |
| Mouse | Câmera após clicar no canvas |
| Clique esquerdo | Ataque principal |
| Clique direito | Defesa/ação contextual |
| `Space` | Esquiva |
| `Shift` | Corrida |
| `Q` | Habilidade 1 |
| `E` | Habilidade 2 |
| `R` | Habilidade especial/contextual |
| `F` | Interagir |
| `Tab` | Alternar foco de inimigo |
| `1` / `2` | Kael / Dheren, quando a narrativa permite |
| `Esc` | Pausa |
| `F1` | Painel de desenvolvimento |

## Conteúdo implementado

| Sistema | Entrega no vertical slice |
|---|---|
| Mundo | Ferrosul procedural com praça, ferraria, Raposa Vermelha, capela, casas, clareira e limite da floresta. |
| Personagens | Kael e Dheren com perfis, visual procedural, troca contextual e companheiro por IA simples. |
| Combate | Ataque leve, magia/projétil, onda de impulso, espada, Passo Etéreo, esquiva, contra-ataque, foco e inimigos com chefe. |
| Narrativa | Tutorial diegético, madeira para Darion, retorno à ferraria, Raposa Vermelha, ataque e fuga. |
| Sistemas | HUD mínimo, Grimório, menu de pausa, dificuldade, painel de debug, áudio sintetizado e save local em IndexedDB. |
| Direção visual | **Gravura de Cinzas** com marca rúnica, papel queimado, azul Última-Chama e ouro ritual. |

## Arquitetura

A camada React mantém apenas o ciclo de vida do canvas. A lógica sem dependência de React vive em `client/src/game`, especialmente `GameWorld.ts`, `actors.ts`, `InputManager.ts`, `SaveManager.ts`, `AudioManager.ts` e `UIController.ts`. As definições de personagens, inimigos e quests são dirigidas por dados em `data.ts`.

## Documentação de adaptação

Os documentos `docs/LORE_CANON.md`, `PLAN.md`, `STRUCTURE.md`, `ASSETS.md` e `ideas.md` preservam o cânone disponível, decisões de design, riscos, estrutura e manifesto de assets.

## Limitações atuais

O livro em DOCX mencionado no briefing não acompanhou os arquivos disponíveis para a implementação. Portanto, o slice usa somente fatos explicitamente contidos no briefing e evita inventar acontecimentos literários específicos. A geração de imagens também não estava disponível por limite diário no ambiente; por isso, personagens, vila e efeitos são proceduralmente renderizados nesta versão, já organizados para receber texturas geradas em uma iteração posterior.
