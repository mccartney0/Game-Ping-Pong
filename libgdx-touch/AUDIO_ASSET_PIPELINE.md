# Pipeline de áudio do Game-Ping-Pong

Este documento descreve o pacote de áudio inicial e extensível do jogo. A organização separa trilhas longas, efeitos curtos, catálogo e ferramentas de geração para que novas arenas, bosses, desafios e telas possam receber áudio sem espalhar caminhos de arquivo pela lógica de gameplay.

## Pacote entregue

| Família | Localização | Conteúdo |
|---|---|---|
| Música de menu | `assets/audio/music/menu_neon_loop.ogg` | Trilha convidativa, futurista e de baixa densidade para o menu principal. |
| Música de gameplay | `assets/audio/music/gameplay_neon_loop.ogg` | Groove competitivo para Classic, Survival, Turbo e Versus. |
| Música Mutante | `assets/audio/music/mutant_arena_loop.ogg` | Textura instável para Arena Mutante e desafios. |
| Música de campanha | `assets/audio/boss/campaign_boss_loop.ogg` | Trilha tensa para os quatro bosses atuais. |
| Efeitos de UI | `assets/audio/sfx/ui` | Toque, confirmação, voltar, pausa, erro e contagem regressiva. |
| Efeitos de gameplay | `assets/audio/sfx/gameplay` | Raquete, parede e ponto. |
| Efeitos de power-up | `assets/audio/sfx/powerups` | Spawn, coleta, Energy, Slow, Split e Multi. |
| Efeitos de habilidade | `assets/audio/sfx/abilities` | Overdrive, Shield, Wide e energia insuficiente. |
| Efeitos de boss | `assets/audio/sfx/boss` | Alerta e mudança de fase. |
| Efeitos de resultado | `assets/audio/sfx/results` | Vitória e derrota. |
| Catálogos | `assets/audio/metadata` | Manifestos JSON com nomes, caminhos, categorias, volumes e slots futuros. |
| Gerador de SFX | `libgdx-touch/tools/audio_asset_generator.py` | Síntese determinística de 25 WAVs curtos, sem dependência de Android. |

As quatro trilhas possuem aproximadamente 87 a 119 segundos e foram convertidas para OGG/Vorbis com extensão coerente. Os 25 efeitos são WAV PCM de 16 bits, mono, 44,1 kHz e curtos, apropriados para carregamento como `Sound`. A duração exata e o caminho de cada SFX estão em `assets/audio/metadata/sfx_catalog.json`.

## Integração Java

`AudioAssetCatalog` fica no módulo `core` e usa apenas APIs multiplataforma do libGDX: `AssetManager`, `Music` e `Sound`. Nenhum arquivo do catálogo importa `android.*`.

O catálogo define `MusicTrack` para `MENU`, `GAMEPLAY`, `MUTANT` e `CAMPAIGN`, e define `Cue` para os efeitos. O método `trackFor(MobileGameMode)` faz a associação inicial:

| Modo | Trilha atual | Próxima evolução possível |
|---|---|---|
| `CLASSIC` | `GAMEPLAY` | Uma variação com menos camadas para partidas longas. |
| `SURVIVAL` | `GAMEPLAY` | Camada de tensão após perder uma vida. |
| `TURBO` | `GAMEPLAY` | Versão com BPM maior ou camada de hi-hat. |
| `VERSUS` | `GAMEPLAY` | Tema alternado por jogador ou placar. |
| `MUTANT` | `MUTANT` | Variação por regra mutante da arena. |
| `CAMPAIGN` | `CAMPAIGN` | Variações dedicadas para VOLT, MIRROR, TWIN e GRAVITY. |

`PingPongTouchGame` carrega o catálogo durante `create()`, inicia a música de menu, troca a música ao iniciar um modo, toca cues de confirmação/pausa/transição e mapeia `world.getLastEvent()` para efeitos de hit, ponto, poder e coleta. O evento é deduplicado para impedir que o mesmo som seja disparado continuamente em todos os frames. O desligamento do áudio chama `dispose()` junto com os demais recursos.

O mapeamento distingue `POWER ENERGY` como spawn e `PLAYER ENERGY`/`TOP ENERGY` como coleta; também diferencia `WALL HIT`, `PLAYER HIT`, `TOP HIT`, `BOSS HIT`, `SHIELD BREAK` e `ENERGY LOW`. Isso evita tocar o som de coleta no momento errado e mantém espaço para cues dedicados dos bosses.

Se o carregamento falhar, o catálogo entra em modo silencioso e o jogo continua funcionando. Isso é importante para builds incompletas, testes headless e aparelhos em que um formato ou backend não esteja disponível.

## Execução do gerador de efeitos

O gerador usa uma seed fixa e síntese simples de ondas, sweeps, ruído filtrado, envelopes e normalização. Ele não chama rede e não exige Android Studio. Para regenerar todos os efeitos:

```bash
cd Game-Ping-Pong
python3 libgdx-touch/tools/audio_asset_generator.py
```

Os arquivos serão recriados em `assets/audio/sfx`, e o manifesto será atualizado em `assets/audio/metadata/sfx_catalog.json`. Para validar os arquivos rapidamente, use o script incluído:

```bash
bash libgdx-touch/tools/validate_audio_assets.sh
```

O script verifica que as músicas são OGG/Vorbis e que os efeitos são PCM 16-bit mono em 44,1 kHz. Para inspecionar arquivos individualmente:

```bash
find assets/audio/sfx -type f -name '*.wav' -print | sort
for file in assets/audio/sfx/**/*.wav; do
  ffprobe -v error -show_entries stream=codec_name,sample_rate,channels \
    -of default=noprint_wrappers=1 "$file"
done
```

O segundo comando usa globstar em alguns shells; se ele não funcionar, use `find` com `-exec ffprobe` ou verifique arquivos individuais. O critério esperado é `pcm_s16le`, `44100` e `1` canal.

## Como substituir ou adicionar um efeito externo

Para importar um efeito criado em Audacity, REAPER, LMMS, Blender, um sintetizador ou outra ferramenta, normalize a fonte para uma duração curta, remova silêncio excessivo, ajuste o pico para evitar clipping e exporte em WAV PCM 16-bit mono a 44,1 kHz. Use nomes minúsculos com sublinhado, por exemplo `boss_gravity_phase.wav`.

Depois, copie o arquivo para a categoria correta, adicione sua entrada ao manifesto e crie um item correspondente no enum `AudioAssetCatalog.Cue`. O caminho no Java deve começar em `audio/`, pois a raiz lógica do libGDX é a pasta `assets`:

```text
arquivo local: assets/audio/sfx/boss/boss_gravity_phase.wav
caminho no libGDX: audio/sfx/boss/boss_gravity_phase.wav
```

Para música externa, prefira OGG/Vorbis para manter o APK menor. A música precisa de um ponto inicial e final compatíveis para loop ou de uma cauda curta que permita crossfade. Coloque-a em `assets/audio/music` ou `assets/audio/boss`, adicione-a a `MusicTrack` e atualize `audio_catalog.json`.

## Como criar áudio com IA

Para música, descreva explicitamente duração, BPM, tonalidade, instrumentação, densidade, atmosfera, qualidade e a exigência `Instrumental only, no vocals`. Peça material original, sem imitação de artista ou melodia reconhecível, e solicite que o início e o fim tenham estado harmônico compatível para facilitar o loop.

Um prompt adequado para uma nova variação de boss é:

> Instrumental only, no vocals. Create an original 90-second boss phase loop for a neon cyber-sport arcade game at 132 BPM in F-sharp minor. Use tense electronic percussion, controlled sub bass pulses, metallic synth accents, a sparse five-note tension motif, and a clean wide mix with space for gameplay sound effects. Keep the first and last 6 seconds harmonically compatible for a crossfade loop. Avoid recognizable melodies, vocals, spoken words, harsh clipping, abrupt silence, and artist imitation. Entirely original musical material.

Depois de receber o arquivo, confirme o codec com `ffprobe`. Se ele tiver extensão `.wav` mas o codec for MP3 ou outro formato, não o coloque diretamente no APK. Converta-o para OGG ou para um WAV real com `ffmpeg`, mantendo a extensão correspondente.

Para efeitos one-shot criados com IA, prefira exportar ou converter para WAV curto. O efeito deve conter uma ideia sonora simples e sem cauda exagerada: um clique, impacto, sweep, pulso, alarme ou confirmação. A limpeza, normalização e validação continuam obrigatórias mesmo quando a fonte foi gerada por IA.

## Mixagem e regras de reprodução

Efeitos críticos devem ter volume moderado e duração curta, porque colisões podem ocorrer repetidamente. Não use um som longo em cada frame. Eventos contínuos, como energia baixa ou uma habilidade ativa, devem usar no máximo um loop controlado ou uma atualização com cooldown.

A música é controlada como `Music` e deve tocar em loop com volume entre 0,30 e 0,45 no catálogo inicial. Os SFX são controlados como `Sound` e recebem volumes individuais. O Android usa `SoundPool` para `Sound` e `MediaPlayer` para `Music`; a documentação oficial do libGDX alerta que latência, reprodução simultânea e diferenças entre dispositivos podem afetar o resultado [1]. Por isso, a implementação inicial evita disparos por frame, mantém os efeitos curtos e deixa o áudio silencioso como fallback.

Se o jogo evoluir para baixa latência rigorosa, muitos sons simultâneos ou trilha mais complexa, avalie `AsynchronousAndroidAudio`, libGDX Oboe ou gdx-miniaudio. Essas alternativas também são citadas na documentação oficial do libGDX [1], mas devem ser introduzidas separadamente e validadas no CI Android.

## Build e empacotamento

O módulo Android já empacota a pasta raiz `assets` por meio do `sourceSets.main.assets.srcDirs` em `android/build.gradle`. Não copie os arquivos para outro diretório e não coloque áudios em `src/pong/main`, que pertence ao jogo AWT original.

Após qualquer alteração:

```bash
cd Game-Ping-Pong
python3 libgdx-touch/tools/audio_asset_generator.py
cd libgdx-touch
./gradlew :core:test :lwjgl3:compileJava --no-daemon
./gradlew :android:assembleDebug --no-daemon
```

A build Android exige um SDK local configurado ou o workflow do GitHub Actions. O APK debug deve ser testado em um aparelho real com volume baixo inicialmente, verificando menu, contagem, pausa, colisões, power-ups, poderes, boss, vitória e derrota.

## Catálogo de futuras expansões

O arquivo `assets/audio/metadata/audio_catalog.json` já reserva slots para `daily_challenge`, `weekly_event`, `boss_volt`, `boss_mirror`, `boss_twin` e `boss_gravity`. Esses slots permitem adicionar áudio sem renomear os arquivos atuais.

As próximas melhorias recomendadas são crossfade entre menu e partida, ducking temporário da música quando um stinger de boss toca, variações por combo, feedback estéreo leve para o lado da raquete, volume separado de música/SFX, opção de mute persistida em preferências e eventos de áudio específicos para arenas criadas pelo editor.

## Referências

[1]: https://libgdx.com/wiki/audio/audio "Documentação oficial de áudio do libGDX"
