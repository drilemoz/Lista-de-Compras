# Lista de Compras 1.0.39+43

- Redesenho do editor de produtos com cabeçalho e botões fixos; menos rolagem, campo opcional recolhível.
- Escolha de ícones em grade e edição de ícones de categorias existentes, persistidos em SQLite.
- Calculadora para promoções com várias unidades: 2 por R$ 14,79 indica R$ 7,40 por unidade, enquanto o subtotal preserva R$ 14,79.
- Campo de nome do produto com espaçamento interno maior para separar melhor rótulo e texto digitado.
- Biblioteca de ícones ampliada com opções para tecnologia, carros, bicicleta, ferramentas, roupas, games, escritório e outras áreas.
- Diálogo de ícones permanece em grade e agora é rolável para acomodar as novas opções.
- Dados da promoção seguem gravados em listas, modelos e backup JSON schema 7, com importação dos schemas anteriores; CSV com colunas de combo.
- Compatível com applicationId e Secrets da versão anterior.

**A validar:** compilar o APK em GitHub Actions e testar o formulário e cálculos num aparelho Android.
