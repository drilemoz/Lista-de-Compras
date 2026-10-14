# Changelog

## 1.0.38+42 (editor, categorias e combos)

- Tela de inclusão/edição de produtos reconstruída com cabeçalho e ação Salvar fixos, contornos discretos e opções secundárias recolhíveis.
- Seletor de emojis em grade de cinco colunas, com estado visual da seleção e possibilidade de alterar ícone de categoria existente sem modificar seu nome.
- Calculadora de ofertas: total do combo ÷ quantidade de unidades (2 a 999), exibindo preço por unidade arredondado, mas preservando a precisão para cálculos de subtotal.
- O total monetário é calculado a partir do valor original da oferta e arredondado apenas no fim, evitando R$ 14,79 virar R$ 14,80 em ofertas de 2 unidades.
- Metadados `comboTotalPrice`/`comboUnitCount` persistidos em listas, modelos e backup JSON schema 7; leitores aceitam backups anteriores. Exportação CSV inclui as duas colunas.
- No modo de compra, a edição manual do total substitui os metadados da oferta, para não mostrar valores antigos nos totais.
- Android continua em `com.listadecompras.app`, preservando dados locais e assinatura (os Secrets de assinatura não foram alterados).
- Verificações estáticas e testes de cálculo documentados; compilação Gradle/Android ainda não confirmada neste ambiente.

## 1.0.37+41 (14/10/2026)

- **Correção do Build 2**: o log confirmou `:app:testDebugUnitTest`, `:app:assembleDebug` e `:app:lintDebug` aprovados; falhou apenas a etapa "Resolve signing secrets" porque todos os Secrets de assinatura estavam vazios.
- O workflow sempre entrega o APK Debug validado em **Actions > Artifacts** (retenção de 30 dias), mesmo quando não houver chave Release.
- A Release assinada é **condicional** a um conjunto completo e coerente de quatro Secrets, sem misturar credenciais de grupos diferentes e sem publicar APK Debug como Release de produção.
- O script `scripts/resolve_signing.sh` valida Base64, arquivo de keystore, senha e alias, mantém senhas fora dos outputs e registra apenas o estado da assinatura.
- A assinatura Android Release passa a ser validada por `apksigner verify`, com suporte aos esquemas modernos do APK.
- Adicionados testes da assinatura com casos sem Secrets, configuração parcial, chave completa, fallback e chaves inválidas.
- Identificador `com.listadecompras.app`, funcionalidades, bancos SQLite, schema de backup 6 e compatibilidade com backups antigos foram preservados.
- **Pendente:** executar o workflow atualizado e, para publicar uma Release assinada, configurar os Secrets reais no repositório.

## 1.0.36+40 (14/10/2026)

- Identidade técnica alterada para `com.listadecompras.app` (applicationId/namespace/pacotes), nome técnico `listadecompras`, preferências locais e estilos Material.
- Novo catálogo de categorias persistente em SQLite com ícones: seleção visual, cadastro personalizado, prevenção de duplicidade e exibição nos itens e catálogo.
- Criação de listas redesenhada: escolha visual entre começar do zero/modelo, formulário atualizado e sugestões de nome.
- Backup schema 6 inclui as categorias e mantém leitura dos backups legados; categorias antigas podem ser recuperadas dos produtos e listas exportados.
- Workflow aceita Secrets novos e antigos de assinatura. Importante: mudar applicationId instala outro app; dados não são migrados automaticamente, usar exportação/importação JSON.
- Análises estáticas e XML; compilação Android e teste no aparelho pendentes.

## 1.0.35+39 — 2026-10-14

- **Nova marca pública:** aplicativo renomeado para **Lista de Compras** no launcher, tela Sobre, configurações de projeto e metadados.
- **Distribuição:** APKs, releases, nomes sugeridos para backup JSON e exportação CSV passam a usar **Lista de Compras**.
- **Referências externas:** identificadores de aplicação nas requisições HTTP atualizados; documentos atuais, instruções de assinatura e tela de Novidades sincronizados.
- **Compatibilidade:** mantidos `applicationId=com.listamercado.app`, namespace Kotlin, nomes de `SharedPreferences`, formato interno `meu-supermercado-backup` (schema 5), chave de assinatura e Secrets legados. Os backups antigos continuam restauráveis.
- **Validação:** incluído teste estático de identidade pública e preservação de dados. Compilação Android, Lint e teste em aparelho pendentes do CI.

## 1.0.34+38 — 2026-10-13

- **Correção do Build 26:** a versão 1.0.33 compilou Debug e passou nos testes JUnit, mas o Android Lint continuou apontando `UnsafeOptInUsageError` em `imageProxy.image` (1 erro, 272 avisos).
- **CameraX:** removido opt-in Kotlin que não foi reconhecido pelo Android Lint no callback; criado método isolado com `@androidx.annotation.OptIn` no acesso experimental.
- **Teste de regressão:** incluído `scripts/validate_lint_build26.py`, atualizado o guard do Build 25; Lint continua obrigatório, sem supressões.
- **Compatibilidade:** backup schema 5, armazenamento, permissões, assinatura e identificador Android preservados.
- **Pendente:** validação completa do Android Lint e Release no GitHub Actions.

## 1.0.33+37 — 2026-10-13

- **Correção do Build 25:** a compilação Debug e os testes passaram, mas o Android Lint encontrou 13 erros e 272 avisos. Corrigidos os 13 erros reportados no relatório de CI: um uso de `ImageProxy.image` sem opt-in CameraX e 12 usos de `android:tint` em seis layouts AppCompat.
- **CameraX:** anotação `@OptIn(ExperimentalGetImage::class)` adicionada ao método que registra o analisador. Sem alteração do algoritmo de leitura de códigos de barras.
- **Material 3/AppCompat:** ícones preservam as mesmas cores e atributos, usando `app:tint` compatível com AppCompat.
- **Prevenção:** novo `scripts/validate_lint_build25.py` integrado ao workflow; Android Lint permanece habilitado e obrigatório.
- **Compatibilidade:** schema 5, dados locais, configuração de assinatura, minSdk e applicationId preservados.
- **Pendente:** validar `:app:lintDebug` e `:app:assembleRelease` no CI; os 272 avisos não foram tratados nesta correção.


## 1.0.32+36 — 2026-10-13

- **Correção do Build 24:** o workflow compilou Debug e executou os testes unitários, mas parou em `:app:lintDebug` (15 erros, 270 avisos). O log disponibilizou apenas o primeiro: `MissingPermission` em `NearbyMarketsActivity.kt:153`.
- **Permissão e localização:** validação explícita de `ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION` nos métodos que chamam APIs de localização, `try/catch SecurityException` e tratamento para provedor indisponível.
- **Diagnóstico no CI:** se o Lint falhar, o workflow imprime todas as linhas de erro do relatório, sem reduzir severidade ou desativar o Lint.
- **Regressão:** validador estático novo para proteger essa correção; schema 5, dados existentes e assinatura preservados.
- **Limitação:** sem Android SDK local para reproduzir o Lint completo; os outros 14 erros reportados só podem ser diagnosticados com o relatório integral de um novo build.

## 1.0.30+34

- Catálogo: filtros de favoritos e recorrentes combináveis com busca.
- Cartões do catálogo com preço destacado e estado vazio contextual.
- Comparação/histórico com resumo visual separado.
- Configurações com orientação textual e conteúdo existente preservado.
- Dados, backup, app ID, assinatura e minSdk inalterados.

## 1.0.29+33

- Interface de compra reorganizada com botão fixo e progresso visual.
- Preservação dos rascunhos de preço e quantidade em rolagem e recriação de Activity.
- Campos de edição com altura mínima de 56 dp; marcação por item ampliada.
- Armazenamento e schema de backup inalterados.

## 1.0.31+35 — 2026-10-13

- **Desempenho da interface:** as listas de compras, produtos, catálogo e comparações agora usam `ListAdapter`/`DiffUtil` e atualizam as linhas alteradas em vez de redesenhar todas. Itens e listas mutáveis são copiados antes da comparação assíncrona para não mascarar alterações.
- **Correção de persistência:** o checkbox de um produto e as ações de lista usam o objeto vigente no repositório, não o snapshot visual utilizado pelo adaptador.
- **Mapa reforçado:** carregamentos externos e navegação no WebView passam por lista explícita de origens/caminhos permitidos; JavaScript, CSS e tiles somente por HTTPS. Fixados hashes SRI para Leaflet 1.9.4 e bloqueado acesso universal de arquivos locais.
- **Qualidade:** adicionados testes Android/JUnit para totais planejados/comprados, preços digitados e recursos do mapa, além de teste Kotlin offline e etapa Debug/testes/lint no GitHub Actions.
- **Compatibilidade:** `applicationId`, Android 8.0 mínimo, banco local SharedPreferences/JSON, backup schema 5, chaves de assinatura e Modo compra preservados. As alterações não migraram o armazenamento nem modificaram formatos de dados.
- **Validação:** análises locais estáticas, XML, testes Kotlin e ZIP; build completo, JUnit Android e lint dependem do GitHub Actions/SDK Android.


## 1.0.28+32 — 2026-10-13

- **Build 22 corrigido:** Evitada a herança implícita para o estilo inexistente `Widget.ListaMercado`, definindo `Widget.Material3.CardView.Outlined` como pai de `Widget.ListaMercado.Card`.
- **Prevenção:** Adicionado `scripts/validate_resource_styles.py` ao workflow para identificar estilos locais sem pai e referências não resolvidas antes do AAPT2.
- **Compatibilidade:** Nenhum modelo de dados ou schema modificado; layout e navegação da versão 1.0.27 preservados.
- **Escopo:** Versão de correção de compilação; redesenho do Modo compra adiado para a próxima versão funcional.


## 1.0.27+31 — 2026-10-13

- **Resumo compacto:** Orçamento, estimativa, carrinho e progresso passaram a ocupar menos altura.
- **Cartões de produto:** Nome, quantidade, preço e categoria ganharam hierarquia visual; observações e alertas permanecem acessíveis.
- **Filtros e estados vazios:** Filtros de 48 dp, contador de produtos visíveis e mensagens específicas de busca, pendentes e comprados.
- **Identificação da lista:** O nome do mercado aparece sob o título quando está cadastrado.
- **Continuidade:** Inclui a navegação compacta da versão anterior, sem alteração de schema ou de regras financeiras.



## 1.0.25+29 — 2026-10-13

- Design Material 3: Adicionados estilos reutilizáveis de cartões, botões, campos e títulos com espaçamentos e raios padronizados.
- Tema claro: Superfícies e contornos claros agora usam tokens explícitos, preservando contraste.
- Cartões e campos: Aplicados componentes visuais consistentes às telas existentes, sem alterar a navegação.
- Acessibilidade: Alvos de toque principais ampliados para 48 dp nas listas e nos detalhes.
- Correções mantidas: Incluída toda a proteção de dados, preços, busca e orçamento da versão anterior.


## 1.0.24+28 — 2026-10-13

- Edição segura de preços: Os valores do Modo compra são confirmados ao concluir ou sair do campo; rascunhos vazios não viram zero.
- Proteção das listas: JSON ilegível não gera uma lista vazia nem é sobrescrito automaticamente; cópia local anterior pode ser restaurada com confirmação.
- Orçamento correto: A cor de alerta aparece apenas quando o orçamento é excedido.
- Busca ampliada: Localiza listas também pelo nome do mercado e pela categoria dos produtos.
- Backup compatível: Mantido o schema 5 e o formato existente de listas.


## 1.0.23+27 — 2026-09-26

- Separada a **quantidade planejada** da **quantidade realmente comprada** em cada item.
- A estimativa da lista continua usando a quantidade planejada; o valor do carrinho usa a quantidade comprada dos itens concluídos.
- O **Modo compra** ganhou campo de quantidade comprada e o preço rápido passou a representar o **total pago** pela quantidade realmente levada.
- Ao marcar um item como comprado sem informar a quantidade real, o aplicativo assume a quantidade planejada para manter o fluxo rápido e a compatibilidade com o comportamento anterior.
- A lista normal mostra Planejado e Comprado nos itens concluídos e usa o total realmente pago no card.
- O histórico de preço passa a considerar compras concluídas e exibe quantidade comprada e total pago.
- Backup atualizado para **schema 5** e CSV ampliado com quantidade planejada e quantidade comprada; backups antigos migram automaticamente.
- Modelos, duplicação de listas e recorrentes sempre começam com quantidade comprada zerada.
- Tela de novidades atualizada para versionCode 27.

## 1.0.22+26 — 2026-09-26

- Adicionado **preço-alvo opcional por produto** no catálogo.
- O preço-alvo pode ser definido, editado ou removido pelo catálogo e pela tela Adicionar/Editar item.
- Cards das listas mostram **abaixo do alvo**, **dentro do alvo** ou **acima do alvo** com texto e símbolo, sem depender somente de cor.
- O **Modo compra** atualiza o status do alvo imediatamente conforme o preço é digitado.
- Histórico de preços agora compara preço atual, alvo, último preço anterior e média histórica, além de menor e maior preço.
- Backup atualizado para **schema 4**, preservando preço-alvo e unidade normalizada do alvo e mantendo compatibilidade com schemas anteriores.
- Exportação CSV inclui preço-alvo e unidade do alvo.
- Tela de novidades atualizada para versionCode 26.

## 1.0.21.1+25 — 2026-09-26

- Leitor de código passou a exigir três confirmações consecutivas antes de retornar ao editor.
- Códigos EAN-13, EAN-8 e UPC-A recebem validação de checksum antes de serem aceitos.
- Adicionada permissão `VIBRATE` e proteção para que falha no feedback háptico nunca encerre o aplicativo.
- O analisador da câmera é encerrado antes do retorno do resultado, reduzindo corridas durante o fechamento da Activity.
- Retornos sem código confirmado agora exibem mensagem clara em vez de falhar silenciosamente.
- O alvo da leitura (novo item ou item em edição) é salvo durante recriações da Activity; se o callback em memória for perdido, o editor é reaberto com o código já aplicado.
- O editor sempre mostra o número do código reconhecido, inclusive quando não encontra o nome do produto.
- A consulta opcional de produtos foi ampliada para considerar nomes genéricos e fallback por marca/quantidade.
- Tela de novidades atualizada exclusivamente com as mudanças desta versão.

## 1.0.21+24 — 2026-09-26

- Adicionada seleção manual do raio de mercados em **5 km, 10 km, 20 km, 30 km e 50 km**.
- Mantida a expansão automática para raios maiores quando a opção escolhida encontra poucos resultados.
- Supermercados agora podem ser **favoritados** e são ordenados antes dos demais resultados.
- A tela Mercados próximos ganhou lista de resultados com nome, distância, endereço quando disponível e origem do dado.
- Marcadores favoritos recebem destaque no mapa e o popup permite favoritar/desfavoritar.
- Listas criadas a partir de um mercado ficam associadas a ele; buscas futuras mostram **Abrir lista deste mercado**.
- Adicionado cache simples de resultados recentes para uso quando internet/Overpass estiverem indisponíveis.
- Mensagens de erro diferenciam falha do Overpass, falta de internet e ausência de resultados, sem atribuir automaticamente o problema à localização.
- Backup atualizado para **schema 3**, preservando associação lista/mercado, mercados favoritos e raio preferido.
- Tela de novidades atualizada exclusivamente com as mudanças desta versão.

## 1.0.20+23 — 2026-09-26

- Adicionado suporte a **produtos favoritos** no catálogo, com favoritos priorizados nas sugestões.
- Adicionada configuração de **produto recorrente** com frequência semanal, quinzenal ou mensal.
- Nova tela **Adicionar recorrentes** dentro das listas para selecionar rapidamente produtos recorrentes sem duplicar itens já presentes.
- Adicionados modelos iniciais **Cicloviagem**, **Compra do mês**, **Churrasco**, **Camping** e **Limpeza**.
- Somente o modelo **Cicloviagem** possui itens predefinidos; os demais começam vazios.
- O botão **Nova lista** agora permite escolher entre lista vazia e criação a partir de modelo.
- Qualquer lista do usuário pode ser transformada em modelo pelo menu de ações.
- Modelos personalizados podem ser excluídos sem afetar listas já criadas.
- Backup JSON atualizado para schema 2, incluindo favoritos, recorrência e modelos personalizados.
- Tela de novidades atualizada exclusivamente com as mudanças desta versão.

## 1.0.19.1+22 — 2026-09-26

- Refinada a tela **Adicionar item**, com foco especial no campo principal **Nome do produto**, que deixou de usar a apresentação comprimida e passou a ter entrada mais alta, placeholder e ajuda visual.
- O editor agora exibe melhor o estado do código de barras: sem código, reconhecido no catálogo local, código novo e identificado online para revisão.
- Quando um código não existe no catálogo local, o app tenta opcionalmente consultar a internet para preencher nome, categoria e detalhes básicos sem inventar preço.
- A leitura continua priorizando o catálogo offline; após salvar, o produto passa a abrir diretamente do banco local nas próximas leituras.
- O leitor de código ganhou botão de luz, vibração ao reconhecer e uma seleção mais estável do código central para reduzir leituras erradas.
- Tela de novidades atualizada para versionCode 22.

## 1.0.19+21 — 2026-09-26

- Implementado backup completo em JSON com `schemaVersion = 1`.
- Exportação inclui listas, itens, quantidades, unidades, preços, orçamento, estado comprado, observações, catálogo e histórico de preços derivado das listas.
- Adicionada importação pelo seletor de arquivos do Android com validação de formato e schema.
- Antes de importar, o app mostra quantidade de listas, itens, produtos do catálogo e registros de preço.
- Adicionadas opções **Mesclar**, **Substituir** e **Cancelar**, com confirmação adicional antes da substituição.
- Adicionada exportação CSV tabular de listas/itens e catálogo.
- Tema escuro atualizado de preto fechado para grafite azulado mais visível.
- Tela **Adicionar item** redesenhada com seções Produto, Compra e Detalhes, superfícies preenchidas e menos contornos.
- Fluxos existentes de catálogo e leitura offline de código de barras preservados.
- Tela de novidades atualizada para versionCode 21.

## 1.0.18.1+20 — 2026-09-26

- Corrigida a falha do workflow em `:app:checkReleaseAarMetadata`.
- A causa era o CameraX `1.6.2`, que exige compileSdk 36 e Android Gradle Plugin 8.9.1 ou superior.
- CameraX (`camera-camera2`, `camera-lifecycle` e `camera-view`) foi fixado em `1.5.3`, compatível com a base compileSdk 35 do projeto.
- Mantidos AGP 8.7.3, Kotlin 2.0.21, Java 17, targetSdk 35 e minSdk 26.
- Leitor de código de barras, catálogo local e ML Kit embarcado foram preservados.
- Adicionada validação preventiva para impedir CameraX 1.6.x sem atualização coordenada da toolchain Android.
- Tela de novidades atualizada para o novo versionCode.

## 1.0.18+19 — 2026-09-26

- Criado catálogo local/offline com produtos já cadastrados nas listas.
- Catálogo guarda nome, categoria, unidade padrão, último preço e código de barras opcional.
- Adicionada deduplicação por nome normalizado, ignorando acentos, diferenças entre maiúsculas/minúsculas e espaços extras.
- Editor de item passou a sugerir produtos conhecidos e preencher automaticamente categoria, unidade e último preço ao selecionar uma sugestão.
- Adicionado leitor de código de barras pela câmera com CameraX e modelo ML Kit embarcado no APK.
- Códigos conhecidos carregam o produto; códigos desconhecidos são associados somente aos dados digitados pelo usuário, sem consulta externa.
- Adicionada tela **Catálogo de produtos** em Configurações para consultar os dados locais.
- Alterações de preço feitas no Modo compra atualizam o último preço do catálogo.
- Tela de novidades atualizada para esta versão.

## 1.0.17+18 — 2026-09-26

- Adicionado botão **Modo compra** dentro de cada lista.
- Criada tela dedicada ao uso no supermercado, exibindo somente itens pendentes.
- Cards do modo de compra usam checkbox ampliado, nome do produto e quantidade/unidade em destaque.
- Adicionado campo de preço rápido diretamente no item; o valor digitado representa o total da quantidade e é convertido para o preço unitário interno já usado pelo app.
- Ao marcar como comprado, o item é ocultado da lista ativa e itens restantes, carrinho e orçamento são atualizados imediatamente.
- Adicionada ação **Desfazer** após a marcação de um item.
- O modo de compra reutiliza os mesmos registros da lista original, sem duplicar itens ou criar armazenamento paralelo.
- Histórico e comparação de preços permanecem compatíveis com o modelo existente.
- Tela de novidades atualizada para esta versão.

## 1.0.16+17 — 2026-09-26

- Três pontos dos cards de listas e produtos agora usam `colorOnSurface`, ficando claros e legíveis no modo escuro.
- Menu de ações da lista redesenhado em bottom sheet compacto, com Renomear, Duplicar e Excluir lado a lado.
- Exclusão de listas/itens e limpeza de comprados passam a usar confirmação padronizada, com ação destrutiva em vermelho.
- Busca da tela de detalhes movida para uma lupa no cabeçalho e recolhida por padrão.
- Resumo da lista remodelado com Estimado, Carrinho e Falta/Excedeu em destaque; valor faltante usa vermelho nos temas claro e escuro.
- Valor do carrinho recebeu o mesmo peso visual do total estimado.
- Busca de mercados agora amplia automaticamente o raio de 5 km para 15 km e 30 km quando encontra poucas opções.
- Consulta de mercados passou a considerar `shop=supermarket` e `shop=convenience` e ganhou endpoint Overpass de contingência.
- HTML do mapa foi limpo para remover script duplicado.
- Tela de novidades atualizada para esta versão.

## 1.0.15+16 — 2026-09-25

- Busca da tela inicial movida para um ícone de lupa ao lado da engrenagem.
- Campo de busca agora abre somente quando solicitado e pode ser fechado novamente pelo mesmo botão ou pelo Voltar.
- Resumo de compras compactado: listas, itens e comprados ficam na linha superior do card.
- Removida a fileira grande de estatísticas na parte inferior do card de resumo.
- Mais espaço vertical disponível para os cards das listas.
- Tela de novidades atualizada para esta versão.

## 1.0.14+15 — 2026-09-25

- Corrigido o erro de `lintVitalRelease` que impedia a geração do APK.
- Adicionadas declarações padrão em `values/colors.xml` para todas as 12 cores definidas em `values-night/colors.xml`.
- Mantido o novo fundo grafite azulado no modo escuro.
- Adicionada validação preventiva para detectar recursos de cor noturnos sem equivalente padrão antes do build.
- Tela de novidades atualizada para esta versão.

## 1.0.13+14 — 2026-09-25

- Preenchidos os preços de referência já definidos para a lista inicial Cicloviagem.
- Migração segura: itens já alterados pelo usuário não têm seus preços sobrescritos.
- Valores de itens em g/mL passam a ser exibidos de forma legível por kg/L, mantendo os subtotais corretos.
- Modo escuro deixou de usar preto puro e passou para grafite azulado, com superfícies em camadas.
- Paleta violeta ajustada para melhor contraste sobre o novo fundo escuro.
- Tela de novidades atualizada para a versão atual.

## 1.0.12+13 — 2026-09-25

- Corrigida a tela de novidades que podia aparecer mais de uma vez na mesma versão.
- A tela principal só dispara novidades em uma criação nova, evitando duplicação por recriação de Activity.
- `WhatsNewActivity` agora usa `singleTop` e flags de navegação para impedir instâncias duplicadas.
- A versão é marcada como vista somente ao tocar em **Continuar**.
- A gravação da confirmação usa `commit()` para garantir persistência antes de fechar a tela.
- Uma instância restaurada fecha automaticamente caso a versão já tenha sido confirmada.

## 1.0.11+12 — 2026-09-25

- Adicionado orçamento por lista, com valor restante ou excedido e progresso visual.
- Adicionado histórico inteligente de preços por produto: último, menor, maior, média e tendência.
- Adicionado acesso ao histórico pelo menu de cada produto.
- Corrigida a combinação visual verde + texto azul no tema escuro.
- Nova paleta violeta/lilás aplicada ao aplicativo, mapa e ícone.
- Botões preenchidos agora usam explicitamente cor principal + `colorOnPrimary`.
- Tela de novidades atualizada para esta versão.

## 1.0.10+11 — 2026-09-25

- Adicionado mapa de supermercados próximos com OpenStreetMap, localização do aparelho e consulta Overpass sem chave de API.
- Adicionada distância até cada supermercado e ação para criar uma nova lista diretamente pelo marcador.
- Reformulada a tela de detalhes da lista para ficar mais compacta e visualmente limpa.
- Cards de produtos compactados; edição por toque e ações agrupadas em menu de três pontos.
- Adicionada tela de novidades pós-atualização, exibida apenas uma vez por `versionCode`.
- `Últimas alterações` nas Configurações passa a usar a mesma fonte da tela de novidades.
- Adicionadas permissões de Internet e localização necessárias ao mapa.

## 1.0.9+10 — 2026-09-25

- Adicionada a lista inicial pronta **Cicloviagem** com 18 produtos e quantidades predefinidas.
- A sugestão é criada uma única vez e continua totalmente editável como qualquer outra lista.
- Usuários existentes recebem a sugestão sem perder ou alterar listas já cadastradas.
- Se a lista Cicloviagem for excluída, o aplicativo não a recria.
- Adicionada unidade `lata` às opções do editor de produtos.

## 1.0.8+9 — 2026-09-25

- Tela inicial redesenhada com resumo visual das compras.
- Adicionado total estimado geral, número de listas, itens e produtos comprados.
- Cards de listas modernizados com ícone, data de atualização e progresso visual.
- Cards de produtos modernizados com preço unitário, subtotal, chips e ações reorganizadas.
- Busca e estado vazio da tela inicial refinados.
- Mantido o comportamento de listas, comparação, histórico, configurações e modo escuro.

## 1.0.7+8 — 2026-09-25

- Reformulada a experiência visual das caixas de adicionar e renomear em bottom sheets modernas.
- Melhorado o formulário de item com campos mais arredondados, dropdown de categoria/unidade e ações mais claras.
- Corrigido o layout para respeitar barra de status, teclado e barra de navegação.
- Melhoradas as caixas de busca principais com visual Material 3 mais consistente.
- Mantida a estrutura por listas, comparação e histórico de preços.

## 1.0.6+7 — 2026-09-25

- Nova tela inicial baseada em listas de compras, em vez de itens soltos.
- Migração automática dos dados antigos para uma lista preservada.
- Suporte a criar, renomear, duplicar e excluir listas.
- Nova comparação entre listas e histórico de preço por produto.
- Nova área Configurações com modo Sistema/Claro/Escuro, Últimas alterações, Doação Pix e Sobre.
- Preço unitário passa a usar formatação automática em reais durante a digitação.
- Filtros da lista reorganizados para caberem em telas menores.
- `Limpar comprados` movido para ação separada dos filtros.
- Interface de criação/edição de itens modernizada com campos Material 3.

## 1.0.5+6 — 2026-09-25

- Corrigido o build que falhava ao detectar `.github/workflows/android.yml` ainda presente no repositório.
- O workflow principal agora remove automaticamente o workflow legado da branch `main`.
- A limpeza é registrada em commit com `[skip ci]`, evitando uma execução em cascata.
- Mantido somente `android-release.yml` no ZIP entregue.
- Mantida geração de APK Release assinado e publicação direta na GitHub Release.

## 1.0.4+5 — 2026-09-25

- Removido completamente o workflow redundante `Android CI`.
- Mantido apenas `Build and Release Android APK`.
- Corrigida a etapa de assinatura que falhava quando `ANDROID_KEYSTORE_BASE64` não existia.
- O workflow agora reconhece secrets `ANDROID_*`, `LISTA_MERCADO_*`, `KEYSTORE_*` e `SIGNING_*`.
- Adicionadas mensagens de erro específicas para identificar exatamente qual secret de assinatura falta.
- Adicionada validação da assinatura do APK antes da publicação.
- Adicionado `SIGNING-SECRETS.txt` com a configuração esperada, sem armazenar a chave privada no código.
- Mantida a regra: APK fora do ZIP e publicação direta do `.apk` na GitHub Release.

## 1.0.3+4 — 2026-09-25

- Corrigida a regra de entrega do GitHub Actions: o workflow agora gera APK **Release assinado** também em `push` para `main` e em execução manual, além de tags.
- O APK é publicado diretamente na GitHub Release como `Meu-Supermercado-v1.0.3.apk`.
- Removida a compilação Debug do workflow de Release.
- `android.yml` ficou restrito à validação de pull requests, evitando build duplicado na `main`.
- Adicionado `scripts/validate_workflow.py` para impedir regressões como ausência de `assembleRelease`, uso de `actions/upload-artifact` ou geração de `source.zip`.
- APK permanece fora do ZIP de código-fonte.

## 1.0.2+3 — 2026-09-25

- Corrigida a compilação Kotlin que falhava com `Unresolved reference 'BuildConfig'` em `MainActivity.kt`.
- Habilitada explicitamente a geração de `BuildConfig` com `android.buildFeatures.buildConfig = true`.
- Reforçado `scripts/validate_source.py` para detectar uso de `BuildConfig` sem a configuração necessária.
- Mantidos os workflows nativos Kotlin/Gradle corrigidos na versão anterior.

## 1.0.1+2 — 2026-09-25

- Corrigido o workflow `android-release.yml`, que tratava incorretamente o projeto Kotlin nativo como Flutter.
- Adicionado `scripts/validate_source.py`, anteriormente chamado pelo CI mas ausente.
- Separado CI de Debug (`android.yml`) do fluxo de Release por tag (`android-release.yml`).
- Release passou a obter a versão dinamicamente de `VERSION`, sem nome de APK fixo em versão antiga.
- Mantida compatibilidade com secrets genéricos `ANDROID_*` e com os secrets legados `LISTA_MERCADO_*`.
- Adicionadas validações para impedir comandos Flutter e APKs dentro do ZIP do código-fonte.

## 1.0.0+1 — 2026-09-25

- Primeira versão funcional do Lista de Mercado.
- Cadastro, edição e exclusão de itens.
- Quantidade, unidade, categoria, preço e observações.
- Busca e filtros por estado da compra.
- Totais estimado e comprado.
- Persistência local offline.
- Estrutura Android nativa Kotlin/XML modular.
- Manifestos de identidade e integração com GitHub Manager.
- Workflow de validação e release preparado para APK assinado.
