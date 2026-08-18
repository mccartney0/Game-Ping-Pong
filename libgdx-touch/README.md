# Game Ping Pong — camada libGDX de toque

Esta pasta contém uma adaptação incremental do jogo AWT para libGDX. O projeto AWT original permanece intacto; o módulo adiciona um `core` libGDX com controle multitouch para as raquetes, launcher Android, launcher LWJGL3 e um playthrough automatizado.

## Gestos

| Gesto | Resultado |
|---|---|
| Arrastar na metade inferior | Move a raquete do jogador 1. |
| Arrastar na metade superior | Move a raquete do jogador 2 ou o adversário de teste. |
| Toque duplo na metade inferior | Ativa a habilidade do jogador 1. |
| Toque duplo na metade superior | Ativa a habilidade do jogador 2. |
| Ponteiros simultâneos | Permite controlar as duas raquetes em paralelo. |
| `pause()` do Android | Pausa o mundo automaticamente quando o app perde foco. |

O reconhecimento usa `Viewport.unproject`, portanto o gesto é convertido para coordenadas lógicas da arena e não depende da resolução física do aparelho. O toque duplo só é aceito dentro de uma janela de `0.28s` e com deslocamento máximo de `0.65` unidade de mundo. Um toque que ultrapasse `0.18` unidade vira arraste e não é contado como toque duplo.

## Estrutura

```text
libgdx-touch/
├── core/       lógica do mundo e entrada por toque
├── android/    AndroidLauncher e configuração do APK
├── lwjgl3/     teste desktop com mouse
└── playthrough/TouchPlaythrough.java
```

A classe `PaddleTouchInput` implementa `InputAdapter` e delega ações para `PaddleTouchTarget`. O mundo de exemplo, `TouchPongWorld`, contém as duas raquetes, bola, placar, habilidade e pausa. Na migração definitiva, substitua o corpo de `TouchPongWorld` pelo `GameWorld` extraído do projeto atual, mantendo o contrato `PaddleTouchTarget`.

## Execução do Android

Abra `libgdx-touch` como projeto Gradle/Android Studio após configurar o Android SDK. Os comandos esperados são:

```bash
./gradlew :lwjgl3:run
./gradlew :android:assembleDebug
adb install -r android/build/outputs/apk/debug/android-debug.apk
```

O Android está fixado em paisagem, com modo imersivo, sem acelerômetro e sem bússola. Para o APK, é necessário ter o Android SDK e o Gradle wrapper configurados; o sandbox desta tarefa validou o módulo core diretamente com o jar libGDX 1.14.2.

## Playthrough validado

O roteiro `playthrough/TouchPlaythrough.java` simula 900 frames a 60 FPS, controla as duas raquetes com ponteiros independentes, dispara um toque duplo nas duas raquetes, gera pontos, pausa e retoma o mundo e verifica que a bola permanece numericamente válida.

Resultado atual:

```text
touch playthrough: OK
score=3:0
abilityActivations=2
pauseToggles=2
drags=898/898
```

Para reproduzir no sandbox depois de baixar `gdx-1.14.2.jar`:

```bash
mkdir -p /tmp/libgdx-touch-build
javac -cp /tmp/gdx-1.14.2.jar \
  -d /tmp/libgdx-touch-build \
  $(find core/src/main/java -name '*.java')
javac -cp /tmp/gdx-1.14.2.jar:/tmp/libgdx-touch-build \
  -d /tmp/libgdx-touch-build playthrough/TouchPlaythrough.java
java -Djava.awt.headless=true \
  -cp /tmp/gdx-1.14.2.jar:/tmp/libgdx-touch-build \
  TouchPlaythrough
```

O resultado detalhado fica em `playthrough.log`.

## Efeitos visuais da bola

`BallEffects` mantém pools fixos de pontos de trilha e partículas, evitando criar objetos durante cada frame. A trilha acompanha a posição e a velocidade da bola; partículas são emitidas em colisões com paredes e raquetes, ativações de habilidade e pontos. O renderer habilita alpha blending apenas durante a camada visual.

A qualidade pode ser ajustada em três níveis:

```java
world.setEffectsQuality(BallEffects.Quality.LOW);
world.setEffectsQuality(BallEffects.Quality.MEDIUM);
world.setEffectsQuality(BallEffects.Quality.HIGH);
```

`LOW`, `MEDIUM` e `HIGH` controlam a quantidade máxima de pontos de trilha e partículas. Para economizar bateria em aparelhos antigos, use `LOW`, desligue os efeitos em menus/pausa e mantenha o `ShapeRenderer` fora de loops de criação de objetos.

## GitHub Actions

O arquivo `.github/workflows/android-apk.yml` executa quando há alterações em `libgdx-touch/` ou manualmente por `workflow_dispatch`. O job instala Java 17, Android SDK/API 35, Gradle 8.2, executa:

```bash
gradle :core:build :android:assembleDebug --no-daemon --stacktrace
```

Depois, publica `android/build/outputs/apk/debug/android-debug.apk` como artefato por 14 dias. Esse pipeline gera um APK debug não assinado para testes. Para release, adicione um keystore armazenado em GitHub Secrets, configure `signingConfigs` no módulo Android e troque a tarefa para `assembleRelease`.

## Google Play Games Services

O módulo Android inclui `AndroidGameServices` com PGS v2 para autenticação, envio de score no fim da partida e abertura da UI oficial de leaderboards. O `core` usa `GameServices` e `NoopGameServices`, portanto o launcher desktop continua offline.

Antes de executar no Android, substitua o project ID e os IDs reais dos leaderboards em `android/src/main/res/values/strings.xml`. O passo a passo da Play Console, configuração de testadores e primeiro AAB está em [`PGS_PLAY_CONSOLE_SETUP.md`](PGS_PLAY_CONSOLE_SETUP.md).
