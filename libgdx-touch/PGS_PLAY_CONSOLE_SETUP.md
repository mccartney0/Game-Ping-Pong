# Google Play Games Services v2 e primeiro lançamento

Este documento explica como completar a configuração que o código libGDX/Android espera. A integração usa o SDK Play Games Services v2, inicializado em `GameApplication`, com o adapter `AndroidGameServices`. O módulo `core` depende somente de `GameServices`, então o desktop continua funcionando com `NoopGameServices`.

## 1. O que já está no código

| Arquivo | Responsabilidade |
|---|---|
| `core/.../services/GameServices.java` | Contrato de autenticação, envio de score e abertura de leaderboards. |
| `core/.../services/NoopGameServices.java` | Fallback offline para desktop e testes. |
| `core/.../services/LeaderboardIds.java` | IDs lógicos centralizados. |
| `android/.../GameApplication.java` | Chama `PlayGamesSdk.initialize(this)` e `MobileAds.initialize(this, ...)` no ciclo de vida da aplicação. |
| `android/.../AndroidGameServices.java` | Verifica autenticação, chama `signIn()`, envia score e abre UI oficial. |
| `android/.../AndroidLauncher.java` | Injeta o adapter PGS no jogo e seleciona o leaderboard padrão. |
| `android/src/main/AndroidManifest.xml` | Declara `com.google.android.gms.games.APP_ID`. |
| `android/build.gradle` | Gera os recursos de IDs a partir de propriedades Gradle/variáveis de ambiente, com placeholders e IDs de teste como padrão seguro. |

O SDK v2 tenta autenticar automaticamente quando o jogo inicia. O código verifica `GamesSignInClient.isAuthenticated()` e só chama `signIn()` quando uma operação precisa de autenticação. Essa é a abordagem indicada para uma integração nova v2 [1].

## 2. Criar o aplicativo na Play Console

Acesse [`play.google.com/console`](https://play.google.com/console) com uma conta de desenvolvedor verificada e siga esta sequência:

1. Abra **Home > Create app**.
2. Selecione o idioma padrão e informe **Game Ping Pong Touch** como nome inicial.
3. Marque **Game**, escolha se o jogo é gratuito ou pago e informe um e-mail de contato público.
4. Aceite as declarações do Developer Program Policies, leis de exportação e termos do Play App Signing.
5. Clique em **Create app**.

O nome do pacote do projeto é `com.mccartney0.gamepingpong`. O package name é permanente e não deve ser alterado depois do primeiro bundle enviado [2].

Na ficha inicial, confirme que o `applicationId` de `libgdx-touch/android/build.gradle` é exatamente esse valor:

```groovy
defaultConfig {
    applicationId 'com.mccartney0.gamepingpong'
}
```

## 3. Configurar Play App Signing

No Play Console, abra **Release > Setup > App signing** e aceite o Play App Signing. Para uma nova aplicação, deixe o Google gerar e proteger a **app signing key**, enquanto o CI usa uma **upload key** separada para assinar o AAB enviado.

No GitHub, configure um Environment chamado `production` e adicione estes Secrets:

| Secret | Valor |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Keystore de upload convertido para Base64. |
| `ANDROID_KEYSTORE_PASSWORD` | Senha do keystore. |
| `ANDROID_KEY_ALIAS` | Alias usado no `keytool`. |
| `ANDROID_KEY_PASSWORD` | Senha da chave. |

A chave privada não deve entrar no Git. Um workflow de release deve restaurar a keystore em `${{ runner.temp }}`, executar `bundleRelease` e apagar o runner ao terminar. O workflow versionado neste repositório gera APK debug e executa testes; ele não recebe credenciais de produção. A Play App Signing separa a chave de distribuição da upload key e permite redefinir uma upload key perdida sem trocar a identidade de distribuição [3].

## 4. Configurar o jogo dentro de Play Games Services

No app criado, abra **Grow users > Play Games Services > Setup and management > Configuration**. Se a seção ainda não existir, inicie a configuração do jogo.

Preencha o nome e os dados básicos do jogo e obtenha o **Games services project ID** exibido na área de configuração. Depois, forneça o número real do projeto, sem espaços ou aspas extras, por propriedade Gradle ou variável de ambiente. O manifesto já aponta para o recurso gerado:

```bash
GAME_SERVICES_PROJECT_ID=123456789012 ./gradlew :android:assembleDebug
```

Para builds de release, prefira Secrets/variáveis do ambiente do CI. O recurso é gerado por `android/build.gradle` e não deve ser editado diretamente em `strings.xml`:

```groovy
resValue 'string', 'game_services_project_id', resourceValue(
        'gameServicesProjectId', 'GAME_SERVICES_PROJECT_ID', 'REPLACE_WITH_PLAY_GAMES_PROJECT_ID')
```

O manifesto aponta para esse recurso:

```xml
<meta-data
    android:name="com.google.android.gms.games.APP_ID"
    android:value="@string/game_services_project_id" />
```

### Vincular o aplicativo Android

Na configuração do Play Games Services, adicione um aplicativo Android vinculado com:

| Campo | Valor |
|---|---|
| Package name | `com.mccartney0.gamepingpong` |
| Certificado SHA-1 | Certificado da build usada no teste ou distribuição. |
| Google Cloud project | O projeto criado/associado pela Play Console. |

Cadastre os certificados correspondentes aos ambientes que serão testados. Para desenvolvimento local, use o SHA-1 do debug keystore; para APK assinado fora da Play, use o certificado da upload key; para instalação entregue pela Play, confirme o certificado de app signing indicado em **Release > Setup > App signing**.

Não confunda o **Games services project ID** com um client ID OAuth ou com o leaderboard ID. O manifest usa o primeiro; os leaderboards usam os IDs gerados na seção de leaderboards.

## 5. Criar os leaderboards

Na configuração do jogo, abra **Grow users > Play Games Services > Setup and management > Leaderboards** e clique em **Create leaderboard** [4]. Crie inicialmente estes placares:

| Nome sugerido | Tipo | ID que deve ser copiado para o código |
|---|---|---|
| Arena Mutante — Melhor pontuação | Numeric, maior é melhor | `leaderboard_mutant_arena_score` gerado pela Play Console. |
| Campanha — Bosses derrotados | Numeric, maior é melhor | `leaderboard_campaign_bosses` gerado pela Play Console. |
| Sobrevivência — Melhor pontuação | Numeric, maior é melhor | `leaderboard_survival_score` gerado pela Play Console. |
| Campanha — Menor tempo | Time, menor é melhor | `leaderboard_speed_run_ms` gerado pela Play Console. |
| Melhor combo | Numeric, maior é melhor | `leaderboard_best_combo` gerado pela Play Console. |

Os valores acima são nomes/aliases de organização. A Play Console gera IDs reais que normalmente possuem um identificador próprio. Copie os IDs gerados e forneça-os por propriedades Gradle ou variáveis de ambiente, sem gravá-los no código versionado:

```bash
LEADERBOARD_SURVIVAL_SCORE=ID_REAL_GERADO_NA_PLAY_CONSOLE \
./gradlew :android:assembleDebug
```

O código atual seleciona `leaderboard_survival_score` como placar padrão no `AndroidLauncher`. Para cada modo, selecione o ID apropriado antes de iniciar a partida. Envie o score somente no fim da partida e não a cada frame, evitando chamadas excessivas [5].

Exemplo de integração no core:

```java
game.setCurrentLeaderboardId(
        getString(R.string.leaderboard_survival_score));

// O PingPongTouchGame chama isto uma vez quando o match termina.
game.submitCurrentMatchScoreIfFinished();
```

Exemplo para abrir a UI oficial:

```java
game.showLeaderboard(
        getString(R.string.leaderboard_survival_score));

// Ou abrir todos os placares:
game.showAllLeaderboards();
```

A implementação Android usa `LeaderboardsClient.submitScore()` e `getLeaderboardIntent()`. A UI oficial deve ser aberta com `startActivityForResult`, conforme a documentação do Android [5].

## 6. Adicionar testadores

Adicione as contas de teste nos dois lugares necessários:

1. Em **Play Games Services > Setup and management > Testers**, adicione os e-mails Google usados no dispositivo.
2. Em **Test and release > Internal testing > Testers**, crie a lista de testadores e adicione os mesmos e-mails.

Durante o teste, use uma conta que esteja logada no aparelho e que tenha acesso ao track interno. A autenticação PGS v2 e os leaderboards só devem ser testados em um APK/AAB cujo package name e certificado estejam cadastrados na configuração do jogo.

## 7. Criar o primeiro AAB

Antes do primeiro upload:

1. Injete o project ID, os IDs de leaderboards, achievements e AdMob por Secrets/variáveis do ambiente ou propriedades locais ignoradas pelo Git.
2. Verifique o `applicationId` e o certificado SHA-1 no Play Console.
3. Gere um `versionCode` maior que qualquer versão já enviada.
4. Execute o workflow de release por uma tag, por exemplo `v0.1.0`.
5. Baixe o artefato `game-ping-pong-release-aab` e confirme sua assinatura.
6. No Play Console, abra **Test and release > Internal testing**.
7. Crie um track interno, adicione os testadores e clique em **Create new release**.
8. Faça upload de `android-release.aab`.
9. Informe as notas da versão, revise os avisos e clique em **Save**.
10. Na tela de revisão, clique em **Review release** e depois em **Start rollout to internal testing**.
11. Copie o link de teste e envie aos testadores.

O teste interno é o primeiro destino recomendado para QA e aceita até 100 testadores. A Play Console recomenda testar antes de produção; contas pessoais criadas depois de 13 de novembro de 2023 podem ter requisitos adicionais antes da publicação pública [6].

## 8. Preencher a ficha de lançamento

No dashboard do app, complete cada cartão pendente antes de enviar para produção.

### Store listing

Em **Grow users > Store presence > Main store listing**, preencha:

| Campo | Sugestão inicial |
|---|---|
| App name | `Game Ping Pong Touch` |
| Short description | `Duelo neon de ping-pong com arenas mutantes e bosses.` |
| Full description | Explique controles por toque, Arena Mutante, campanha, skins e jogo offline. |
| App icon | Ícone PNG/JPEG quadrado de alta resolução, sem texto pequeno. |
| Feature graphic | Arte horizontal do jogo para a ficha. |
| Screenshots | Menu, partida, Arena Mutante, boss e inventário de skins. |
| Categoria | Games > Arcade ou categoria equivalente disponível. |
| E-mail | Endereço de suporte que você monitora. |

A ficha é compartilhada entre os tracks de teste e produção [2].

### App content

Em **Policy and programs > App content**, complete:

| Seção | O que informar |
|---|---|
| Privacy policy | URL pública da política de privacidade, especialmente se PGS, analytics ou anúncios forem usados. |
| Ads | Declare se o app contém banners, intersticiais, rewarded ads ou outro SDK de anúncios. |
| App access | Explique que o jogo pode ser jogado offline e que o login PGS é opcional, se esse for o comportamento final. |
| Target audience | Faixa etária real do jogo. |
| Content rating | Questionário de classificação de conteúdo. |
| Data safety | Dados coletados/compartilhados por PGS, anúncios, analytics e armazenamento. |
| Permissions | Declare qualquer permissão sensível, se adicionada futuramente. |

A página App content existe para declarações de privacidade, anúncios, acesso, público-alvo, classificação e segurança de dados [7]. Não declare “sem dados” sem verificar as práticas do SDK e do aplicativo.

### Pricing e distribuição

Em **Products > App pricing** ou seção equivalente:

1. Defina o app como gratuito ou pago.
2. Configure países/regiões de distribuição.
3. Se houver compras dentro do app, configure produtos no Google Play Billing antes de publicar a versão que os referencia.
4. Confirme que a classificação e a política de anúncios correspondem ao conteúdo real.

## 9. Publicar depois do teste interno

Depois de verificar instalação, autenticação, envio de score, UI de leaderboard, rotação, pausa, retorno do background, compras e privacidade:

1. Promova o mesmo bundle ou uma versão com `versionCode` maior para **Closed testing**.
2. Caso a conta tenha requisito de teste fechado, cumpra o período e o grupo exigidos exibidos pela própria Play Console.
3. Corrija os problemas do pre-launch report.
4. Complete todos os itens obrigatórios da ficha e App content.
5. Vá para **Production > Create new release**.
6. Faça upload do AAB assinado, adicione notas e revise os avisos.
7. Envie para review.
8. Use managed publishing se quiser aprovar mudanças antes de disponibilizá-las imediatamente.

Não reutilize um `versionCode` e não altere o package name depois do primeiro release. Para atualizações, gere sempre uma nova versão assinada com a upload key registrada.

## 10. Checklist de teste PGS

| Teste | Resultado esperado |
|---|---|
| Primeiro launch com conta autorizada | `isAuthenticated()` retorna sucesso sem bloquear a partida. |
| Conta sem perfil PGS | O jogo continua jogável; o fluxo de autenticação pode ser iniciado em momento apropriado. |
| Jogo offline | O score permanece local e nenhuma exceção encerra a partida. |
| Fim da partida | `submitScore()` é chamado uma única vez. |
| Score inválido | Valores negativos são rejeitados antes da API. |
| Abrir leaderboard | A UI oficial abre com `startActivityForResult`. |
| Reabrir o jogo | Autenticação é verificada novamente ao voltar ao foreground. |
| AAB instalado pelo track interno | Package name e certificado correspondem ao app vinculado. |

## Referências

[1]: https://developer.android.com/games/pgs/android/android-signin "Android Developers — Platform authentication for Android games"

[2]: https://support.google.com/googleplay/android-developer/answer/9859152?hl=en "Play Console Help — Create and set up your app"

[3]: https://developer.android.com/studio/publish/app-signing "Android Developers — Sign your app"

[4]: https://developer.android.com/games/pgs/leaderboards "Android Developers — Leaderboards"

[5]: https://developer.android.com/games/pgs/android/leaderboards "Android Developers — Leaderboards in Android games"

[6]: https://support.google.com/googleplay/android-developer/answer/9845334?hl=en "Play Console Help — Set up an open, closed, or internal test"

[7]: https://support.google.com/googleplay/android-developer/answer/9859455?hl=en "Play Console Help — Prepare your app for review"
