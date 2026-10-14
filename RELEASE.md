# Lista de Compras 1.0.37+41

- Correção do workflow após Build 2: o Debug, os testes unitários e o Android Lint haviam passado, porém a assinatura falhou por falta dos Secrets no GitHub.
- APK Debug entregue via **Actions > Artifacts** após cada build aprovado, independentemente da assinatura Release.
- Publicação de Release condicionada a quatro Secrets de um mesmo conjunto (preferência por `LISTA_COMPRAS_*`). Não há combinação de campos de conjuntos diferentes.
- Validação do APK Release por `apksigner verify` e verificação da keystore antes de construir.
- Permanecem o identificador `com.listadecompras.app`, as categorias SQLite com ícones, a nova criação de listas e o backup JSON schema 6 compatível com versões anteriores.

**Para distribuir APK Release atualizável:** configure os Secrets de assinatura reais no repositório conforme `SIGNING-SECRETS.txt`. Sem esses Secrets, o GitHub Actions gera apenas o APK Debug. Uma instalação assinada com Debug não é substituída pela Release sem desinstalação; faça backup antes de migrar.
