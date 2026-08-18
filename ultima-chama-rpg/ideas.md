# Direção de Design — A Chama do Último Reino

## Três abordagens consideradas

| Tema | Introdução breve | Probabilidade |
|---|---|---:|
| **Gravura de Cinzas** | Fantasia medieval estilizado-realista, organizada como uma crônica iluminada pelo fogo: pedra, papel, cinzas e magia azul como elementos narrativos. A proposta dá à aventura uma presença épica e humana, sem cair em visual genérico de fantasia sombria. | 0,07 |
| **Veyra de Vidro e Latão** | Uma direção urbana de fantasia renascentista, com canais, metal polido e luz de lamparina. Privilegiaria o contraste entre o interior acolhedor de Ferrosul e a arquitetura labiríntica de Veyra. | 0,04 |
| **Lenda Pintada pelo Vento** | Um universo de pinceladas amplas, névoa, pedra clara e silhuetas heroicas, inspirado em livros de contos visuais. Daria grande legibilidade à exploração e uma sensação mais contemplativa. | 0,09 |

## Abordagem escolhida — Gravura de Cinzas

### Movimento de design

**Gravura fantástica contemporânea**: o mundo tridimensional é simples e legível como uma maquete artesanal, enquanto a interface, o grimório e os momentos de magia assumem a textura de uma crônica antiga tocada por brasas. A apresentação evita tanto o fotorrealismo pesado quanto o desenho infantil.

### Princípios centrais

1. **A chama é narrativa**: azul para a instabilidade arcana de Kael; dourado para a técnica e a linhagem de Dheren; laranja-cinza para a ameaça de Malgor e para Ferrosul em ruína.
2. **Espaço antes de interface**: a cena 3D deve respirar; HUD e diálogos surgem como instrumentos discretos e contextualizados, não como um painel tecnológico permanente.
3. **Matéria com memória**: madeira, ferro, pergaminho, pedra e cinzas devem comunicar a história da vila antes de qualquer diálogo.
4. **Contraste de experiência**: Kael é visualmente volátil e irregular; Dheren é preciso, dourado e estabilizador. A diferença deve ser visível em efeitos, postura e feedback de combate.

### Filosofia de cor

O mundo de Ferrosul usa **carvão profundo**, madeira escurecida e verde de pinheiro para produzir uma base acolhedora, porém ameaçadora. O azul elétrico da magia de Kael nunca é uma decoração genérica: aparece como acento raro e intenso contra essa base quente. O dourado de Dheren comunica controle, proteção e trajetória. A cor proprietária é **Azul Última-Chama — #62C9FF**.

### Paradigma de layout

A experiência usa o mundo como palco principal. O menu inicial parece uma folha de grimório aberta sobre o fogo; durante a exploração, informações importantes ocupam os cantos e bordas sem criar uma moldura rígida. O painel de missão é uma tira assimétrica de pergaminho na margem esquerda; os retratos de party se empilham verticalmente à direita; diálogos surgem baixos e largos, como legendas de uma crônica.

### Elementos de assinatura

1. **Runas orbitais**: círculos rúnicos incompletos e partículas que se condensam em movimentos discretos ao conjurar magia.
2. **Papel queimado**: bordas irregulares, linhas de gravura e pequenas manchas de tinta no grimório, objetivos e menus.
3. **Brasa direcional**: motas de fogo azul e dourado que sugerem objetivo, ameaça e vínculo sem depender de setas modernas.

### Filosofia de interação

Cada ação deve ter peso, leitura e consequência. Os ataques têm antecipação visual curta, as magias deixam rastros consistentes e as interações narrativas chegam como gestos no mundo — uma fala de Dheren e uma pequena indicação contextual — em vez de tutoriais desconectados.

### Animação

Animações de interface devem usar transições curtas e orgânicas, com opacidade, deslocamento leve e partículas; nunca `scale(0)`. O fogo, a névoa e as runas podem ter movimento contínuo lento. Ataques e esquivas devem responder de imediato, em contraste com a deriva lenta dos elementos ambientais. A experiência respeita `prefers-reduced-motion` para efeitos não essenciais.

### Sistema tipográfico

Títulos usam **Cinzel Decorative** em caixa alta curta e com espaçamento amplo, evocando inscrições e capítulos; textos de leitura e diálogos usam **Spectral**, com ritmo de livro. Labels de controle usam **IBM Plex Mono** em pequeno corpo, preservando clareza. Inter é proibida nesta direção.

### Essência da marca

**A Chama do Último Reino é uma aventura de fantasia de ação para quem quer habitar, e não apenas atravessar, a queda de um reino.** Personalidade: **íntima, incandescente, ancestral**.

### Voz da marca

As manchetes devem ser concisas, evocativas e concretas; CTAs soam como um convite narrativo, não como um produto digital. Exemplos: **“A chama não obedece. Ainda.”** e **“Atravesse Ferrosul antes que as cinzas a encontrem.”**. Evitar frases vazias como “Bem-vindo” ou “Comece agora”.

### Wordmark e logo

O logotipo combina uma chama azul estilizada dentro de um anel rúnico incompleto com uma coroa de cinzas quebrada no topo. O símbolo é autônomo, sem texto, e deve permanecer reconhecível tanto no cabeçalho quanto como favicon. O wordmark usa uma construção própria com serifa gravada, jamais uma fonte-padrão isolada.

## Style Decisions

1. A primeira viewport é sempre uma crônica material: contém marca rúnica, enquadramento de pergaminho/gravura e um acento de chama, sem se resolver em um vazio escuro.
2. **Azul Última-Chama — #62C9FF** permanece raro e funcional, reservado para Kael, runas e convites primários; ouro pertence à proteção e ao controle associados a Dheren.
3. O menu é uma página de grimório aberta sobre Ferrosul; o estado de demonstração é um palco de mundo em chamas, acompanhado por missão, party e diálogo em linguagem de crônica.
