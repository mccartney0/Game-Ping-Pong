# Diagnóstico de corrupção visual após anúncios Android

## Sintoma

Em alguns aparelhos, depois que um anúncio de teste AdMob abre ou fecha, a superfície do jogo pode voltar com blocos, texturas embaralhadas ou formas ampliadas. A captura recebida mostra esse padrão: a tela continua com as cores gerais da arena, mas os elementos parecem compostos por retângulos grandes e sem a geometria normal.

O problema é compatível com uma combinação de troca de foco da `Activity`, composição de uma `AdView` sobre a superfície libGDX e recursos de renderização que precisam ser revalidados depois do retorno ao contexto OpenGL. Não é necessário interpretar a imagem como um problema do criativo do anúncio.

## Correções aplicadas

| Área | Correção |
|---|---|
| Recuperação GL | `AndroidLauncher.onWindowFocusChanged(true)` sinaliza a recuperação ao `PingPongTouchGame`. No próximo frame GL, o jogo recria `ShapeRenderer`, `SpriteBatch`, `BitmapFont` e `GlyphLayout`. |
| Estado GL | O renderer reaplica viewport, blend, máscaras de cor e desativa scissor, depth, cull e stencil antes de desenhar a cena. |
| Viewport | A câmera e o `FitViewport` são atualizados com as dimensões atuais depois do retorno. |
| AdMob banner | `AndroidMonetizationService` pausa e retoma o `AdView` nos callbacks `onPause()`/`onResume()` da Activity. |
| Shader/atlas | O caminho Android continua sem shader neon e sem atlas experimental por padrão; o fallback procedural é o caminho estável. |
| AWT | O jogo original em `src/pong/main` não é alterado. |

O backend Android do libGDX 1.14.2 preserva o contexto EGL no `GLSurfaceView`, invalida recursos gerenciados quando uma superfície é criada e encaminha `pause()`/`resume()` para a aplicação [1]. A recuperação explícita deste projeto é uma camada adicional defensiva para aparelhos que retornam de anúncios com estado gráfico ou composição diferente.

## Como validar no aparelho

Instale o APK debug novo e abra o menu principal. Inicie uma partida, abra uma tela que exiba banner e, se a integração estiver habilitada, abra um anúncio recompensado. Ao fechar o anúncio, aguarde pelo menos dois frames e verifique se a arena, raquetes, bola, HUD e textos continuam nítidos.

Repita o teste com rotação bloqueada em landscape, com o aparelho em modo economia de energia e após alternar rapidamente entre o anúncio e o jogo. Também teste a sequência `pausa -> anúncio -> retorno -> continuar`, pois ela verifica tanto o ciclo da Activity quanto a lógica de pausa do mundo.

Se o problema persistir, capture um `adb logcat` filtrando `AndroidGraphics`, `libGDX`, `AdMob`, `OpenGLRenderer` e `Game Ping Pong`. O dado mais útil é o renderer OpenGL informado no início da Activity e qualquer mensagem de erro imediatamente antes da imagem corrompida.

## Limitação da validação local

O sandbox de desenvolvimento não possui Android SDK local. Por isso, a validação local cobre `:core:test`, compilação desktop, playthrough headless e verificação estrutural do código; a compilação Android e o teste em emulador são executados pelo GitHub Actions. O APK final deve ser confirmado em pelo menos um aparelho físico, porque diferenças entre GPUs e backends Android podem afetar a composição de `SurfaceView` e anúncios.

## Referências

[1]: https://raw.githubusercontent.com/libgdx/libgdx/1.14.2/backends/gdx-backend-android/src/com/badlogic/gdx/backends/android/AndroidGraphics.java "libGDX 1.14.2 AndroidGraphics"

## Evidência da gravação de 22/08/2026

A gravação recebida começa com o banner de teste já visível e a área do jogo já aparece severamente pixelizada desde o primeiro frame. O vídeo não registra o fechamento do anúncio, portanto não prova que a corrupção começou no retorno de uma tela fullscreen. O anúncio permanece nítido, enquanto somente a superfície libGDX fica em baixa resolução aparente. Isso aponta para um problema de dimensionamento/composição da `SurfaceView` dentro do layout compartilhado com o `AdView`.

A correção adicional foi implementada em `AndroidLauncher`: a Activity agora usa um `LinearLayout` vertical com um `gameHost` ponderado e um `adHost` separado. O `AdView` não é mais sobreposto diretamente sobre a mesma área do jogo; quando o banner aparece, o host do jogo recebe a altura restante e o `GLSurfaceView` recebe uma medida consistente, permitindo que o `FitViewport` ajuste a câmera corretamente.
