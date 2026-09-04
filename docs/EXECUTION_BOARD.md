# Execution Board — Gestor Driver

Board de acompanhamento. O README e o [Roadmap](Roadmap.md) descrevem o estado do produto; este arquivo registra o que já saiu da fila.

## To Do

1. **Bloco C** — teste de rua Pro no SM-A145M ([ROTEIRO_PRO.md](ROTEIRO_PRO.md)).
2. Calibrar parser e aceite com notificações reais (`notificacoes_diagnostico.txt`).
3. Testes instrumentados no dispositivo (depois da calibração).
4. Cobrança / Play Store (Free na loja = 🔒; Pro liberada).

Não misturar `vs-2.0` no Beta congelado `main` 1.1.10. Commit / push só com pedido.

## Doing

1. Fechamento Pro 2.0 — telas A/B no código; próximo = rua.

## Done

1. Sprint 2: pipeline de notificações em Python + testes.
2. Classificação oficial, `AnaliseCorrida`, combustível, planos Free/Beta(=Pro)/Pro.
3. Núcleo portado para Kotlin.
4. NotificationListenerService + overlay + selo.
5. Room (histórico de aceites) e DataStore (configurações).
6. Detecção de aceite (padrões) + expiração sem gravar histórico.
7. Navegação Maps/Waze a partir de endereços extraídos.
8. Log local de notificações para calibração.
9. README e Roadmap alinhados ao código (portfólio).
10. Faixas de classificação (Beta 4 faixas; Pro 3 faixas).
11. UI Beta congelada `1.1.10` (02/09/2026).
12. Pro no código: custos operacionais, dashboard, tema, alerta óleo, abastecimento com pergunta, Free com cadeados.
