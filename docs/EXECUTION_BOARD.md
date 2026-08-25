# Execution Board — Gestor Driver

Board de acompanhamento. O README e o [Roadmap](Roadmap.md) descrevem o estado do produto; este arquivo registra o que já saiu da fila.

## To Do

1. Calibrar parser e aceite com notificações reais (usar `notificacoes_diagnostico.txt`).
2. Expandir testes do pipeline (textos truncados, variações regionais).
3. Testes instrumentados no dispositivo.
4. Observabilidade e consumo de bateria em uso real.
5. Material de demo para portfólio (gravação de tela após calibração).
6. RC1 quando o piloto no celular estiver estável.

## Doing

1. Beta em teste real pelo autor (ver [ROTEIRO_BETA.md](ROTEIRO_BETA.md)).

## Done

1. Sprint 2: pipeline de notificações em Python + testes.
2. Classificação oficial, `AnaliseCorrida`, combustível, planos Free/Beta/Pro.
3. Núcleo portado para Kotlin.
4. NotificationListenerService + overlay + selo.
5. Room (histórico de aceites) e DataStore (configurações).
6. Detecção de aceite (padrões) + expiração sem gravar histórico.
7. Navegação Maps/Waze a partir de endereços extraídos.
8. Log local de notificações para calibração.
9. README e Roadmap alinhados ao código (portfólio).

## Critérios da Sprint 3 (código)

Atendidos no app: listener, fallback sem crash para plataforma desconhecida, testes unitários do domínio. Pendente: confirmação com notificação **real** no aparelho.
