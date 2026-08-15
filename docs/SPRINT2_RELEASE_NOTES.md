# Sprint 2 - Sistema de Leitura de Corridas

## Resumo

A Sprint 2 transforma o Gestor Driver em um pipeline de processamento de notificacoes orientado a produto.
A inteligencia permanece desacoplada do Android e pode ser validada localmente em Python.

Fluxo implementado:

NotificationData -> PlatformDetector -> Parser por plataforma -> Validator -> Corrida -> CalculadoraCorrida

## Entregas

- framework interno de notificacoes no pacote notifications
- excecoes de dominio para falhas explicitas de parse
- extracao robusta de valor, distancia e tempo
- suporte a distancia em km e metros (com conversao para km)
- suporte a variacoes de tempo: min, mins, minuto, minutos
- parsers para Uber, 99 e inDrive
- simulador de notificacoes para testes locais
- demo executavel de ponta a ponta
- documentacao dos modulos core para portfólio
- atualizacao da estrategia de testes e README

## Qualidade

- testes unitarios: 7 casos automatizados
- validacao de erro para plataforma nao suportada
- execucao da demo confirmada em runtime

## Arquivos de destaque

- notifications/parser.py
- notifications/extractor.py
- notifications/platform_detector.py
- notifications/simulator.py
- tests/test_notifications_pipeline.py
- app/notifications_demo.py

## Proximos passos sugeridos

- incluir fixtures com notificacoes reais anonimizadas
- ampliar cobertura para mensagens truncadas e variacoes regionais
- integrar o pipeline ao NotificationListenerService no Android
