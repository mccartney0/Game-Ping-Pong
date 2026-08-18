# FoldCut — arquitetura do MVP

O FoldCut é um aplicativo Android nativo em Kotlin, Jetpack Compose e Material 3. O projeto preserva os arquivos originais e grava somente referências `content://` e metadados de edição em armazenamento local.

## Camadas

| Camada | Responsabilidade | Pacotes principais |
|---|---|---|
| UI | Telas responsivas, gestos, controles e estados visuais | `ui`, `ui.components`, `ui.screens` |
| Domínio | Modelos de projeto, timeline, clipes e presets | `domain` |
| Dados | Serialização e salvamento automático de projetos | `data` |
| Video Engine | Reprodução, clipping, thumbnails e sincronização | `engine` |
| Export | Composição e exportação MP4 local | `engine.Media3ExportEngine` |

## Fluxo principal

1. A tela inicial solicita vídeos por `ACTION_OPEN_DOCUMENT` com múltipla seleção.
2. O aplicativo lê duração e nome via `ContentResolver`/`MediaMetadataRetriever`, solicita a permissão persistente apenas para os URIs selecionados e cria um `Project`.
3. A tela do editor transforma os `VideoClip` em itens de reprodução Media3, mantendo o playhead em milissegundos na timeline lógica.
4. Cortes, divisões, volumes, mute, remoção de áudio, áudio externo e movimentações produzem novos estados imutáveis do projeto. Cada estado é salvo automaticamente e enviado ao histórico undo/redo.
5. A exportação constrói uma `Composition` Media3 em arquivo MP4 dentro da área privada de filmes do aplicativo. Nenhum conteúdo é enviado para servidores.
6. O resultado usa `FileProvider` e o Android Sharesheet para assistir, abrir ou compartilhar.

## Timeline

A timeline representa cada vídeo como um intervalo `[timelineStartMs, timelineEndMs)`. A posição de origem é preservada por `[sourceStartMs, sourceEndMs)`, permitindo edição não destrutiva. A escala horizontal pode ser alterada por pinch e o deslocamento é mantido como estado local da UI.

Faixas de áudio são armazenadas em `Track.audioClips`. O MVP já permite adicionar, selecionar, mover, cortar logicamente, silenciar e ajustar volume. A composição Media3 recebe as faixas externas como sequências concorrentes; os metadados de posicionamento continuam no modelo para evoluções de mixagem e fades.

## Foldable e telas grandes

A UI usa `BoxWithConstraints` para trocar entre um layout compacto, adequado à tela fechada, e um layout amplo com preview e ferramentas lado a lado, adequado à tela aberta ou tablet. A atividade declara mudanças de tamanho/orientação como configuráveis, preservando o ViewModel e o projeto ao redimensionar a janela.

## Estratégia de performance

A reprodução usa ExoPlayer/Media3 e não carrega o vídeo inteiro em memória. A timeline usa blocos visuais e miniaturas assíncronas por clipe, com `Coil` e URI local. Exportações usam `Transformer.start`, cuja operação é assíncrona e fornece progresso consultável; a interface permanece responsiva e permite cancelamento.

## Escopo do MVP

O MVP entrega criação/reabertura de projeto, importação de múltiplos vídeos, preview, timeline, seleção, playhead, divisão, exclusão, trim, áudio original, áudio externo, mute, volume, undo/redo, persistência automática, exportação MP4 e compartilhamento. Texto, stickers, transições, filtros, velocidade e keyframes ficam representados como pontos de extensão futuros, sem inflar a primeira versão.
