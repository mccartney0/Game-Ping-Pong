# Pesquisa de ferramentas para assets neon

## libGDX TexturePacker

A documentação oficial do libGDX descreve o TexturePacker como um empacotador que reúne várias imagens pequenas em páginas maiores e grava um arquivo `.atlas` com as posições. O `TextureAtlas` carrega o atlas e permite localizar regiões por nome, criar sprites e patches. Fonte: https://libgdx.com/wiki/tools/texture-packer

Aplicação no projeto: gerar um atlas com `power_overdrive`, `power_shield`, `power_wide`, `power_energy`, `power_slow`, `power_split`, `power_multi`, elementos da arena e estados do menu. O atlas deve ser carregado uma vez e reutilizado para reduzir trocas de textura.

## libGDX ShaderProgram

A documentação oficial mostra que `ShaderProgram` combina vertex shader e fragment shader, podendo carregar GLSL de arquivos ou de strings. Recomenda verificar `isCompiled()` e consultar `getLog()` em caso de erro. Os atributos padrão do pipeline incluem `a_position`, `a_color` e `a_texCoord0`. Fonte: https://libgdx.com/wiki/graphics/opengl-utils/shaders

Aplicação no projeto: usar um fragment shader simples para brilho, pulso, scanlines e vinheta em elementos neon; manter o `ShapeRenderer` como fallback e reduzir a qualidade dos efeitos em aparelhos fracos.

## Direção prática

Para ícones pequenos e legíveis, a rota preferencial é SVG ou PNG vetorial/limpo criado por script ou editor, seguido por rasterização em múltiplas escalas e empacotamento com TexturePacker. Para brilho e movimento, a rota preferencial é shader e partículas em runtime, não imagens enormes pré-renderizadas. Para variações de arena, Blender Geometry Nodes ou scripts Python podem gerar padrões e exportar sprites, mas não são necessários para os primeiros ícones 2D do jogo.

## Blender Geometry Nodes

A documentação do Blender define Geometry Nodes como um sistema de modificação de geometria por operações baseadas em nós, aplicado por um Geometry Nodes Modifier e organizado em Node Groups. Ele pode trabalhar com meshes, curves, point clouds, volumes e instances. Fonte: https://docs.blender.org/manual/en/latest/modeling/geometry_nodes/introduction.html

Aplicação no projeto: usar Blender para gerar variações de arenas ou objetos 3D exportados como imagens, não como dependência do APK. O arquivo exportado deve entrar no repositório apenas como PNG/SVG final ou como fonte de arte versionada.

## Pillow ImageDraw

A documentação do Pillow apresenta `ImageDraw` como uma API para desenhar gráficos 2D em objetos `Image`, com suporte a modos RGB/RGBA e máscaras de transparência. Fonte: https://pillow.readthedocs.io/en/latest/reference/ImageDraw.html

Aplicação no projeto: criar um script determinístico para desenhar silhuetas dos poderes, halos, bordas e estados dos botões em RGBA; salvar PNGs em 1x, 2x e 3x; e então empacotar as imagens com o TexturePacker do libGDX.
