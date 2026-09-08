# Situação real das UIs da vs-2.0

Este documento descreve as telas que o APK da branch `vs-2.0` cria a partir do código atual. Não é mockup e não substitui uma captura feita no aparelho.

## Fluxo principal

1. O app inicia o monitoramento e exibe o selo/overlay.
2. Ao abrir os atalhos, a tela principal mostra o menu `Atalhos`.
3. O menu `Atalhos` contém: Histórico, Carteira, Despesas, Semáforo, Usuário, Configurar e Fechar.
4. `Histórico` e `Carteira` substituem a tela principal por uma tela nativa.
5. A seta de voltar retorna ao menu de Atalhos.

## Telas já implementadas

### Atalhos / Opções

- Painel em tela cheia, com tema claro ou escuro.
- Botão com ícone do selo no topo para voltar ao selo. O `X` não é usado para fechar o menu de Opções.
- Título `Atalhos` e subtítulo de acesso rápido.
- Lista vertical de itens com ícone, título, subtítulo e seta.
- A implementação está em `OpcoesTela.kt`; o overlay equivalente está em `AtalhosUi.kt` e `OverlayService.kt`.

### Histórico

- Cabeçalho `Histórico`, voltar, busca e lixeira.
- Abas `Todos`, `Uber`, `99` e `inDrive`.
- Navegação semanal com setas e dias da semana.
- Resumo do dia: faturamento, distância, tempo online e corridas aceitas.
- Lista dos cards de corridas aceitas.
- Toque no card abre o detalhe; lixeira exige seleção e confirmação.

### Card de detalhes da corrida

- Abre como painel inferior sobre o Histórico, com fundo escurecido atrás.
- Cabeçalho `DETALHES DA CORRIDA` e botão `X`.
- O `X` fecha somente o card de detalhes e retorna ao Histórico.
- Plataforma, data/hora, status `ACEITA`, nota e classificação.
- Embarque, destino e área reservada para mapa.
- Valor, R$/km, distância, tempo, combustível, custo, lucro e lucro/km.
- Custo total e custo operacional.

### Carteira

- Cabeçalho `Carteira` e voltar.
- O cabeçalho segue o padrão comum das abas: seta para Opções e botão selo para retornar ao selo.
- Abas `Semana`, `Mês` e `Ano`.
- Setas para avançar ou voltar o período.
- Seleção de dia/período.
- Indicadores de tempo em corridas, quilômetros e consumo estimado.
- Área financeira com receitas, despesas, lucro e margem.
- Indicadores de ganhos/km, custo/km, ganhos/hora e demais métricas abaixo, com rolagem.

## Pontos que ainda precisam de captura real

- Não há screenshots oficiais da `vs-2.0` nesta pasta.
- As imagens em `docs/imagens-telas/legado-v1` são capturas antigas e não representam a UI atual.
- Para obter as imagens definitivas, é necessário instalar o APK atual em um aparelho/emulador e capturar cada estado acima.

## Fontes no código

- `android-app/app/src/main/java/br/com/gestordriver/ui/AppScreen.kt`
- `android-app/app/src/main/java/br/com/gestordriver/ui/OpcoesTela.kt`
- `android-app/app/src/main/java/br/com/gestordriver/ui/HistoricoScreen.kt`
- `android-app/app/src/main/java/br/com/gestordriver/ui/DetalhesCorridaSheet.kt`
- `android-app/app/src/main/java/br/com/gestordriver/ui/DashboardTela.kt`