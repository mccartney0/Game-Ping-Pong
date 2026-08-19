# Manifesto de Assets

**Direção de arte:** Gravura de Cinzas — fantasia medieval estilizado-realista, com madeira e pedra escurecidas, azul Última-Chama para Kael, dourado ritual para Dheren e laranja-cinza para o ataque de Ferrosul.

| Asset | Papel | Status |
|---|---|---|
| Referência visual de Ferrosul | Âncora de composição e paleta do menu e da vila. | Solicitação enviada; geração indisponível por limite diário de imagens. |
| Kael | Referência de silhueta e roupa. | Construído temporariamente com malha procedural. |
| Dheren | Referência de silhueta e energia dourada. | Construído temporariamente com malha procedural. |
| Marca rúnica | Logo/favicion. | Ícone vetorial funcional provisório no HUD; substituir por PNG gerado quando disponível. |

Nenhum asset externo é incorporado nesta versão. A geometria e os efeitos são criados em runtime; portanto, não há dependência de licença de terceiros para o slice atual.

## Atualização GLB planejada

| Modelo prioritário | Papel em Ferrosul | Formato e escala alvo | Fonte e licença |
|---|---|---|---|
| Casa modular | Ferraria, Raposa Vermelha e casas de Ferrosul. | glTF convertido para GLB, 5–9 m de largura. | [Retro Medieval Kit, Kenney](https://kenney-assets.itch.io/retro-medieval-kit) — CC0 1.0. |
| Props de vila | Barris, placas, carroças, tochas, poço e ferramentas. | GLB único por kit, 0,4–3 m. | [Retro Medieval Kit, Kenney](https://kenney-assets.itch.io/retro-medieval-kit) — CC0 1.0. |
| Vegetação | Árvores, troncos e pedras de borda. | GLB instanciável, 4–8 m. | Modelos CC0 do kit selecionado ou malhas auxiliares existentes. |
| Kael e Dheren | Personagens principais. | glTF convertido para GLB bipede, 1,75–1,9 m; idle, caminhada e ataque disponíveis no modelo-fonte. | [RPG Character Pack, Quaternius](https://quaternius.com/packs/rpgcharacters.html) — CC0; personagens humanos rigados e animados. |
| Rastreador de Sangue | Mini-chefe de Ferrosul. | GLB de criatura, 2,6–3 m. | Requer modelo de criatura licenciado; fallback procedural preservado enquanto a fonte é validada. |

O pacote selecionado declara mais de cem modelos nos formatos OBJ, FBX e glTF, sob CC0 1.0. A integração usará GLB derivados somente dos arquivos glTF do pacote e manterá o aviso de licença em `THIRD_PARTY_LICENSES.md`.

O fluxo oficial de download do pacote disponibiliza a opção gratuita “No thanks, just take me to the downloads”; nenhum pagamento, e-mail ou dado pessoal será fornecido para obter os assets.

Para a party, foi validado também o **RPG Character Pack** da Quaternius: seis personagens de fantasia, rigados, animados, texturizados e disponíveis em glTF sob CC0. Kael e Dheren receberão variações de material e efeitos próprios para preservar seus sinais canônicos; nenhum modelo externo será apresentado como retrato literal de um personagem do livro.

O download gratuito da Quaternius abre a pasta pública [RPG Characters — Nov 2020](https://drive.google.com/drive/folders/1MIRQXLfTd21HMI5rwOb6Xy0rv0xv1m8b?usp=sharing), que expõe diretórios de glTF, rig humanoide e texturas. A seleção de um personagem será verificada antes de qualquer uso no runtime.

## Assets incorporados nesta atualização

| Asset | Uso no jogo | URL de runtime |
|---|---|---|
| Quaternius Rogue | Kael, sem cajado para preservar sua condição de iniciante. | `/manus-storage/kael-wanderer_bf0c29b7.glb` |
| Quaternius Warrior | Dheren Varenn, com efeitos dourados e habilidades de Guerreiro Místico do jogo. | `/manus-storage/dheren-warrior_cd65ad32.glb` |
| Kenney Retro Medieval | Estrutura da ferraria, telhado da Raposa Vermelha, árvores, barris e torre do limite de Elwen. | URLs individuais em `GameWorld.ts` |
| Lyra Archer | Modelo low-poly original do projeto, com manto, capuz, aljava, arco e flecha. | `/manus-storage/lyra-archer-exclusive_3bb23d88.glb` |

Os GLBs de personagens foram convertidos a partir de glTF para um único arquivo cada e carregam de modo assíncrono com fallback procedural. Os GLBs de ambiente foram reempacotados com as texturas incorporadas, impedindo buscas relativas bloqueadas pelo armazenamento web. O Rastreador de Sangue permanece como criatura procedural nesta iteração para não reutilizar um personagem humano como inimigo canônico.

O GLB exclusivo de Lyra possui clipes `Idle`, `Walk`, `Bow_Shot` e `Thorn_Rain`, ligados diretamente às suas habilidades de Flecha de Vigia e Chuva de Espinhos.

## Atualização planejada — Rastreador de Sangue

O Rastreador passará a usar um monstro do pacote [Ultimate Monsters, da Quaternius](https://quaternius.com/packs/ultimatemonsters.html). A fonte declara cinquenta criaturas animadas, formato glTF e licença CC0. A integração selecionará uma silhueta predatória, preservará somente animações de espera, caminhada, ataque, dano e morte e converterá o modelo para um GLB auto-contido antes do upload ao armazenamento do projeto.

O modelo **Demon** foi convertido para o GLB auto-contido `/manus-storage/blood-tracker-demon_5c2ab469.glb` e passou a representar o Rastreador de Sangue. A implementação seleciona estados de espera, deslocamento, ataque, dano e morte quando presentes no arquivo e usa as mesmas transições para sincronizar feedback visual e combate.
