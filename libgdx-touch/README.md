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
| Toque duplo na metade inferior | Ativa a habilidade do jogador 1. |
| Toque duplo na metade superior | Ativa a habilidade do jogador 2. |
| Ponteiros simultâneos | Permite controlar as duas raquetes em paralelo. |
| `pause()` do Android | Pausa o mundo automaticamente quando o app perde foco. |

O reconhecimento usa `Viewport.unproject`, portanto o gesto é convertido para coordenadas lógicas da arena e não depende da resolução física do aparelho. O toque duplo só é aceito dentro de uma janela de `0.28s` e com deslocamento máximo de `0.65` unidade de mundo. Um toque que ultrapasse `0.18` unidade vira arraste e não é contado como toque duplo.

`TouchPongWorld` mantém física determinística, placar, limite de partida, pausa, habilidades e efeitos da bola. O render usa `ShapeRenderer` e os efeitos visuais usam pools fixos de trilha e partículas para reduzir alocações por frame.

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

Keystores, senhas e aliases nunca são versionados. O módulo aceita `android/signing.properties` local, que é ignorado pelo Git, ou estas variáveis no CI:

```text
ANDROID_KEYSTORE_PATH
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

Exemplo local de `android/signing.properties` — **não commitar**:

```properties
storeFile=/caminho/seguro/game-ping-pong-upload.jks
storePassword=senha-local
keyAlias=game-ping-pong
keyPassword=senha-local
```

Para gerar um AAB assinado localmente, com as propriedades acima configuradas:

```bash
./gradlew :android:bundleRelease --no-daemon --stacktrace
```

Sem credenciais, `assembleRelease` continua compilável, mas não deve ser tratado como artefato pronto para publicação. No GitHub Actions, injete o keystore a partir de Secrets e defina os quatro valores de assinatura no ambiente do job; o workflow versionado gera apenas APK debug.

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

O playthrough `playthrough/TouchPlaythrough.java` simula 900 frames a 60 FPS, controla as duas raquetes com ponteiros independentes, dispara toque duplo nos dois lados, gera pontos, pausa e retoma o mundo e verifica que a bola permanece numericamente válida.

Os testes JUnit do core cobrem:

| Teste | Cobertura |
|---|---|
| `ServicesTest` | idempotência de unlock, IDs ausentes e fallback offline do rewarded |
| `ServicesIntegrationTest` | envio único do score final e visibilidade do banner após a partida |
| `TouchPongWorldStressTest` | simulação prolongada, limites de placar, efeitos e ausência de NaN/infinito |
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

O resultado detalhado fica em `playthrough.log`. O resultado validado nesta versão inclui `score=3:0`, duas ativações de habilidade, duas transições de pausa, trilha ativa e partículas emitidas.

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

A sequência do pipeline é:

```bash
./gradlew :core:test :core:build --no-daemon --stacktrace
./gradlew :android:assembleDebug --no-daemon --stacktrace
./gradlew :android:connectedDebugAndroidTest --no-daemon --stacktrace
```

A etapa instrumentada usa um emulador API 29/x86 com imagem padrão, snapshot e aceleração Linux desativados e opções headless para reduzir falhas de boot; o APK e o target continuam em API 35. O APK debug é publicado como artefato por 14 dias. Falhas preservam relatórios Gradle e resultados de testes quando existirem.

O workflow de debug não injeta IDs de produção nem credenciais de assinatura. O workflow separado [`.github/workflows/android-release.yml`](../.github/workflows/android-release.yml), acionado manualmente ou por tag `v*.*.*`, restaura a upload key apenas no runner, exige todos os IDs e Secrets de produção e executa `bundleRelease`; se qualquer valor obrigatório estiver ausente, o job falha antes de gerar o AAB.


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
