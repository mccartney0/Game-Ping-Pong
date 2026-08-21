# Pipeline de assets neon do Game-Ping-Pong

Este documento explica como criar um asset, seja com geração de imagem por IA, editor externo ou código procedural, transformá-lo em um arquivo utilizável pelo jogo, ajustar suas propriedades com o trainer local e publicar a alteração no GitHub. O pipeline foi desenhado para manter o `core` independente de Android, preservar o fallback procedural e impedir que um asset ausente torne a partida inutilizável.

## Arquitetura entregue

| Parte | Localização | Responsabilidade |
|---|---|---|
| Preset de desenho | `assets/config/neon_assets.json` | Nomes, paleta, tamanhos, escalas e parâmetros de desenho. |
| Preset de atlas | `assets/config/neon_pack.json` | Padding, bleed, alpha, tamanho máximo e filtro linear. |
| Trainer/editor | `libgdx-touch/tools/neon_asset_trainer.py` | Geração procedural, preview, varredura de parâmetros e validação. |
| Atalho de build | `libgdx-touch/tools/build_neon_assets.sh` | Executa geração, preview e validação em sequência. |
| Assets finais | `assets/generated/neon/{1x,2x,3x}` | PNGs RGBA usados como fonte do atlas. |
| Atlas final | `assets/generated/atlas/neon.{png,atlas}` | Página empacotada carregada pelo jogo. |
| Preview | `assets/generated/previews` | Inspeção visual rápida do pacote. |
| Shader vertex | `assets/shaders/neon_glow_scanlines.vert` | Passa posição, cor e coordenadas de textura. |
| Shader fragment | `assets/shaders/neon_glow_scanlines.frag` | Aplica glow, pulso e scanlines. |
| Catálogo Java | `core/.../NeonAssetCatalog.java` | Carrega texturas por nome usando `AssetManager`. |
| Integração | `core/.../PingPongTouchGame.java` | Configura o shader e renderiza o power-up texturizado. |

O Android empacota a pasta raiz `assets` por meio de `android/build.gradle`. O desktop compartilha os mesmos recursos pelo source set do `core`. Nenhum arquivo do pacote depende de `android.*`.

## Como criar um asset com IA

A geração por IA deve ser usada para explorar identidade visual, arenas, fundos e variações artísticas. Para ícones críticos de gameplay, a imagem final precisa ser reduzida a uma silhueta simples e verificada em tamanho pequeno. Não use texto dentro do ícone; o texto do poder continua no HUD do libGDX.

Use um pedido visual semelhante ao seguinte:

> Crie um ícone de jogo 2D para um power-up chamado SPLIT, sem texto, vista frontal, silhueta de dois núcleos circulares separados por uma linha vertical, estilo neon cyber-sport, contorno branco fino, brilho laranja controlado, centro transparente, fundo verdadeiramente transparente, composição centralizada, margem de segurança de 12%, aparência legível em 24 pixels, sem sombra sólida, sem moldura e sem elementos cortados.

Para `OVERDRIVE`, troque a silhueta por um raio ou setas de aceleração; para `SHIELD`, use um escudo; para `WIDE`, use uma raquete expandida; para `ENERGY`, um núcleo elétrico; para `SLOW`, um relógio; e para `MULTI`, uma estrela ou símbolo de multiplicação. Gere preferencialmente uma imagem sem texto e com fundo transparente real. Se o gerador entregar fundo escuro, remova-o em um editor externo e valide o alpha antes de copiar o arquivo para o projeto.

O resultado de IA não deve substituir automaticamente o preset inteiro. Primeiro escolha uma imagem aprovada, remova detalhes que prejudicam a leitura, padronize contorno e brilho e depois converta para PNG RGBA. Se a imagem tiver perspectiva, ruído ou texto ilegível, ela deve ser tratada como referência, não como asset final.

## Como criar um asset em ferramenta externa

Em um editor vetorial como Inkscape, desenhe uma forma simples com preenchimento ciano, laranja, violeta ou branco e um contorno contrastante. Mantenha o documento quadrado, centralize a silhueta e deixe pelo menos 8% a 12% de margem para o glow. Exporte o SVG como PNG RGBA em 128, 256 e 384 pixels, correspondentes às densidades 1x, 2x e 3x usadas pelo preset.

Em Blender, use Geometry Nodes para gerar variações procedurais de arenas, grades e objetos decorativos. Renderize apenas o resultado final necessário para o jogo e exporte-o como PNG com alpha ou como uma textura compatível. O Blender não deve ser uma dependência do APK; ele é uma ferramenta de autoria externa.

Para um asset desenhado por código, use Pillow ou outra biblioteca 2D. O trainer deste repositório já implementa essa abordagem para todos os ícones atuais. Em vez de alterar manualmente 27 imagens, altere o preset e regenere o pacote.

## Como instalar e executar o trainer

O único requisito Python do trainer é Pillow. No ambiente de desenvolvimento do projeto:

```bash
cd Game-Ping-Pong
python3 -m pip install --user Pillow
```

Em ambientes em que o Python gerenciado não aceita instalação de usuário, use o gerenciador de pacotes do sistema ou um ambiente virtual. O trainer não precisa de Android Studio nem de uma conta externa.

Para gerar o pacote padrão:

```bash
python3 libgdx-touch/tools/neon_asset_trainer.py generate
```

Para gerar, criar preview e validar em uma única etapa:

```bash
bash libgdx-touch/tools/build_neon_assets.sh
```

O resultado fica em:

```text
assets/generated/neon/1x/*.png
assets/generated/neon/2x/*.png
assets/generated/neon/3x/*.png
assets/generated/atlas/neon.png
assets/generated/atlas/neon.atlas
assets/generated/previews/neon_preview.png
```

Para empacotar os PNGs 1x em um atlas com padding, bleed e filtragem linear:

```bash
cd libgdx-touch
./gradlew packNeonAssets --no-daemon
```

Esse comando usa `assets/config/neon_pack.json` e grava `assets/generated/atlas/neon.png` e `assets/generated/atlas/neon.atlas`. O `NeonAssetCatalog` carrega `generated/atlas/neon.atlas` uma única vez e localiza as regiões por nome. Se o atlas estiver ausente ou falhar na leitura, ele retorna texturas nulas e o `ShapeRenderer` continua como fallback.

Para criar uma grade de treinamento e comparar rapidamente brilho e contorno:

```bash
python3 libgdx-touch/tools/neon_asset_trainer.py tune \
  --clean \
  --glow-values 4,8,12,16 \
  --stroke-values 4,6,8
```

Cada combinação é criada em `assets/generated/tuning/`. As variantes intermediárias podem ser removidas depois da escolha; elas são sempre reproduzíveis pelo comando `tune` e não precisam ser carregadas pelo jogo.

Para alterar o estilo sem editar o código, modifique `assets/config/neon_assets.json`:

```json
{
  "size": 128,
  "glow_radius": 10,
  "stroke": 6,
  "scales": [1, 2, 3]
}
```

Os nomes em `assets` são a API visual do jogo. Não renomeie `power_overdrive`, `power_shield`, `power_wide`, `power_energy`, `power_slow`, `power_split`, `power_multi`, `menu_button` ou `arena_grid` sem atualizar o catálogo Java.

## Como converter e instalar um asset externo

Suponha que um novo `power_split.png` tenha sido criado por IA ou em um editor externo. O procedimento é:

1. Recorte o asset para uma imagem quadrada e preserve toda a silhueta, sem cortar o halo.
2. Converta o modo da imagem para RGBA e remova qualquer fundo que não deva aparecer no jogo.
3. Salve a arte-mestre em uma pasta de trabalho, por exemplo `art-source/power_split_master.png`; não substitua a versão aprovada sem revisão.
4. Se o nome for um asset já conhecido pelo jogo, coloque o arquivo aprovado no preset/procedimento de geração ou substitua o PNG em `assets/generated/neon/1x/` e gere as escalas correspondentes.
5. Para manter 2x e 3x consistentes, prefira alterar a fonte do trainer e executar `generate`; não redimensione manualmente cada cópia.
6. Execute `validate` e abra `neon_preview.png` para verificar alpha, margem, contraste e silhueta.
7. Compile o core e o desktop antes de publicar.

A versão atual do jogo usa `NeonAssetCatalog` para carregar o PNG do power-up por nome. Se a textura não existir ou falhar, o mundo continua desenhando o power-up proceduralmente com `ShapeRenderer`, portanto a integração é segura durante a criação incremental.

## Uso do shader GLSL

O fragment shader está em `assets/shaders/neon_glow_scanlines.frag`. Ele recebe a textura do `SpriteBatch`, calcula a maior alpha em oito amostras ao redor do pixel, transforma a diferença entre a alpha vizinha e a alpha original em halo, aplica um pulso temporal e reduz levemente linhas alternadas da tela.

Os uniforms usados são:

| Uniform | Função | Valor típico |
|---|---|---|
| `u_time` | Pulso animado do brilho | Tempo acumulado em segundos. |
| `u_resolution` | Escala das scanlines em pixels | Largura e altura da tela. |
| `u_texelSize` | Passo das amostras de glow | Aproximadamente `1/256` por eixo para os ícones atuais. |
| `u_glowStrength` | Intensidade do halo | `0.55` baixa, `0.95` média, `1.35` alta. |
| `u_glowRadius` | Distância da amostragem | `2` média, `3` alta. |
| `u_scanlineStrength` | Intensidade das linhas | `0` baixa, `0.35` média, `0.65` alta. |
| `u_tint` | Cor adicional do glow | Ciano padrão `(0.25, 0.85, 1.0, 1.0)`. |

O shader é compilado no `create()`. Em caso de erro de leitura ou compilação, o jogo registra o problema e usa o shader padrão do `SpriteBatch`. O `ShapeRenderer` do mundo continua sendo a camada de gameplay e fallback.

## Build local e Android

Depois de alterar assets ou shader, execute:

```bash
cd libgdx-touch
./gradlew :core:test :lwjgl3:compileJava --no-daemon --stacktrace
```

Para gerar o APK debug:

```bash
./gradlew :android:assembleDebug --no-daemon
```

O APK costuma aparecer em `android/build/outputs/apk/debug/`. Para gerar AAB release, configure o `signing.properties` local ou as variáveis de assinatura esperadas pelo workflow e execute:

```bash
./gradlew :android:bundleRelease --no-daemon
```

Keystores, senhas e IDs de produção nunca devem entrar no Git. O CI Android já empacota a pasta `assets` definida em `android/build.gradle`; não copie os PNGs para `android/src/main/assets` manualmente.

## Publicação no GitHub

Antes de publicar uma mudança visual, rode o trainer, a validação, os testes e o playthrough. Na raiz do repositório:

```bash
python3 libgdx-touch/tools/neon_asset_trainer.py generate
python3 libgdx-touch/tools/neon_asset_trainer.py preview
python3 libgdx-touch/tools/neon_asset_trainer.py validate
cd libgdx-touch
./gradlew :core:test :lwjgl3:compileJava --no-daemon
cd ..
```

Depois revise o diff para garantir que `src/pong/main/` não foi alterado. O conjunto mínimo de arquivos a publicar é o preset, o trainer, os shaders, o catálogo Java, o código de integração, as imagens finais e esta documentação. Use uma mensagem de commit que descreva a mudança, por exemplo:

```bash
git add assets libgdx-touch/core libgdx-touch/tools libgdx-touch/README.md \
  libgdx-touch/ASSETS_MOBILE_PLAN.md libgdx-touch/NEON_ASSET_PIPELINE.md
git commit -m "Adiciona pipeline procedural de assets neon"
git push origin master
```

Após o push, aguarde o workflow Android e baixe o artefato apenas do commit correto. A build local valida Java e recursos; o workflow valida o empacotamento final do APK.

## Critérios de aceite

Um asset está pronto quando permanece reconhecível em aproximadamente 24 pixels, possui alpha limpo, não corta o halo, não depende somente da cor, passa no `validate`, aparece no preview, é carregado pelo catálogo ou usa o fallback, e não aumenta o custo de renderização de forma desnecessária. A mudança só deve ser publicada quando o jogo AWT continuar intacto e o `core` continuar sem imports Android.

## Referências

[1]: https://libgdx.com/wiki/tools/texture-packer "Documentação oficial do TexturePacker do libGDX"

[2]: https://libgdx.com/wiki/graphics/opengl-utils/shaders "Documentação oficial de shaders do libGDX"

[3]: https://docs.blender.org/manual/en/latest/modeling/geometry_nodes/introduction.html "Manual oficial do Blender Geometry Nodes"

[4]: https://pillow.readthedocs.io/en/latest/reference/ImageDraw.html "Documentação oficial do Pillow ImageDraw"
