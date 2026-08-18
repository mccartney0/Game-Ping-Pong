# Relatório — Vertical Slice Ferrosul

## Implementado

O vertical slice fornece menu, mundo 3D de Ferrosul, terceiros pessoa, Kael e Dheren, combate com duas fantasias distintas, tutorial orgânico, tarefas de vila, Raposa Vermelha, ataque, Rastreador de Sangue, Grimório, HUD, save local e final de fuga. A rota `/?demo` automatiza o ataque para revisão visual.

## Arquivos criados

Os módulos de jogo estão em `client/src/game/`, o componente de hospedagem em `client/src/components/GameCanvas.tsx`, o estilo em `client/src/index.css` e os documentos de adaptação na raiz e em `docs/`.

## Como testar

Execute `pnpm install` e `pnpm dev`. Selecione **Acender a chama**, use `Q` duas vezes para concluir o treino, siga os marcadores de interação com `F`, chegue à Raposa Vermelha e derrote o Rastreador e os inimigos para liberar a saída. A rota `/?demo` mostra o combate automaticamente.

## Controles

Os controles completos estão no `README.md`, incluindo movimento por `WASD`, câmera por mouse, `Q/E/R` para habilidades, `F` para interação, `Tab` para foco, `1/2` para party e `F1` para debug.

## Bugs encontrados e corrigidos

Durante a integração, uma colisão de identificador na construção dos telhados e uma importação apenas de tipo para `Scene` impediram o build. Ambos foram corrigidos. A cena foi reiniciada após a correção para eliminar estado residual de recarga a quente.

## Limitações atuais

O mundo usa malhas e materiais procedurais para manter o primeiro build leve. Modelos GLB, animações esqueléticas, as áreas posteriores e a versão validada contra o DOCX canônico permanecem para os próximos marcos. O save recupera a estrutura de progresso, mas a opção de continuar reabre o início do slice enquanto a campanha de checkpoints não é expandida.

## Próximo marco

O próximo marco recomendado é consolidar a versão animada de Ferrosul com assets gerados, adicionar colisão mais precisa e expandir a Floresta de Elwen, onde Lyra pode entrar na narrativa sem antecipar a party completa.
