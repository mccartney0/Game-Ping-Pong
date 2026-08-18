# Estrutura Técnica

> **Padrão:** React é a moldura; Babylon.js é o canvas; `client/src/game` contém a lógica de jogo sem dependência de React.

| Módulo | Responsabilidade |
|---|---|
| `scene.ts` | Cria a cena Babylon e devolve um `GameHandle` com ciclo de vida explícito. |
| `GameWorld.ts` | Coordena vila, personagens, narrativa, combate, IA, câmera e atualização por frame. |
| `InputManager.ts` | Traduz teclado/mouse em ações semânticas e deltas de visão. |
| `actors.ts` | Constrói personagens e inimigos procedurais, com saúde e animação simples de movimento. |
| `data.ts` e `types.ts` | Definições dirigidas por dados de personagens, inimigos e quests. |
| `SaveManager.ts` | Persiste o save local em IndexedDB, com fallback de navegador. |
| `AudioManager.ts` | Gera feedback sonoro via Web Audio API após gesto do usuário. |
| `UIController.ts` | Cria HUD, diálogos, grimório, menus, debug e mensagens sobre o canvas. |

## Fluxo de runtime

`GameCanvas` cria um `Engine` apenas uma vez e chama `createGameScene`. A cena instancia `GameWorld`, que possui seus nós Babylon e chama `update(delta)` em um observador único. A desmontagem chama `GameWorld.dispose`, remove listeners de DOM e encerra o engine.

## Contrato de dados

`CharacterDefinition`, `AbilityDefinition`, `EnemyDefinition`, `QuestDefinition` e `SaveData` mantêm valores configuráveis fora de rotinas de renderização. A progressão do slice é uma máquina de estados explícita: `title → training → wood → forge → tavern → attack → escape → ending`.
