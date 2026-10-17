# Validação 1.0.46+50

- Executar `scripts/validate_list_compact.py` (orçamento clicável, filtros compactos e funcionais, indicação de link junto da categoria e supressão de "sem parcelas").
- Executar todos os verificadores do diretório `scripts/` e o teste de assinatura (sem revelar segredos).
- Verificar os recursos XML e as referências de IDs dos layouts no Kotlin.
- Fluxos manuais: tocar em qualquer ponto do orçamento; selecionar Todos/Pendentes/Comprados em um toque; cartões com e sem foto; compra só à vista vs parcelada; item com e sem URL; categorias longas em aparelhos pequenos; confirmar que notas ainda aparecem quando existirem.
- Compilação Android, lint e testes em aparelho pendentes de GitHub Actions; esta entrega contém código-fonte e não APK.
- Dados e backup schema 9 preservados, sem necessidade de limpar dados locais.
