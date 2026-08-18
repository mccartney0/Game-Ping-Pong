# FoldCut

FoldCut é um editor de vídeo Android nativo, escrito em Kotlin com Jetpack Compose, Material 3 e Jetpack Media3. O foco do MVP é uma experiência local, rápida e confortável em celulares, tablets e dispositivos foldable, incluindo o Galaxy Z Fold.

> Os vídeos e áudios permanecem no aparelho. O projeto salva referências persistentes aos URIs selecionados e os metadados das edições; o arquivo original nunca é alterado.

## O que já está funcionando

| Área | Implementação |
|---|---|
| Projetos | Novo projeto, múltiplos vídeos, projetos recentes, reabrir e excluir |
| Preview | ExoPlayer/Media3, play/pause, avanço/retrocesso de 5 segundos e sincronização com playhead |
| Timeline | Clipes visuais, faixa de áudio, seleção, seek, arraste, scroll horizontal e pinch-to-zoom |
| Edição | Trim não destrutivo, divisão no playhead, exclusão, movimentação, mute e volume de 0% a 200% |
| Áudio | Importação de MP3/AAC/WAV/M4A, faixa externa, seleção, movimentação, mute e volume |
| Histórico | Undo/redo com até 80 estados e salvamento automático após cada comando |
| Exportação | MP4 com H.264 ou H.265, AAC, progresso, cancelamento e arquivo privado de filmes |
| Compartilhamento | `FileProvider` e Android Sharesheet para assistir, abrir ou compartilhar o MP4 |
| Foldable | Layout compacto abaixo de 680dp e layout amplo com preview e timeline lado a lado |

## Abrir no Android Studio

Abra a pasta `foldcut-android` como um projeto independente no Android Studio. O projeto usa Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, compile SDK 35 e min SDK 26.

Caso o Android Studio não encontre o SDK, crie `local.properties` apontando para a instalação local, por exemplo:

```properties
sdk.dir=/caminho/para/Android/Sdk
```

Depois, sincronize o Gradle e execute a configuração `app` em um aparelho físico ou emulador Android 8.0 ou superior.

## Compilar pela linha de comando

No diretório `foldcut-android`:

```bash
./gradlew assembleDebug
```

O APK será gerado em `app/build/outputs/apk/debug/app-debug.apk`. Para rodar os testes unitários:

```bash
./gradlew testDebugUnitTest
```

## Arquitetura

A descrição completa dos modelos e decisões está em [`ARCHITECTURE.md`](ARCHITECTURE.md). A divisão principal é:

```text
app/src/main/java/com/foldcut/app/
├── MainActivity.kt
├── data/
│   └── ProjectStore.kt
├── domain/
│   └── Models.kt
├── engine/
│   └── Media3ExportEngine.kt
└── ui/
    ├── EditorViewModel.kt
    ├── FoldCutTheme.kt
    ├── HomeViewModel.kt
    ├── components/TimelineView.kt
    └── screens/
        ├── EditorScreen.kt
        └── HomeScreen.kt
```

A reprodução utiliza ExoPlayer e a exportação utiliza a API `Transformer`/`Composition` do Media3. A documentação oficial descreve clipping, remoção de áudio, transcoding, composição de múltiplos ativos e acompanhamento de progresso.[^1] [^2]

## Observações do MVP

A composição de exportação já suporta a sequência principal de vídeo e sequências de áudio externas. Os metadados de posicionamento, volume e fades estão no modelo para que a próxima etapa evolua a mixagem com precisão de timeline, fades e normalização de loudness. O MVP não inclui texto, legendas, stickers, transições, filtros, velocidade ou keyframes.

O campo de resolução está presente na UI e no modelo de exportação. A aplicação pode ser estendida com efeitos `Presentation` do Media3 para aplicar canvas e escala antes da composição final. O projeto mantém essa separação para não acoplar a primeira versão do editor a uma única proporção de vídeo.

## Privacidade

O aplicativo usa o seletor de documentos do Android, solicita somente acesso de leitura aos URIs escolhidos e não declara permissões amplas de armazenamento. Nenhum endpoint de rede ou serviço externo é usado para processar mídia.

## Referências

[^1]: [Media3 Transformer — Getting started](https://developer.android.com/media/media3/transformer/getting-started)
[^2]: [Media3 Transformer — Transformations e Composition](https://developer.android.com/media/media3/transformer/transformations)
