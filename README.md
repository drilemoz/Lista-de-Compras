# Lista de Compras

Aplicativo Android nativo, leve e offline para organizar compras em listas separadas e acompanhar preços ao longo do tempo.

## Versão

`1.0.41+45`

## Identidade técnica

- Nome público: Lista de Compras
- Nome técnico: `listadecompras`
- **Application ID: `com.listadecompras.app`**
- Namespace Kotlin: `com.listadecompras.app` (fontes e testes movidos)
- Android mínimo: 8.0 (API 26)
- compileSdk/targetSdk: 35
- Kotlin: 2.0.21; Java/JVM: 17; Android Gradle Plugin: 8.7.3
- UI: Material 3 com XML e BottomSheetDialog
- **IMPORTANTE:** a troca do applicationId cria um aplicativo novo. O Android não transfere dados do antigo automaticamente. Faça backup JSON pelo app antigo antes de desinstalar e importe na nova instalação. Os dois apps podem coexistir.

## Atualização 1.0.41+45 (correção do Build 6)

- Corrigida a validação de código-fonte do GitHub Actions que ainda exigia a consulta antiga `shop="supermarket"` após a ampliação da busca no Overpass.
- A consulta foi isolada em `MarketOverpassClient.kt` para respeitar o limite de tamanho dos arquivos Kotlin. O novo contrato verifica os tipos suportados, a inclusão de feiras e os filtros por nome/marca.
- O guard de tint do Build 25 foi ajustado para refletir o card com emoji da home, sem desativar as regras do Android Lint.
- Sem mudanças de `applicationId`, keystore/Secrets, banco SQLite, dados salvos ou formato do backup.
- É necessário repetir o workflow para verificar o build Gradle/Android e os testes no runner.

## Atualização 1.0.40+44 (listas mais elegantes e mercados aprimorados)

- Cartões das listas redesenhados com visual mais rico: ícone contextual por lista, estado de compra destacado, mercado vinculado e mensagem de progresso/orçamento mais clara.
- Diálogo **Nova lista** ajustado com sugestões rápidas em grade 2x2, alinhadas visualmente e mais fáceis de tocar.
- Tela **Mercados próximos** agora busca também mercearias, hortifrutis, atacados, conveniências e marketplaces/feiras, reduzindo os casos em que quase nada aparecia.
- Mapa Leaflet/OpenStreetMap melhorado com marcadores em `circleMarker`, ajuste automático de layout (`invalidateSize`) e estados de carregamento/empty state mais claros.
- Lista de resultados de mercados ganhou contagem visível, vazio contextual, tipo do estabelecimento e CTA de lista mais descritivo.
- Persistência de favoritos, vínculo com listas e cache recente foram preservados.
- Validações estáticas foram reexecutadas; compilação Gradle/Android e teste físico continuam pendentes neste ambiente.

## Atualização 1.0.39+43 (ajustes do editor e novos ícones)

- Tela de inclusão/edição de produtos reconstruída com cabeçalho e ação Salvar fixos, contornos discretos e opções secundárias recolhíveis.
- Seletor de emojis em grade de cinco colunas, com estado visual da seleção e possibilidade de alterar ícone de categoria existente sem modificar seu nome.
- Calculadora de ofertas: total do combo ÷ quantidade de unidades (2 a 999), exibindo preço por unidade arredondado, mas preservando a precisão para cálculos de subtotal.
- O total monetário é calculado a partir do valor original da oferta e arredondado apenas no fim, evitando R$ 14,79 virar R$ 14,80 em ofertas de 2 unidades.
- Campo de nome com mais respiro visual e seletor de categorias expandido com novos ícones para tecnologia, carros, bicicleta e outras áreas. Estrutura de combos, categorias e backup JSON schema 7 foi preservada.
- No modo de compra, a edição manual do total substitui os metadados da oferta, para não mostrar valores antigos nos totais.
- Android continua em `com.listadecompras.app`, preservando dados locais e assinatura (os Secrets de assinatura não foram alterados).
- Verificações estáticas e testes de cálculo documentados; compilação Gradle/Android ainda não confirmada neste ambiente.

## Atualização 1.0.37+41 (correção do Build 2)

- A compilação Debug, os testes Kotlin e o Android Lint da versão anterior **passaram** no GitHub Actions; falha ocorreu exclusivamente pela falta dos Secrets da assinatura permanente.
- Mesmo sem Secrets, o workflow agora disponibiliza o **APK Debug** em Actions > Execução > Artifacts (30 dias).
- A **GitHub Release** somente será criada quando quatro Secrets de um mesmo conjunto estiverem presentes e válidos. Grupos legados permanecem aceitos, sem misturar senhas e keystore de conjuntos diferentes.
- Foi reforçada a verificação de assinatura da Release com `apksigner` e adicionada validação de cenários de Secrets para impedir regressões.
- Identidade técnica, dados, interface, categorias, backup schema 6 e demais funcionalidades continuam inalterados.

## Atualização 1.0.36+40

- Renomeados applicationId, namespace/pacotes Kotlin, nomes técnicos, preferências, estilos e recursos técnicos para **Lista de Compras**.
- Categorias padrão e personalizadas com nome/emoji persistidos em SQLite (`lista_de_compras_categories.db`), duplicatas ignoradas sem distinguir maiúsculas, ícones no seletor e etiquetas de itens.
- Novo botão `+ Nova` no cadastro de item abre formulário de categoria com seletor de 20 ícones. Categorias persistem mesmo sem itens associados.
- Novo fluxo de `Nova lista`: BottomSheet Material 3 com cartões para lista vazia ou modelo; formulário com sugestões de nomes e CTA destacado.
- Backups no formato `lista-de-compras-backup`, schema 6, exportam categorias; importador continua aceitando `meu-supermercado-backup` schemas 1 a 5, recuperando nomes de categorias dos itens quando possível.
- Segredos de assinatura aceitam `LISTA_COMPRAS_*` com fallback para `LISTA_MERCADO_*` já existentes. Não é preciso alterar a keystore usada anteriormente para compilar uma assinatura válida.
- CI inclui `scripts/validate_categories_and_identity.py` para validar identidade, schema SQLite e recursos XML. Compilação, Lint e testes no Android ainda precisam ser confirmados antes de disponibilização de APK.

## Atualização 1.0.35+39 (novo nome público)

- Aplicativo renomeado para **Lista de Compras** no Android e nas informações exibidas ao usuário.
- Arquivos APK, backups JSON e exportações CSV adotam o novo nome; backups antigos seguem compatíveis.
- Metadados, tela de Novidades, workflow de Release e documentação sincronizados.
- Namespace do código, armazenamento local, formato do backup e Secrets de assinatura legados preservados.

## Atualização 1.0.34+38 (correção do Build 26)

- O Build 26 confirmou testes JUnit e APK Debug, mas o Lint falhou com 1 erro (`UnsafeOptInUsageError`, `BarcodeScannerActivity.kt:119`) e 272 avisos.
- O opt-in Kotlin aplicado em `startCamera` não foi reconhecido pelo Android Lint dentro do callback. O acesso a `ImageProxy.image` foi isolado em `readMediaImage(ImageProxy)`, com `@androidx.annotation.OptIn(markerClass = [ExperimentalGetImage::class])` no próprio método.
- O leitor continua usando ML Kit e liberando o `ImageProxy` ao fim do processamento; sem mudança nos códigos reconhecidos.
- Adicionado `scripts/validate_lint_build26.py` ao workflow, e atualizado o validador do Build 25 para detectar a regressão real, sem suprimir o Lint.
- Dados, schema de backup 5, assinatura e applicationId não foram alterados.
- Pendentes `:app:lintDebug`, Release e teste físico no GitHub Actions.

## Atualização 1.0.33+37 (correção do Build 25)

- Lint corrigido em `BarcodeScannerActivity` com anotação explícita de API experimental CameraX.
- Os 12 `android:tint` incompatíveis com AppCompat foram substituídos por `app:tint` em seis layouts; ícones e cores preservados.
- Validação preventiva nova no workflow para evitar que os 13 erros retornem.
- Não houve alteração de listas, preços, scanner funcional, armazenamento, backup schema 5 nem assinatura.
- O CI precisa confirmar Android Lint e APK Release; esta entrega contém o código-fonte.

## Atualização 1.0.32+36 (correção do Build 24)

- Corrigido o acesso a `LocationManager.getLastKnownLocation()` e `requestLocationUpdates()` para verificar explicitamente permissões de localização e capturar `SecurityException` caso sejam revogadas após a checagem.
- A tela de mercados informa falha quando nenhum provedor pode ser registrado, evitando indicador de carregamento permanente.
- O GitHub Actions continuará exigindo `:app:lintDebug` e mostrará os demais erros do relatório completo em caso de falha. O log do Build 24 informou 15 erros, mas expôs somente o primeiro.
- Novo validador estático `scripts/validate_location_lint.py` para impedir a regressão da proteção de localização.
- Nenhuma mudança no schema de backup 5, identificador, assinatura, banco ou modelo de listas.

## Atualização 1.0.31+35

- Listas e catálogo mais fluidos com `ListAdapter`/`DiffUtil` e snapshots dos modelos mutáveis.
- Correção do vínculo entre a linha da lista e o registro real ao marcar comprado/renomear.
- Navegação e subrecursos do mapa limitados a endereços permitidos, com integridade da biblioteca Leaflet.
- Testes JUnit de valores, total da lista e política de URLs; build Debug e lint adicionados ao workflow.
- Mantidas as funções existentes, armazenamento offline e backup schema 5.

## Atualização 1.0.30+34

- Filtros combináveis para produtos favoritos e recorrentes no catálogo, com resultado e estado vazio contextual.
- Preço mais destacado nos cartões do catálogo, com contorno discreto.
- Resumos de comparação e histórico em cartão legível.
- Descrições curtas nas seções de Configurações, sem modificar os fluxos de exportação e restauração.

## Atualização 1.0.29+33

- Novo progresso visual da compra, com contador de itens concluídos.
- Ação principal de encerrar fixada no rodapé, respeitando a barra de navegação do Android.
- Campos de quantidade e total com altura de toque ampliada; rascunhos temporários preservados durante rolagem ou rotação.
- Não encerra a compra com rascunhos incompletos, mesmo quando a linha não estiver visível.

## Atualização 1.0.28+32

- Corrigido o erro AAPT2 `resource style/Widget.ListaMercado not found` do build 22 ao definir explicitamente `Widget.Material3.CardView.Outlined` como estilo pai do cartão reutilizável.
- Acrescentada validação preventiva de estilos e referências XML ao GitHub Actions.
- Preservados o aplicativo, as telas, os dados, o backup schema 5 e a assinatura configurada no repositório.

## Atualização 1.0.24+28

Correções de integridade de dados, confirmação de preço, busca e orçamento; schema 5 inalterado.

## Atualização 1.0.25+29

Sistema de estilos reutilizáveis aplicado às telas existentes, mantendo compatibilidade e todas as correções de 1.0.24+28.

## Atualização 1.0.27+31

Detalhes, filtros e cartões redesenhados; sem alterações de armazenamento e cálculo.

## Funcionalidades atuais

- tela inicial com listas independentes, por exemplo `Supermercado do João`, `Compra do mês` ou `Viagem`;
- criar, renomear, duplicar e excluir listas;
- migração automática dos itens das versões antigas para `Minha lista`;
- detalhes de lista e cartões compactos com contador, filtros claros e identificação do mercado;
- itens com quantidade planejada, quantidade comprada, unidade, categoria, preço e observação;
- marcação de comprado com estimativa pela quantidade planejada e carrinho pela quantidade realmente comprada;
- modo de compra rápida por lista, mostrando apenas pendentes, checkbox grande, preço total direto no item, carrinho/orçamento em tempo real e ação para desfazer;
- catálogo local/offline com sugestões ao adicionar itens, último preço, unidade/categoria padrão e código de barras opcional;
- produtos favoritos e recorrentes com frequência semanal, quinzenal ou mensal;
- preço-alvo opcional por produto, com status abaixo/dentro/acima do alvo na lista e no Modo compra;
- modelos de lista iniciais e modelos personalizados criados a partir de qualquer lista;
- leitor de código de barras pela câmera, com catálogo local offline e consulta opcional na internet apenas quando o código ainda não for conhecido;
- busca por lista, supermercado, produto ou categoria;
- filtros Todos/Pendentes/Comprados adaptados à largura da tela;
- preço com formatação automática em real;
- comparação de duas listas e de preços unitários;
- histórico de um produto em diferentes listas/datas;
- Configurações por engrenagem no topo;
- aparência Sistema/Claro/Escuro;
- Últimas alterações, Sobre e Doação via Pix;
- armazenamento local offline;
- backup completo em JSON com schema versionado, importação validada e opções de mesclar/substituir;
- exportação CSV de itens e catálogo usando o seletor de arquivos do Android.

## Persistência e histórico

Cada lista preserva a data, os itens e os preços registrados. Duplicar uma lista cria uma nova compra com os mesmos produtos e preços de referência, mas desmarca os itens como comprados. Isso permite atualizar os valores e depois comparar compras diferentes.

## Build e Release

O projeto mantém somente `.github/workflows/android-release.yml` como workflow principal. Ele valida a versão e o código-fonte, executa testes unitários, compila `:app:assembleDebug` e verifica `:app:lintDebug`.

O **APK Debug** é publicado como Artifacts da execução do GitHub Actions mesmo sem chave de produção. Para a **Release**, é necessária uma keystore válida com os quatro Secrets de um mesmo conjunto. Quando completos, o workflow compila `:app:assembleRelease`, valida a assinatura com `apksigner` e publica no GitHub Releases. O APK Debug **não** é publicado como Release oficial e sua assinatura de teste pode mudar entre execuções. O workflow não gera `source.zip`.

Os nomes de Secrets aceitos estão documentados em `SIGNING-SECRETS.txt`.


## Melhorias visuais da v1.0.7+8

- Corrigido o problema de conteúdo encostando na barra de notificações e na navegação com tratamento de insets.
- Tela de adicionar/editar item refeita como bottom sheet moderna, com campos mais agradáveis, dropdowns melhores e botões mais claros.
- Caixa de renomear/criar lista refeita no mesmo padrão visual moderno.
- Campos de busca principais migrados para caixas Material 3 mais consistentes.


## Refinamento visual — 1.0.8+9

- Tela inicial redesenhada com card de resumo geral, valor estimado, quantidade de listas, itens e comprados.
- Cards das listas ganharam ícone, data de atualização, total e barra de progresso da compra.
- Cards dos produtos foram reorganizados com preço total, preço unitário, chips de quantidade/categoria e ações mais limpas.
- Estado vazio e busca da tela inicial foram refinados para seguir o mesmo padrão Material 3.


## Lista inicial Cicloviagem — 1.0.9+10

O aplicativo cria uma única vez a lista padrão **Cicloviagem**, com 18 itens iniciais de alimentação para facilitar o preparo de uma viagem de bicicleta. A lista é totalmente editável e pode ser excluída; se for removida pelo usuário, não é recriada automaticamente. Em instalações já existentes, ela é adicionada uma única vez sem alterar as listas atuais.


## Mapa e tela de novidades — 1.0.10+11

- Novo **Mercados próximos** com OpenStreetMap e consulta Overpass sem chave de API.
- Usa a localização do Android, mostra a distância e amplia automaticamente a busca de 5 km para 15 km e até 30 km quando necessário.
- Cada marcador permite criar uma lista já nomeada com o supermercado e a data.
- Tela de detalhes da lista foi compactada para mostrar mais produtos, com resumo, busca e filtros mais limpos.
- Cards dos produtos foram redesenhados: tocar no card edita e o menu de três pontos concentra editar/excluir.
- Nova tela **O que mudou nesta atualização**, exibida somente na primeira abertura após cada versão.
- `WhatsNewContent.kt` centraliza as novidades e deve ser atualizado em todas as próximas versões.


## Versão 1.0.11+12

- Orçamento configurável por lista, com restante, excesso e barra de uso.
- Histórico inteligente de preços com último, menor, maior, média e tendência.
- Atalho “Histórico de preço” diretamente no menu de cada produto.
- Paleta principal alterada de verde para violeta/lilás, incluindo mapa, ícone e botões principais.
- Contraste de `colorOnPrimary` definido explicitamente para impedir texto azul sobre botões preenchidos.


## Correção da tela de atualização — 1.0.12+13

A tela de novidades agora é protegida contra abertura duplicada durante recriações de Activity/tema. A confirmação é persistida de forma síncrona ao tocar em **Continuar**, e instâncias restauradas fecham automaticamente se a versão já tiver sido vista.


## v1.0.13+14 — Cicloviagem e tema escuro

A lista inicial Cicloviagem recebe os preços de referência que já tinham sido definidos nas conversas de planejamento. Em instalações existentes, apenas itens ainda sem preço são preenchidos. O tema escuro usa agora um grafite azulado (`#101820`/`#121B24`) em vez de preto puro, mantendo o violeta como destaque.


## Correção de build 1.0.14+15

O Android Lint exige que recursos definidos em `values-night` tenham uma declaração padrão em `values`. A versão anterior introduziu 12 cores exclusivas do tema escuro sem esses defaults. Esta versão adiciona os equivalentes padrão e uma validação automática para impedir regressão.


## Interface da tela inicial — 1.0.15+16

A busca agora é aberta pela lupa no cabeçalho, ao lado das Configurações. O resumo de compras foi compactado e mostra listas, itens e comprados junto ao título, deixando mais espaço para as listas cadastradas.


## Interface e mercados — 1.0.16+17

- Busca dentro da lista movida para uma lupa no cabeçalho, abrindo somente quando necessária.
- Resumo da lista reorganizado para dar o mesmo destaque ao total estimado e ao valor do carrinho, além de mostrar quanto falta ou excedeu do orçamento.
- Menus de três pontos ficam legíveis no tema escuro e as ações de lista aparecem lado a lado em um painel compacto.
- Confirmações destrutivas usam vermelho para destacar exclusão/limpeza.
- A busca de mercados é adaptativa: começa em 5 km, amplia para 15 km e pode chegar a 30 km se houver poucas opções.
- A consulta inclui supermercados e mercados de conveniência cadastrados no OpenStreetMap.

## Modo compra — 1.0.17+18

Cada lista possui agora um **Modo compra** dedicado ao uso dentro do supermercado. A tela trabalha sobre os mesmos itens persistidos da lista normal, mostra apenas produtos pendentes, oferece checkbox grande, quantidade em destaque e entrada rápida do preço total da quantidade comprada. Ao marcar um item, ele sai da lista ativa, o carrinho e o orçamento são recalculados imediatamente e uma ação permite desfazer. Nenhum conjunto paralelo de itens ou histórico é criado.



## Catálogo e código de barras — 1.0.18+19

O aplicativo mantém agora um catálogo local/offline formado pelos produtos já usados nas listas. Nomes equivalentes são consolidados por forma normalizada para evitar duplicações no catálogo. Ao adicionar um item, o campo de nome sugere produtos conhecidos e pode preencher categoria, unidade e último preço registrado.

O editor ganhou leitura de código de barras pela câmera. A leitura usa o modelo embarcado do ML Kit, disponível sem conexão após a instalação: códigos conhecidos preenchem o produto pelo catálogo local; códigos ainda desconhecidos podem consultar opcionalmente o Open Food Facts quando houver internet, sem inventar preço e mantendo todos os campos editáveis. O catálogo pode ser consultado em **Configurações > Catálogo de produtos**.

## Correção de build — 1.0.18.1+20

- Corrigida a falha `checkReleaseAarMetadata` introduzida pelo CameraX 1.6.2.
- CameraX fixado em `1.5.3`, mantendo compatibilidade com `compileSdk 35`, AGP `8.7.3`, Kotlin `2.0.21` e Java 17.
- O leitor de código de barras e o catálogo offline permanecem inalterados funcionalmente.
- `scripts/validate_source.py` passa a bloquear CameraX 1.6.x enquanto a base técnica permanecer em compileSdk 35 / AGP 8.7.3.



## Mercados favoritos e mapa — 1.0.21+24

- **Mercados próximos** permite escolher manualmente raios de **5, 10, 20, 30 ou 50 km**.
- A busca parte do raio escolhido e continua expandindo automaticamente quando encontra poucas opções.
- Supermercados podem ser **favoritados** e aparecem primeiro na lista de resultados, com destaque também no mapa.
- Cada resultado mostra **nome, distância, endereço quando disponível e origem do dado**.
- Ao criar uma lista por um mercado, a associação é persistida e a próxima busca oferece **Abrir lista deste mercado**.
- Um cache simples mantém resultados recentes para contingência quando internet ou Overpass estiverem indisponíveis.
- Ausência de resultados não é apresentada como falha de localização: o aplicativo informa separadamente localização, conectividade e disponibilidade do serviço.
- O backup JSON passa ao **schema 3**, preservando favoritos de mercados, raio preferido e associação entre mercado e lista.

## Favoritos, recorrentes e modelos — 1.0.20+23

- Produtos do catálogo podem ser marcados como **favoritos** e ficam priorizados nas sugestões.
- Qualquer produto conhecido pode ser configurado como recorrente com frequência **semanal**, **quinzenal** ou **mensal**.
- Cada lista possui a ação **Recorrentes**, que abre uma tela de seleção e adiciona os produtos escolhidos sem duplicar nomes já existentes na lista.
- O fluxo **Nova lista** oferece **Lista vazia** ou **Usar modelo**.
- Modelos iniciais: **Cicloviagem**, **Compra do mês**, **Churrasco**, **Camping** e **Limpeza**. Apenas Cicloviagem contém itens predefinidos; os demais são intencionalmente vazios.
- O menu de ações de qualquer lista permite **Salvar modelo**, preservando itens, quantidades, unidades, preços e orçamento, mas zerando o estado de comprado ao reutilizar.
- O backup JSON usa schema 2 e inclui favoritos, recorrência e modelos personalizados.

## Refinos no cadastro e leitura de código — 1.0.19.1+22

- O campo **Nome do produto** da tela **Adicionar item** foi refeito para ficar mais legível e não parecer cortado no topo.
- O editor mostra melhor as mensagens do fluxo de código de barras e diferencia produto conhecido localmente, código novo e dado online sugerido.
- Se o código lido ainda não existir no catálogo local e houver internet, o aplicativo pode consultar o **Open Food Facts** para tentar preencher nome e categoria sem inventar preço.
- O leitor ganhou botão de luz, vibração ao sucesso e uma seleção mais estável do código central, reduzindo leituras erradas.

## Backup, restauração e refinamento visual — 1.0.19+21

- Configurações ganhou **Exportar backup**, **Importar backup** e **Exportar CSV**.
- O backup JSON possui schema versionado e inclui listas, itens, preços, orçamento, estado de compra, observações, catálogo e uma visão derivada do histórico de preços.
- A importação valida o arquivo e mostra um resumo antes de permitir **Mesclar** ou **Substituir**.
- Arquivos são escolhidos pelo Storage Access Framework do Android; não existe pasta fixa obrigatória.
- O tema escuro recebeu superfícies mais claramente grafite/azuladas, sem preto puro, mantendo violeta/lilás como destaque.
- A tela **Adicionar item** foi reorganizada em blocos de Produto, Compra e Detalhes, com campos preenchidos, menos bordas e ação de leitura de código integrada.


## Estabilidade do leitor de código — 1.0.21.1+25

- O reconhecimento só retorna após três leituras consecutivas do mesmo código.
- EAN/UPC são validados quando possuem dígito verificador compatível.
- A vibração de sucesso é não crítica e possui a permissão Android necessária.
- O editor mostra sempre o código reconhecido, mesmo quando o produto ainda não existe no catálogo ou na consulta opcional online.
- Retornos inválidos do scanner deixam de ser silenciosos e exibem orientação para tentar novamente.



## Quantidade planejada x comprada — 1.0.23+27

- `quantity` continua representando a quantidade planejada para compatibilidade com os dados existentes.
- `purchasedQuantity` registra separadamente quanto foi realmente comprado.
- A estimativa usa quantidade planejada; o carrinho e o total pago usam quantidade comprada.
- O Modo compra permite ajustar a quantidade real antes de concluir o item.
- Compras antigas marcadas como concluídas são migradas assumindo que a quantidade comprada era igual à planejada.
- O histórico e o backup passam a registrar a quantidade realmente comprada.

## Preço-alvo por produto — 1.0.22+26

- Cada produto do catálogo pode guardar um preço-alvo opcional e a unidade à qual esse alvo se refere.
- O alvo pode ser definido/removido no catálogo ou durante a edição de um item.
- A lista normal e o Modo compra mostram o resultado com símbolo e texto: abaixo, dentro ou acima do alvo.
- O histórico compara o preço atual com o alvo, o registro anterior e a média histórica.
- O backup usa schema 4 e o CSV inclui os campos do preço-alvo.

