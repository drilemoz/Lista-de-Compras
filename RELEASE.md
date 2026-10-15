# Lista de Compras 1.0.43+47

- Corrigido Build 8: compilação Kotlin falhou com três `Unresolved reference 'orEmpty'` em `ShoppingFieldsForm.kt:143` ao validar loja, variante e loja alternativa.
- A leitura agora converte o texto editável (`Editable`) em `String` antes de verificar o tamanho máximo de 120 caracteres.
- Validador de regressão do modo Shopping atualizado para identificar o padrão inválido.
- Mantidos Supermercado e Shopping, parcelamento, comparações, backup schema 8, identificador Android e assinatura existentes.

**Pendente:** executar novamente o workflow GitHub Actions para confirmar `:app:compileDebugKotlin`, testes, Lint, APK Debug e Release assinada.
