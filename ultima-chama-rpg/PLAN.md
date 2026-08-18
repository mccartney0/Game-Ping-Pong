# Plano de Jogo: A Chama do Último Reino — Vertical Slice Ferrosul

## Riscos isolados

### 1. Câmera de terceira pessoa e captura de ponteiro

- **Por que isolar:** a combinação entre movimento por teclado, ataque no botão esquerdo, câmera por mouse e restrições do navegador pode provocar conflito de entrada ou perda de foco.
- **Abordagem:** o `InputManager` expõe ações semânticas e deltas de visão; o `GameWorld` atualiza uma câmera orbital suave, com captura de ponteiro apenas após gesto explícito no canvas.
- **Verificar:** WASD move o personagem na direção relativa à câmera; o mouse gira a câmera após captura; ataque no botão esquerdo continua responsivo; Esc libera a captura sem travar o jogo.

### 2. Transição narrativa para Ferrosul em chamas

- **Por que isolar:** a vila precisa passar do estado acolhedor para o estado de ataque sem reconstruir a cena, perder quests ou deixar inimigos fora do mapa.
- **Abordagem:** a máquina de estados do mundo altera luz, neblina, emissivos, NPCs, objetivos e spawns de inimigo. Efeitos de fogo são agrupados e podem ser ativados/desativados.
- **Verificar:** a interação na Raposa Vermelha muda o céu e a luz, mostra o alerta de ataque, cria o Rastreador e inimigos sem duplicação e mantém o jogador controlável.

### 3. Combate e troca contextual Kael/Dheren

- **Por que isolar:** as duas fantasias de combate exigem alcances, feedback e habilidades diferentes; a troca só pode ocorrer após seu gatilho narrativo.
- **Abordagem:** personagens são definidos por dados e usam o mesmo contrato de `Actor`; ataques e habilidades resolvem por tipo ativo. A campanha libera Dheren no ataque, com IA acompanhante fora do controle direto.
- **Verificar:** Kael projeta magia azul e a instabilidade muda; Dheren produz golpes dourados e Passo Etéreo; inimigos perdem vida, reagem e morrem; a troca não libera membros ausentes da party.

## Construção principal

O slice inclui menu inicial, world 3D de Ferrosul, câmera de terceira pessoa, Kael, Dheren acompanhante, tutorial diegético, quests encadeadas, interação, combate, inimigos, mini-chefe, grimório, HUD mínimo, painel de debug, autosave/manual save e final de fuga para a floresta.

- **Assets necessários:** geometria procedural de vila, árvores, personagens, inimigos, runas, partículas de fogo e painéis de pergaminho; o primeiro lote de geração de imagem foi solicitado, mas a cota diária indisponível impede a inclusão de imagens geradas nesta versão. A direção visual continua registrada em `ideas.md` e será substituída por texturas geradas quando a cota estiver disponível.
- **Verificar:**
  - movimento, sprint, esquiva e câmera respondem sem erro;
  - interfaces mantêm legibilidade e não bloqueiam o canvas;
  - tutorial, tarefas, ataque, mini-chefe e fuga formam uma sequência contínua;
  - IndexedDB registra o capítulo, objetivo, party, habilidades, inventário, codex e configurações;
  - nenhuma ocorrência do nome substituído aparece no runtime;
  - captura `?demo` apresenta uma vista determinística do gameplay;
  - `pnpm check` e `pnpm build` concluem sem falha.
