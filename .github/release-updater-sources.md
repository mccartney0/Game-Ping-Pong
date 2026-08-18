# Fontes externas do auto-updater

A implementação consulta a última Release pública pelo endpoint oficial `GET /repos/{owner}/{repo}/releases/latest`, que retorna a Release publicada mais recente não-prerelease/não-draft e inclui `tag_name`, `assets` e `browser_download_url`.

Fonte: https://docs.github.com/en/rest/releases/releases — GitHub REST API endpoints for releases.

A publicação usa `GITHUB_TOKEN` com a permissão mínima `contents: write`, passado ao GitHub CLI pelo ambiente `GH_TOKEN`. A documentação recomenda limitar as permissões do token ao mínimo necessário.

Fonte: https://docs.github.com/en/actions/security-for-github-actions/security-guides/automatic-token-authentication — Use GITHUB_TOKEN for authentication in workflows.

O instalador Android usa uma Intent implícita com ação `ACTION_VIEW`, MIME de pacote Android e URI `content://` emitida por `FileProvider`, concedendo leitura ao instalador do sistema.

Fonte: https://developer.android.com/reference/android/content/Intent — Intent API reference.
