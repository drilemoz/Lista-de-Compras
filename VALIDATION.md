# Validação 1.0.44+48

- Executar `scripts/validate_product_details_photos.py` e os validadores de versão, UI, Android, backup e assinatura existentes.
- Regressão Android necessária: tocar card abre detalhes sem teclado; Editar permite salvar; mudar foto pela galeria/câmera; remoção; links principal e alternativo; sem navegador instalado; listas supermercado.
- Backup: ZIP contém `backup.json` schema 9 e `images/*.jpg`, limitado a 60 MiB descompactados e validação de nomes; importação confirma mesclar/substituir. JSON schema 8 e anteriores continua sendo aceito.
- Verificar rotação durante câmera, troca de aplicativo, fotos removidas, links inválidos e uso do cache interno.
- Build Gradle e testes instrumentados do aparelho permanecem pendentes.
