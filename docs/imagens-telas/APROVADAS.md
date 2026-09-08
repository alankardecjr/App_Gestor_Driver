# Telas aprovadas da vs-2.0

Registro das telas aprovadas até 07/09/2026 na branch `vs-2.0`. A UI oficial é a implementada no código Android; não usar imagens da pasta `legado-v1` como referência da versão 2.0.

**Baseline congelado:** telas e paleta descritas no código atual ficam como referência oficial para a criação das próximas telas. Screenshots da `vs-2.0` ainda devem ser capturados no APK/aparelho e adicionados quando disponíveis.

- Atalhos
- Opções
- Histórico
- Card de detalhes da corrida
- Carteira - modo Semana
- Carteira - modo Mês
- Carteira - modo Ano

## Origem da UI

- Atalhos e Opções: `android-app/app/src/main/java/br/com/gestordriver/ui/OpcoesTela.kt`
- Histórico: `android-app/app/src/main/java/br/com/gestordriver/ui/HistoricoScreen.kt`
- Detalhes da corrida: `android-app/app/src/main/java/br/com/gestordriver/ui/DetalhesCorridaSheet.kt`
- Carteira Semana/Mês/Ano: `android-app/app/src/main/java/br/com/gestordriver/ui/DashboardTela.kt`
- Roteamento das telas: `android-app/app/src/main/java/br/com/gestordriver/ui/AppScreen.kt`
- Paleta das telas nativas: `android-app/app/src/main/java/br/com/gestordriver/ui/theme/PaletaApp.kt`
- Paleta do menu Atalhos/Opções: `android-app/app/src/main/java/br/com/gestordriver/overlay/AtalhosUi.kt`

As imagens reais da UI final da `vs-2.0` ainda precisam ser capturadas durante a execução do APK. As capturas antigas foram preservadas em `legado-v1` apenas para histórico.
