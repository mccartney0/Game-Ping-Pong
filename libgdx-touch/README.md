# Game Ping Pong — camada libGDX de toque

Esta pasta contém a adaptação mobile do jogo AWT para libGDX. O jogo AWT original permanece intacto em `src/pong/main`. A camada libGDX separa a lógica independente de plataforma (`core`) dos adapters Android (`android`) e do launcher desktop (`lwjgl3`).

> **Objetivo da arquitetura:** o `core` não importa `android.*`; desktop e testes usam fallbacks offline, enquanto o Android injeta Play Games Services e AdMob por contratos estáveis.

## Estrutura

```text
libgdx-touch/
├── core/
│   ├── src/main/java/        mundo, render, gestos e contratos de serviços
│   └── src/test/java/        JUnit: serviços, idempotência e estresse da física
├── android/
│   ├── src/main/java/        launcher, GameApplication e adapters Android
│   └── src/androidTest/      testes Espresso/instrumentados do launcher
├── lwjgl3/                   launcher desktop offline
├── playthrough/              cenário headless de 900 frames
├── gradlew                   Gradle Wrapper 8.2
└── PGS_PLAY_CONSOLE_SETUP.md guia de configuração no Play Console
```

## Gestos e gameplay

| Gesto | Resultado |
|---|---|
| Arrastar na metade inferior | Move a raquete do jogador 1. |
| Arrastar na metade superior | Move a raquete do jogador 2 ou o adversário de teste. |
| Toque duplo na metade inferior | Ativa o poder selecionado do jogador 1. |
| Toque duplo na metade superior | Ativa o poder do jogador 2 no modo Versus ou do adversário de teste nos demais modos. |
| Toque nas bordas esquerda/direita | Troca entre `OVERDRIVE`, `SHIELD` e `WIDE` na metade tocada. |
| Toque em uma linha do overlay | Seleciona modos, ajuda, configurações, placares e rewarded ad. |
| Ponteiros simultâneos | Permite controlar as duas raquetes em paralelo. |
| `pause()` do Android | Pausa o mundo automaticamente quando o app perde foco. |

O reconhecimento usa `Viewport.unproject`, portanto o gesto é convertido para coordenadas lógicas da arena e não depende da resolução física do aparelho. O toque duplo só é aceito dentro de uma janela de `0.28s` e com deslocamento máximo de `0.65` unidade de mundo. Um toque que ultrapasse `0.18` unidade vira arraste e não é contado como toque duplo.

## Fluxo de telas mobile

O menu principal é a porta de entrada padrão do módulo mobile. Ele oferece `JOGAR`, seleção de modo, ajuda, configurações, placares, conquistas e recompensa. A seleção de modo preserva o último modo escolhido, e as páginas de pausa e resultados reutilizam o mesmo controlador de navegação para manter o fluxo consistente em toque, teclado e botão de voltar do Android.

Ao iniciar uma partida, o jogo entra em uma tela de transição com o nome e a descrição do modo, contagem regressiva e estado `GO!`. Durante essa contagem, os gestos são bloqueados e a física não avança. Ao concluir a partida, uma transição curta `MATCH COMPLETE` antecede a página de resultados. O reinício passa novamente por `START_MATCH`, enquanto `VOLTAR AO MENU` usa `RETURN_TO_MENU` antes de deixar o estado jogável.

| Estado | Entrada | Comportamento |
|---|---|---|
| `MAIN` | Inicialização ou retorno | Exibe o menu principal e não atualiza a física. |
| `MODES` | `ESCOLHER MODO` | Permite escolher os seis modos e iniciar a transição da partida. |
| `START_MATCH` | `JOGAR` ou `JOGAR NOVAMENTE` | Exibe contagem regressiva, bloqueia input e prepara uma partida limpa. |
| `PLAYING` | Fim da contagem | Atualiza física, gestos, placar, poderes, power-ups e efeitos. |
| `PAUSE` | Botão de pausa, `BACK`, `ESC` ou `P` | Congela a física e oferece continuar ou retornar. |
| `SHOW_RESULTS` | Partida concluída | Exibe a transição de encerramento antes dos resultados. |
| `RESULTS` | Fim da transição | Mostra jogar novamente ou voltar ao menu. |
| `RETURN_TO_MENU` | `VOLTAR AO MENU` | Limpa a partida e retorna ao menu principal. |

A implementação está em `MobileTransition.java` e é usada por `PingPongTouchGame.java`. O teste `MobileTransitionTest` cobre duração, progresso, contagem regressiva e fallback de tipo; o playthrough também registra `transitions start/results/menu=OK`.

`TouchPongWorld` mantém física determinística, placar, modos `CLASSIC`, `SURVIVAL`, `TURBO`, `VERSUS`, `MUTANT` e `CAMPAIGN`, energia, poderes ativos, power-ups coletáveis e efeitos da bola. `OVERDRIVE` acelera a bola, `SHIELD` bloqueia uma falha e `WIDE` amplia a raquete. Os power-ups `ENERGY`, `SLOW`, `SPLIT` e `MULTI` alteram energia, velocidade, eco visual e multiplicador do próximo ponto. No `VERSUS`, ambos os jogadores têm energia, seleção independente, ativação por duplo toque e podem coletar power-ups; o jogador do topo usa toque na metade superior e as bordas dessa metade para trocar o poder. O render usa `ShapeRenderer` e os efeitos visuais usam pools fixos de trilha e partículas para reduzir alocações por frame.

## Serviços e fallbacks

O módulo `core` define dois contratos sem dependências Android:

| Contrato | Implementação Android | Fallback desktop/testes |
|---|---|---|
| `GameServices` | `AndroidGameServices`, com autenticação, leaderboards e achievements PGS v2 | `NoopGameServices` |
| `MonetizationService` | `AndroidMonetizationService`, com banner adaptativo e rewarded ads | `NoopMonetizationService` |

`AchievementProgress` coordena os marcos principais de forma idempotente. O primeiro ponto e a vitória da partida são enviados no máximo uma vez por instância de jogo, mesmo que o loop processe muitos frames após o marco.

O rewarded ad só chama `onRewardEarned` quando o SDK informa efetivamente a recompensa. Quando o anúncio não está pronto, o fallback chama `onAdUnavailable`; no desktop não há banner nem anúncio real.

## IDs, AdMob e Play Games

O arquivo `android/src/main/res/values/strings.xml` não contém IDs de produção. Os recursos são gerados em `android/build.gradle` por `resValue`.

Durante o desenvolvimento, o padrão usa os **IDs oficiais de teste do AdMob** e placeholders para PGS, leaderboards e achievements. Para uma build real, forneça os valores por propriedades Gradle ou variáveis de ambiente; nunca grave IDs de produção no código versionado.

| Recurso | Propriedade Gradle | Variável de ambiente | Fallback versionado |
|---|---|---|---|
| Projeto PGS | `gameServicesProjectId` | `GAME_SERVICES_PROJECT_ID` | placeholder |
| App ID AdMob | `admobAppId` | `ADMOB_APP_ID` | ID oficial de teste |
| Banner AdMob | `bannerAdUnitId` | `BANNER_AD_UNIT_ID` | ID oficial de teste |
| Rewarded AdMob | `rewardedAdUnitId` | `REWARDED_AD_UNIT_ID` | ID oficial de teste |
| Achievement primeiro ponto | `achievementFirstPoint` | `ACHIEVEMENT_FIRST_POINT` | placeholder |
| Achievement vitória | `achievementMatchWin` | `ACHIEVEMENT_MATCH_WIN` | placeholder |

Os cinco leaderboards também aceitam propriedades e variáveis homônimas documentadas no `android/build.gradle`, por exemplo `LEADERBOARD_SURVIVAL_SCORE`. O procedimento completo para criar os recursos na Play Console está em [`PGS_PLAY_CONSOLE_SETUP.md`](PGS_PLAY_CONSOLE_SETUP.md).

## Assinatura de APK/AAB

Keystores, senhas e aliases nunca são versionados. Localmente, o módulo aceita `libgdx-touch/signing.properties` — isto é, `signing.properties` na raiz deste módulo — e o arquivo é ignorado pelo Git. No GitHub Actions, os valores são fornecidos pelos seguintes **Repository Secrets**:

| Secret | Conteúdo |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Keystore de upload codificado em Base64, sem quebras de linha |
| `ANDROID_KEYSTORE_PASSWORD` | Senha do keystore |
| `ANDROID_KEY_ALIAS` | Alias da upload key |
| `ANDROID_KEY_PASSWORD` | Senha da chave privada |
| `GAME_SERVICES_PROJECT_ID` | ID do projeto do Play Games Services |
| `LEADERBOARD_MUTANT_ARENA_SCORE` | ID real do leaderboard Arena Mutante |
| `LEADERBOARD_CAMPAIGN_BOSSES` | ID real do leaderboard da campanha |
| `LEADERBOARD_SURVIVAL_SCORE` | ID real do leaderboard de sobrevivência |
| `LEADERBOARD_SPEED_RUN_MS` | ID real do leaderboard de speed run |
| `LEADERBOARD_BEST_COMBO` | ID real do leaderboard de melhor combo |
| `ACHIEVEMENT_FIRST_POINT` | ID real da conquista do primeiro ponto |
| `ACHIEVEMENT_MATCH_WIN` | ID real da conquista de vitória |
| `ADMOB_APP_ID` | App ID AdMob de produção |
| `BANNER_AD_UNIT_ID` | ID da unidade de banner AdMob de produção |
| `REWARDED_AD_UNIT_ID` | ID da unidade rewarded AdMob de produção |

As variáveis `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` e `ANDROID_KEY_PASSWORD` também são aceitas pelo Gradle para builds locais ou outros runners. No workflow de release, `ANDROID_KEYSTORE_PATH` é criado automaticamente no diretório temporário do runner a partir de `ANDROID_KEYSTORE_BASE64`.

Exemplo local de `libgdx-touch/signing.properties` — **não commitar**:

```properties
storeFile=/caminho/seguro/game-ping-pong-upload.jks
storePassword=senha-local
keyAlias=game-ping-pong-upload
keyPassword=senha-local
```

Para gerar localmente os dois artefatos de release:

```bash
./gradlew :core:test :android:bundleRelease :android:assembleRelease \
  --no-daemon --stacktrace
```

Os arquivos esperados são:

```text
android/build/outputs/bundle/release/android-release.aab
android/build/outputs/apk/release/android-release.apk
```

Sem credenciais, `assembleRelease` pode compilar, mas não deve ser tratado como artefato pronto para publicação. A configuração de produção precisa apontar para a upload key real.

## Execução local

Requisitos: Java 8 compatível com os módulos libGDX, Android SDK API 35 para o módulo Android e Gradle Wrapper 8.2.

```bash
./gradlew :core:test
./gradlew :core:build
./gradlew :lwjgl3:run
./gradlew :android:assembleDebug
adb install -r android/build/outputs/apk/debug/android-debug.apk
```

O launcher Android usa paisagem, modo imersivo, aceleração de hardware, sem acelerômetro e sem bússola. A inicialização de `PlayGamesSdk` e `MobileAds` ocorre em `GameApplication`.

## Playthrough e testes automatizados

O playthrough `playthrough/TouchPlaythrough.java` percorre o menu principal, seleção de modos, ajuda, configurações e pausa; testa energia, `OVERDRIVE`, `SHIELD`, `WIDE`, Versus com poderes e coleta nos dois lados, coleta de power-ups, Survival, os quatro bosses da campanha e depois simula 900 frames a 60 FPS com dois ponteiros, toque duplo, pontos, pausa, trilha, partículas e finitude numérica.

Os testes JUnit do core cobrem:

| Teste | Cobertura |
|---|---|
| `ServicesTest` | idempotência de unlock, IDs ausentes e fallback offline do rewarded |
| `ServicesIntegrationTest` | envio único do score final e visibilidade do banner após a partida |
| `TouchPongWorldStressTest` | simulação prolongada, limites de placar, efeitos e ausência de NaN/infinito |
| `PowersAndModesTest` | energia, poderes, power-ups, Survival, Campaign e menu |
| `VersusPowerTest` | poderes e coleta de power-ups pelos dois jogadores no modo Versus |
| `AndroidLauncherUiTest` | montagem da superfície Android e banner inicialmente oculto |

Para reproduzir o playthrough sem Android Studio, usando um jar local do libGDX:

```bash
GDX_JAR=/tmp/libgdx-touch-libs/gdx-1.14.2.jar
rm -rf /tmp/libgdx-complete-build
mkdir -p /tmp/libgdx-complete-build
javac -Xlint:all -cp "$GDX_JAR" \
  -d /tmp/libgdx-complete-build \
  $(find core/src/main/java -name '*.java' | sort)
javac -Xlint:all -cp "$GDX_JAR:/tmp/libgdx-complete-build" \
  -d /tmp/libgdx-complete-build playthrough/TouchPlaythrough.java
java -Djava.awt.headless=true \
  -cp "$GDX_JAR:/tmp/libgdx-complete-build" \
  TouchPlaythrough
```

O resultado detalhado fica em `playthrough.log`. O resultado validado nesta versão inclui `score=2:0`, duas ativações por toque duplo, troca de poder pela borda, coleta de power-up, duas transições de pausa, trilha ativa e partículas emitidas.

## Plano de assets

A direção visual e o pipeline de melhoria dos assets estão documentados em [`ASSETS_MOBILE_PLAN.md`](ASSETS_MOBILE_PLAN.md). O plano cobre ícones sem texto para poderes/power-ups, paleta ciano/laranja, arenas com contraste seguro, `TextureAtlas`, escalas de resolução, fallback do `ShapeRenderer` e critérios de acessibilidade/performance. A pesquisa complementar de ferramentas está em [`ASSET_TOOL_RESEARCH.md`](ASSET_TOOL_RESEARCH.md), com recomendações para `TexturePacker`, `TextureAtlas`, `ShaderProgram`, SVG, scripts Python e Blender Geometry Nodes.

## Efeitos visuais da bola

`BallEffects` mantém pools fixos de pontos de trilha e partículas, evitando criar objetos durante cada frame. A trilha acompanha a posição e a velocidade da bola; partículas são emitidas em colisões com paredes e raquetes, ativações de habilidade e pontos. O renderer habilita alpha blending apenas durante a camada visual.

A qualidade pode ser ajustada em três níveis:

```java
world.setEffectsQuality(BallEffects.Quality.LOW);
world.setEffectsQuality(BallEffects.Quality.MEDIUM);
world.setEffectsQuality(BallEffects.Quality.HIGH);
```

`LOW`, `MEDIUM` e `HIGH` controlam a quantidade máxima de pontos de trilha e partículas. Para economizar bateria em aparelhos antigos, use `LOW`, desligue efeitos em menus/pausa e mantenha o `ShapeRenderer` fora de loops de criação de objetos.

## GitHub Actions

O arquivo [`.github/workflows/android-apk.yml`](../.github/workflows/android-apk.yml) executa em alterações do módulo ou manualmente por `workflow_dispatch`. O job usa Java 17 no runner, Android SDK/API 35 e Gradle 8.2; a compatibilidade de bytecode dos módulos permanece Java 8.

A sequência do pipeline debug é:

```bash
./gradlew :core:test :core:build --no-daemon --stacktrace
./gradlew :android:assembleDebug --no-daemon --stacktrace
./gradlew :android:connectedDebugAndroidTest --no-daemon --stacktrace
```

O workflow de release usa os Secrets acima e executa:

```bash
./gradlew :core:test --no-daemon --stacktrace
./gradlew :android:bundleRelease --no-daemon --stacktrace
./gradlew :android:assembleRelease --no-daemon --stacktrace
```

A etapa instrumentada usa um emulador API 29/x86 com imagem padrão, snapshot e aceleração Linux desativados e opções headless para reduzir falhas de boot; o APK e o target continuam em API 35. A instrumentação é executada como verificação best-effort porque o runner hospedado pode perder o dispositivo ADB durante o boot. O APK debug é publicado como artefato por 14 dias mesmo quando a instrumentação falha depois da compilação; falhas preservam relatórios Gradle e resultados de testes quando existirem.

O workflow de debug não injeta IDs de produção nem credenciais de assinatura e publica o artefato `game-ping-pong-touch-debug-apk`. O workflow separado [`.github/workflows/android-release.yml`](../.github/workflows/android-release.yml), acionado manualmente ou por tag `v*.*.*`, restaura a upload key apenas no runner, exige os IDs e Secrets de produção, executa `:android:assembleRelease` e `:android:bundleRelease` e publica o APK em `dist/release/game-ping-pong-touch-android.apk` e o AAB em `dist/release/game-ping-pong-touch-android.aab`. Em execução manual, informe `version_name` e `version_code`; em tags, a versão é obtida da tag. Se qualquer valor obrigatório estiver ausente, o job falha antes de gerar os artefatos.

## Auto-updater e Releases

O módulo Android executa `AndroidAutoUpdater.check(this)` no launcher. A verificação é assíncrona, limitada a uma vez por dia e não bloqueia o jogo. Ao encontrar uma versão maior, o app baixa `game-ping-pong-touch-android.apk`, verifica o checksum `.sha256`, grava o arquivo no cache privado e abre o instalador por `FileProvider`.

O launcher `lwjgl3` executa o mesmo contrato para `game-ping-pong-touch-desktop.zip`. O pacote é baixado e validado em `~/.game-ping-pong/updates`; depois o sistema abre a pasta para que o usuário extraia a nova distribuição após fechar o jogo.

O módulo `release-updater` é compartilhado pelos dois launchers e também pelo app Java/AWT original. Ele usa apenas APIs Java 8, consulta a última GitHub Release pública e nunca recebe token de escrita. A publicação é feita exclusivamente pelo workflow com `GITHUB_TOKEN` e permissão `contents: write`.

## Progresso visual e teste do auto-updater

O download dos assets agora usa `release-updater` em streaming. O Android mostra `ProgressBar`, porcentagem, tamanho, velocidade, ETA e botão de cancelamento. Os dois launchers Desktop usam `JDialog`/`JProgressBar` com os mesmos dados. A transferência grava primeiro `asset.part`, remove o parcial em caso de cancelamento ou erro e valida o SHA-256 antes de concluir.

O cliente compartilhado aceita `-Dgithub.api.base=http://127.0.0.1:8787` para testes locais. O mock está em [`../.github/scripts/mock-github-release.py`](../.github/scripts/mock-github-release.py) e espera os três nomes de asset da Release. A classe `DownloadProgressTest` executa testes de porcentagem, ETA, streaming, cancelamento e checksum.

Para validar os módulos Java:

```bash
./gradlew :release-updater:test :core:test :lwjgl3:compileJava --no-daemon
```

Para validar o Android:

```bash
./gradlew :android:assembleDebug --no-daemon
```
