## Validação 1.0.35+39 — identidade Lista de Compras

- [x] Código-fonte conferido para localizar nomes públicos antigos e identificadores técnicos persistentes.
- [x] Nome do Android centralizado em `@string/app_name`, atualizado também na tela Sobre.
- [x] Metadados, VERSION, Gradle, README, CHANGELOG, RELEASE e Novidades sincronizados.
- [x] Workflow e novos nomes sugeridos de JSON/CSV atualizados para Lista de Compras.
- [x] `applicationId`, namespace, SharedPreferences, formato de backup schema 5 e Secrets legados preservados.
- [ ] Compilação e Android Lint completos, release assinada e testes físicos devem ser confirmados no GitHub Actions/aparelho.

## Validação 1.0.34+38 — correção do Build 26

- [x] Log real do Build 26 analisado: `:app:assembleDebug` e `:app:testDebugUnitTest` passaram; `:app:lintDebug` reportou 1 erro e 272 avisos.
- [x] Acesso experimental `imageProxy.image` isolado em `readMediaImage` com `@androidx.annotation.OptIn` diretamente no método de leitura.
- [x] Removida a anotação `kotlin.OptIn` anterior que não resolveu o erro do Lint.
- [x] Teste estático de regressão do Build 26 adicionado e guard do Build 25 atualizado; Lint não foi suprimido ou desabilitado.
- [x] VERSION, Gradle, app_identity, github-manager, README, CHANGELOG, RELEASE e novidades sincronizados.
- [ ] Compilação Android/Lint/Release e testes em dispositivo ainda devem ser validados no CI.

## Validação 1.0.33+37 — correção do Build 25

- [x] Log real do Build 25 analisado: `:app:assembleDebug` e `:app:testDebugUnitTest` passaram, `:app:lintDebug` reportou 13 erros e 272 avisos.
- [x] Um `UnsafeOptInUsageError` no scanner: inclusão de opt-in CameraX no escopo do método.
- [x] Doze erros `UseAppTint`: seis layouts migrados de `android:tint` para `app:tint`, mantendo valores.
- [x] Teste de regressão `scripts/validate_lint_build25.py` incluído no workflow (sem suprimir avisos ou falhas).
- [x] `VERSION`, Gradle, app_identity, github-manager, README, CHANGELOG, RELEASE e WhatsNewContent sincronizados.
- [ ] `:app:lintDebug`, `:app:assembleRelease` e testes em aparelho precisam rodar no GitHub Actions; dependências Android SDK/Gradle não disponíveis neste ambiente.
- [ ] Os 272 avisos do Lint devem ser avaliados separadamente, mas não foram a causa do bloqueio neste build.

## Validação 1.0.32+36 — correção do Build 24

- [x] Analisado o log real: `:app:testDebugUnitTest` e `:app:assembleDebug` passaram; `:app:lintDebug` falhou com 15 erros e 270 avisos. O ZIP expõe apenas o primeiro erro.
- [x] Provedores de localização acessados somente após verificações explícitas de permissão e com captura de `SecurityException`.
- [x] Workflow continua executando testes, build Debug e Lint obrigatório; novo relatório de erros integrais no console quando falhar.
- [x] Versionamento sequencial atualizado, preservando o schema 5 e `applicationId`.
- [ ] `:app:lintDebug` e `:app:assembleRelease` precisam de novo CI, pois o SDK Android/Gradle não está disponível no ambiente local.
- [ ] Outros 14 erros reportados pelo Lint não foram detalhados no ZIP e dependem do relatório completo; não presumir que todos estão corrigidos.

## Validação 1.0.31+35 — conclusão planejada da repaginação

- [x] Controle de versões 1.0.31+35 e metadados de app/release sincronizados.
- [x] Adaptadores de listas, produtos, catálogo e comparações convertidos para atualizações diferenciais.
- [x] Snapshots de conteúdo mutável e uso dos registros vigentes nas ações com escrita.
- [x] Política restritiva para URLs do mapa e hashes SRI nos recursos Leaflet.
- [x] Testes JUnit adicionados ao código-fonte e etapa Debug/teste/lint adicionada ao workflow.
- [x] Testes Kotlin offline de URLs, totais e valores, além de validações estáticas locais.
- [ ] `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:lintDebug` e `:app:assembleRelease`: **não executados localmente**; exigem Android SDK/Gradle no CI.
- [ ] Testes instrumentados/manuais com listas grandes, rolagem do Modo compra, scanner, importação/restauração e mapa online: **pendentes**.
- [ ] Integridade SRI do CDN: hashes conhecidos para Leaflet 1.9.4 usados, porém sem teste online neste ambiente; confirmar mapa no aparelho.

## Validação 1.0.30+34

Validadores locais executados. Compilação Gradle/Android e teste em aparelho permanecem pendentes.

## Validação 1.0.29+33

Validadores locais executados. Compilação Gradle/Android e teste em aparelho permanecem pendentes.

## 1.0.28+32 — correção AAPT2 do Build 22

- [x] Identificado erro de vinculação `resource style/Widget.ListaMercado not found` em `:app:processReleaseResources`.
- [x] Corrigido estilo base com pai Material 3 explícito.
- [x] Adicionado teste preventivo `scripts/validate_resource_styles.py` e execução no GitHub Actions.
- [x] Seis validadores Python executados sem falha (versão, fontes, workflow, reformulação, vínculos Kotlin/XML, estilos).
- [x] Teste negativo: um estilo sem `parent` é rejeitado pelo novo validador.
- [x] 12 testes Kotlin de entradas de valores aprovados.
- [x] 47 fontes Kotlin analisadas pelo parser PSI sem erros sintáticos; 54 arquivos XML bem formados.
- [ ] Compilação Gradle/Android e teste no aparelho: pendentes de validação no ambiente com SDK Android.

## 1.0.27+31 — validação

Detalhes, filtros e cartões redesenhados; sem alterações de armazenamento e cálculo.

- [x] Validadores estruturais, de metadados e workflow
- [x] Validação estática dos vínculos de IDs entre XML e Kotlin; 54 XMLs bem formados
- [x] Sintaxe Kotlin verificada com parser PSI (sem erros nos arquivos analisados)
- [x] 12 testes Kotlin de entrada monetária (herdados)
- [ ] Compilação Gradle/Android e testes visuais em dispositivo físico pendentes (SDK Android indisponível neste ambiente)

## 1.0.25+29 — validação

Sistema de estilos reutilizáveis aplicado às telas existentes, mantendo compatibilidade e todas as correções de 1.0.24+28.

## 1.0.24+28 — validação

Correções de integridade de dados, confirmação de preço, busca e orçamento; schema 5 inalterado.

# Validação — 1.0.23+27

- [x] versionName `1.0.23`
- [x] versionCode `27`
- [x] VERSION `1.0.23+27`
- [x] app_identity.json e github-manager.json sincronizados
- [x] WhatsNewContent sincronizado com versionCode 27
- [x] ShoppingItem separa quantidade planejada e quantidade comprada
- [x] Estimativa usa quantidade planejada
- [x] Carrinho usa quantidade realmente comprada
- [x] Modo compra permite editar quantidade comprada e total pago
- [x] Marcação rápida assume a quantidade planejada quando nenhuma quantidade real foi informada
- [x] Lista normal mostra Planejado/Comprado em itens concluídos
- [x] Histórico mostra quantidade comprada e total pago e usa compras concluídas nos indicadores
- [x] Backup JSON atualizado para schema 5 e compatível com schemas anteriores
- [x] CSV inclui quantidade planejada e quantidade comprada
- [x] Modelos, duplicações e recorrentes zeram quantidade comprada
- [x] Funcionalidades anteriores de scanner, preço-alvo, backup, recorrentes, modelos e mercados preservadas
- [x] compileSdk 35, targetSdk 35, minSdk 26, AGP 8.7.3, Kotlin 2.0.21 e Java/JVM 17 preservados
- [x] Workflow principal sem actions/upload-artifact e sem source.zip
- [ ] Build Gradle completo depende do GitHub Actions porque este ambiente não possui Android SDK/Gradle configurado.
- [x] 50 XMLs analisados e bem formados
- [x] Smoke test Kotlin confirmou: planejado 5 × R$ 4 = R$ 20 estimados; comprado 3 × R$ 4 = R$ 12 no carrinho
