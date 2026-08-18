# Neon Ping Pong

Uma versão expandida do Pong em Java/AWT com estética neon, modos de jogo, habilidades e arena dinâmica.

## Execução

O projeto é compatível com Java 13 ou superior e usa apenas bibliotecas da JDK. Pela IDE Eclipse, execute `pong.main.Game`. Pela linha de comando:

```bash
javac -d bin $(find src -name '*.java' | sort)
java -cp bin pong.main.Game
```

As estatísticas são salvas localmente em `pong-stats.properties`, que não é versionado.

## Modos

| Modo | Regra |
|---|---|
| Clássico | Primeiro jogador a alcançar a meta vence. |
| Sobrevivência | O jogador começa com três vidas; a arena evolui e surge um chefe após três pontos. |
| Turbo | Partida curta, com bola mais veloz e meta reduzida. |
| Versus local | Dois jogadores na mesma máquina; Jogador 1 usa `A/D` ou setas e Jogador 2 usa `J/L`. |

## Controles

`SPACE` ativa a habilidade do Jogador 1; `Q` troca entre Overdrive, Escudo e Raquete XL. No versus, o Jogador 2 usa `I` para ativar e `O` para trocar. `ESC` ou `P` pausa, `M` volta ao menu e `F11` alterna tela cheia.

## Sistemas de jogo

A partida conta com energia, combos, recorde, power-ups de energia, slow, split e multiplicador, obstáculos móveis, zonas turbo, gravidade zero, desafios por sequência, progressão local por XP, estatísticas, sons procedurais, partículas, trilha da bola, tremor de impacto e mensagens contextuais.

## Arena Mutante

No menu, escolha `EDITOR DE ARENA`. Use as setas para mover o cursor, `TAB` para trocar o elemento, `SPACE` para colocar, `X` para remover, `BACKSPACE` para limpar e `ENTER` para salvar. Os elementos disponíveis são bloco, turbo, slow, portal e gravidade. A arena salva é aplicada ao modo `ARENA MUTANTE` e pode ser compartilhada pelo código gerado no modelo `ArenaBlueprint`.

## Skins e itens

A opção `SKINS E ITENS` permite trocar skins de raquete, bola, arena e título. Os itens são liberados por XP e o equipamento é persistido em `pong-stats.properties`. As skins alteram cores, halos, gradientes e a identidade visual da partida.

## Campanha de bosses

A `CAMPANHA BOSS` apresenta quatro chefes: Volt, Mirror, Twin e Gravity. Cada chefe altera o alvo da IA ou a trajetória da bola de forma diferente. O jogador possui três vidas e precisa derrotar cada chefe por impactos sucessivos; derrotas e XP são registradas no save local.


## Atualização automática via GitHub Releases

Os três produtos distribuíveis consultam a última Release pública de `mccartney0/Game-Ping-Pong` em segundo plano, com intervalo controlado no Android. O cliente compartilhado compara `tag_name` semanticamente, localiza o asset específico da plataforma e valida o SHA-256 antes de aceitar o arquivo. A consulta usa o endpoint oficial `GET /repos/{owner}/{repo}/releases/latest`, que retorna a última Release publicada não-prerelease.[^1]

| Produto | Asset da Release | Comportamento |
|---|---|---|
| Java/AWT original | `neon-ping-pong-awt.jar` | Pergunta, baixa, valida e abre a pasta de atualização; o JAR atual é substituído depois de fechar o processo. |
| libGDX Android | `game-ping-pong-touch-android.apk` | Pergunta, baixa para cache privado, valida SHA-256 e abre o instalador Android por `FileProvider`. |
| libGDX desktop | `game-ping-pong-touch-desktop.zip` | Pergunta, baixa, valida e abre a pasta para extração após o encerramento do jogo. |

O workflow [`.github/workflows/android-release.yml`](.github/workflows/android-release.yml) é acionado por tags `vX.Y.Z` ou manualmente. Ele compila os testes do core, o APK/AAB Android release, o ZIP desktop libGDX e o JAR AWT; publica os binários, checksums e `release-manifest.json` na mesma GitHub Release. O workflow precisa de `contents: write` para publicar assets usando `GITHUB_TOKEN`, seguindo o princípio de permissões mínimas recomendado pela documentação do Actions.[^2]

Para publicar uma versão, faça push de uma tag sem alterar o nome dos assets:

```bash
git tag v1.2.3
git push origin v1.2.3
```

O APK de atualização deve ser assinado com a mesma chave usada na instalação anterior; por isso, o workflow exige `ANDROID_KEYSTORE_BASE64`, as quatro credenciais de assinatura e os valores de produção do Android antes de gerar a Release. A instalação de APK é encaminhada ao sistema Android por uma URI `content://` concedida pelo `FileProvider`, em vez de expor um caminho privado de arquivo.

[^1]: [GitHub REST API — Get the latest release](https://docs.github.com/en/rest/releases/releases#get-the-latest-release)
[^2]: [GitHub Actions — Use GITHUB_TOKEN for authentication in workflows](https://docs.github.com/en/actions/security-for-github-actions/security-guides/automatic-token-authentication)

## Progresso visual do auto-updater

O download dos três aplicativos agora é feito por streaming, usando blocos de 16 KiB, arquivo temporário `.part`, progresso por bytes, velocidade aproximada, ETA e cancelamento. O arquivo só recebe o nome final depois que a transferência termina; em seguida, o SHA-256 publicado na Release é validado antes de instalar ou abrir a distribuição.

No Android, `AndroidAutoUpdater` exibe um diálogo com `ProgressBar`, porcentagem, bytes transferidos, velocidade, ETA e botão **Cancelar**. No Java/AWT e no desktop libGDX, o atualizador exibe um `JDialog` com `JProgressBar`, velocidade, ETA e cancelamento. As atualizações de interface são encaminhadas para a thread correta de cada toolkit.

Para testar sem acessar uma Release real, o script `.github/scripts/mock-github-release.py` serve os três assets e a resposta compatível com `releases/latest`. O cliente aceita temporariamente uma base alternativa por propriedade de sistema, mantendo `https://api.github.com` como padrão de produção:

```bash
-Dgithub.api.base=http://127.0.0.1:8787
```

O teste unitário `DownloadProgressTest` cobre porcentagem, ETA, streaming para arquivo final, remoção do `.part` durante cancelamento e checksum de arquivo. O workflow de publicação também executa `:release-updater:test` antes de criar ou atualizar a GitHub Release.
