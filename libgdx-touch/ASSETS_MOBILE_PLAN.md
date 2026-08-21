# Plano de melhoria dos assets mobile

## Direção visual

O jogo deve assumir uma identidade **neon cyber-sport**: fundo azul-marinho quase preto, energia ciano para o jogador inferior, laranja para o jogador superior e amarelo para eventos raros. A regra principal é que cada elemento seja reconhecido pela silhueta antes mesmo de o jogador ler o HUD.

| Elemento | Direção recomendada | Regra de leitura |
|---|---|---|
| Raquete do jogador 1 | Ciano, forma larga com núcleo branco e brilho azul | Silhueta horizontal simples |
| Raquete do jogador 2 | Laranja, mesma forma com núcleo branco e brilho âmbar | Mesma geometria, cor oposta |
| Bola | Núcleo branco, halo ciano e trilha dependente da velocidade | Deve continuar visível sobre qualquer arena |
| `OVERDRIVE` | Ícone de raio/setas para frente, ciano-amarelo | Movimento e aceleração |
| `SHIELD` | Ícone de escudo hexagonal, azul-violeta | Defesa e bloqueio |
| `WIDE` | Ícone de raquete expandida, verde-lima | Aumento de área |
| `ENERGY` | Núcleo elétrico, amarelo | Recurso recuperável |
| `SLOW` | Relógio/anel congelado, azul | Redução de velocidade |
| `SPLIT` | Dois círculos orbitais, magenta | Eco visual da bola |
| `MULTI` | Dois símbolos de pontuação sobrepostos, dourado | Próximo ponto multiplicado |

## Como melhorar os assets na prática

A primeira melhoria deve substituir a representação puramente geométrica dos power-ups por ícones com **silhueta, halo e animação de pulso**. Os ícones devem ser produzidos sem texto, com fundo transparente e uma margem de segurança de pelo menos 8% para que o brilho não seja cortado no atlas. A cor não deve ser o único indicador: a forma e o padrão interno precisam diferenciar `ENERGY`, `SLOW`, `SPLIT` e `MULTI` para jogadores com daltonismo ou brilho reduzido.

A segunda melhoria é criar um pequeno conjunto de arenas com identidade própria. A arena clássica pode usar linhas horizontais e grade discreta; a Arena Mutante deve acrescentar obstáculos, portais e zonas de velocidade com silhuetas geométricas consistentes. Cada arena precisa ter um fundo silencioso, sem textura de alto contraste atrás da trajetória da bola. Os efeitos devem ser camadas separadas para permitir reduzir a qualidade em aparelhos antigos.

A terceira melhoria é trocar as fontes padrão e os textos longos do HUD por uma hierarquia visual curta: placar grande, energia em barra, nome do poder em uma cápsula e eventos em uma faixa temporária. O texto continua sendo desenhado pelo libGDX para manter legibilidade e localização; os assets gerados devem cuidar apenas de ícones, fundos, partículas e elementos decorativos.

## Pipeline recomendado

1. Defina uma folha de estilo com paleta, espessura de contorno, raio de cantos, intensidade de brilho e perspectiva. Não gere cada asset com um prompt visual diferente.
2. Gere primeiro um **painel de referência** com a arena, as duas raquetes, a bola e os quatro power-ups. Use-o para fixar proporções e cores antes de gerar variações individuais.
3. Produza cada ícone como PNG RGBA transparente, em resolução mestre de 256 ou 512 pixels, e exporte versões 64, 96 e 128 pixels conforme a densidade do aparelho. Não aplique texto dentro dos ícones.
4. Monte um `TextureAtlas` por família: `gameplay.atlas` para bola/raquetes/power-ups, `arenas.atlas` para fundos e elementos de arena e `ui.atlas` para botões e indicadores. Evite carregar dezenas de texturas individuais durante a partida.
5. Use filtragem linear para assets vetoriais/rasterizados limpos, mantenha o premultiplied alpha consistente e teste o atlas em aparelhos de baixa resolução. O fundo e as partículas devem ter alternativas `LOW`, `MEDIUM` e `HIGH`.
6. Valide cada asset em três condições: arena escura, arena clara e tela pequena. O ícone só deve ser aceito se sua silhueta continuar reconhecível em aproximadamente 24 pixels.

## Integração futura no código

Os assets podem entrar em `libgdx-touch/core/assets/` sem qualquer import Android. O core deve receber um pequeno `AssetCatalog` ou `TextureRegion` provider; o módulo Android apenas empacota os arquivos. A lógica de gameplay não deve depender do asset: se o carregamento falhar, o renderer atual com `ShapeRenderer` continua como fallback.

Para preservar desempenho, carregue atlas no `create()`, reutilize `TextureRegion` e descarte o `AssetManager` em `dispose()`. Não crie `Texture`, `Sprite` ou `BitmapFont` dentro de `render()`. O brilho pode continuar sendo produzido por `BallEffects`, enquanto os novos ícones fornecem apenas a camada de identidade visual.

## Critérios de aceite

| Critério | Aceite |
|---|---|
| Identidade | Jogador inferior ciano e jogador superior laranja são reconhecíveis sem texto |
| Poderes | Os três poderes têm silhuetas diferentes e não dependem apenas da cor |
| Power-ups | Os quatro coletáveis são distinguíveis em 24 px |
| Performance | Nenhuma textura é criada no loop de render; atlas é carregado uma vez |
| Acessibilidade | Cada efeito crítico tem forma, cor e texto/estado no HUD |
| Fallback | O jogo continua jogável se um atlas não carregar |
| CI | Assets não contêm segredos, keystores ou referências Android no core |

A geração de visuais deve ser feita como um **asset pack coerente**, não como imagens isoladas. Para cada novo boss ou arena, mantenha os mesmos contornos, contraste e linguagem de efeitos; a novidade deve vir da silhueta e da mecânica, não de trocar completamente o estilo.

## Implementação publicada

A primeira versão automatizada está disponível no repositório em `libgdx-touch/tools/neon_asset_trainer.py`, com o preset `assets/config/neon_assets.json`. O comando `generate` produz PNGs RGBA nas escalas 1x, 2x e 3x; `preview` cria uma folha de inspeção; `tune` gera uma grade de combinações de brilho e contorno; e `validate` verifica modo RGBA, dimensões e alpha não vazio. O atalho `libgdx-touch/tools/build_neon_assets.sh` executa geração, preview e validação em sequência.

Os shaders `assets/shaders/neon_glow_scanlines.vert` e `assets/shaders/neon_glow_scanlines.frag` fornecem glow, pulso e scanlines para o `SpriteBatch`. O shader é carregado de forma segura pelo `PingPongTouchGame`; se a leitura ou compilação falhar, o render padrão continua funcionando. `NeonAssetCatalog` carrega os assets do caminho lógico `generated/neon/1x/`, e o power-up ativo usa seu PNG neon quando disponível, preservando o círculo procedural em caso de ausência.

O caminho Android está configurado em `android/build.gradle` para empacotar a pasta raiz `assets`; desktop e testes continuam compartilhando o mesmo conjunto de arquivos pelo source set do `core`. O fluxo detalhado de criação com IA ou editor externo, conversão para PNG RGBA, regeneração, validação, build local e publicação está em `NEON_ASSET_PIPELINE.md`.
