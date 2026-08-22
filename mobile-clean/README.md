# Game Ping Pong Mobile Clean

Esta é uma reconstrução independente da versão Android do jogo. Ela existe para estabelecer uma base visual estável antes de reintroduzir os recursos da versão mobile histórica.

> **Princípio da versão Clean:** primeiro uma superfície libGDX única e procedural; depois cada recurso externo entra em um commit isolado, com APK e teste próprios.

## O que esta versão contém

A versão `mobile-clean` usa somente `core` e `android`. O núcleo desenha a arena, duas raquetes, bola, placar, menu inicial, pausa e tela de fim de partida com `ShapeRenderer`. O jogador toca ou arrasta na tela para mover a raquete inferior; o adversário é controlado por uma IA simples. O botão de pausa fica no canto superior direito e o botão físico/gestual de voltar também é tratado.

Ela **não contém** AdMob, Play Games Services, rewarded ads, áudio, atlas, texturas, shaders, `TextureRegion`, `FrameLayout` composto ou o código do módulo `libgdx-touch`. O manifesto não declara permissão de internet. Essa redução é intencional: se este APK ficar estável no aparelho, os recursos serão reintroduzidos individualmente.

O jogo original AWT em `src/pong/main` e a versão mobile anterior em `libgdx-touch` permanecem preservados. Nenhum arquivo desses módulos é necessário para compilar `mobile-clean`.

## Estrutura

| Caminho | Responsabilidade |
|---|---|
| `core/src/main/java/.../CleanPongGame.java` | Jogo procedural, input touch, estados e renderização. |
| `android/src/main/java/.../CleanAndroidLauncher.java` | Única Activity Android e configuração mínima do libGDX. |
| `android/src/main/AndroidManifest.xml` | Activity landscape sem anúncios ou permissões de rede. |
| `android/src/main/res/values/styles.xml` | Tema sem barra de ação. |
| `.github/workflows/android-mobile-clean.yml` | Build e upload do APK Clean separados do pipeline antigo. |

## Build local

O sandbox pode não possuir Android SDK local. Quando `ANDROID_HOME` ou `ANDROID_SDK_ROOT` estiver configurado, execute:

```bash
cd mobile-clean
./gradlew :core:test --no-daemon
./gradlew :android:assembleDebug --no-daemon
```

O APK será criado em:

```text
mobile-clean/android/build/outputs/apk/debug/android-debug.apk
```

Para instalar em um aparelho conectado:

```bash
adb install -r mobile-clean/android/build/outputs/apk/debug/android-debug.apk
adb shell am start -n com.mccartney0.gamepingpong.clean/.CleanAndroidLauncher
```

Se o Android informar conflito de assinatura com outra versão, remova o pacote anterior da versão Clean e instale novamente:

```bash
adb uninstall com.mccartney0.gamepingpong.clean
adb install mobile-clean/android/build/outputs/apk/debug/android-debug.apk
```

## Teste mínimo no aparelho

O teste de aceitação começa no menu e deve seguir a sequência abaixo:

| Etapa | Resultado esperado |
|---|---|
| Abrir o APK | Menu `PONG` nítido e sem blocos. |
| Tocar na tela | Partida inicia e a bola se move. |
| Arrastar horizontalmente na metade inferior | Raquete inferior acompanha o dedo sem sair da arena. |
| Tocar no canto superior direito | A partida pausa; a imagem continua nítida. |
| Tocar novamente | A partida continua. |
| Aguardar um ponto | O placar muda sem alterar escala ou viewport. |
| Interromper e retornar à Activity | O jogo continua pausado, sem perda de textura — não existem texturas nesta etapa. |
| Fazer sete pontos | Tela `FIM` aparece; tocar retorna ao menu. |

Durante a primeira validação não há anúncios. A ausência de anúncios é necessária para separar defeito do renderer de defeito de composição entre `AdView` e `GLSurfaceView`.

## Plano de reintrodução

Os recursos devem voltar nesta ordem, um por vez:

1. Persistência simples de preferências e seleção de modo.
2. Áudio curto com `Sound` e música com `Music`, sem anúncios.
3. Assets PNG estáticos sem shader, carregados em um lote próprio.
4. Poderes e power-ups usando formas procedurais antes de texturas.
5. Atlas de texturas, somente depois de confirmar que PNG individual funciona.
6. Shader neon em uma tela de diagnóstico desligada por padrão.
7. Play Games Services.
8. Banner AdMob em um build separado, com host reservado fora da superfície do jogo.
9. Rewarded Ad apenas após o banner não alterar o viewport.

Cada etapa deve ter um APK identificável e uma matriz de teste com abertura, pausa, mudança de foco, retorno e rotação. O shader e o atlas nunca devem voltar globalmente ao `SpriteBatch` sem um teste específico de GPU Android.

## CI

O workflow separado é acionado quando `mobile-clean/**` muda. Ele roda os testes do `core`, compila o APK debug e publica o artefato `game-ping-pong-mobile-clean-debug-apk`. O workflow antigo continua responsável por `libgdx-touch`.

A documentação geral de ciclo de vida do libGDX está disponível no [guia oficial de ciclo de vida](https://libgdx.com/wiki/app/the-life-cycle) e a configuração do backend Android está no [guia oficial de configuração](https://libgdx.com/wiki/app/starter-classes-and-configuration).
