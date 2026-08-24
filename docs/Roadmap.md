# Roadmap Atualizado

## Núcleo

- [x] Models
- [x] Calculator
- [x] Validator
- [x] Classificação
- [x] Analysis
- [x] Testes automatizados (25 Python)
- [x] Port para Kotlin (`android-app/.../core/`)

## Custos

- [x] Integrar combustível
- [x] Calcular litros estimados
- [x] Calcular custo estimado
- [x] Port combustível para Kotlin
- [ ] Preparar estrutura para custo operacional

## Comportamento

- [x] Definir estados do aplicativo
- [x] Definir comportamento sem notificações
- [x] Definir ciclo de vida da interface
- [x] Ocultar interface (selo flutuante móvel, monitoramento ativo)
- [x] Fechar app (confirmação + encerrar monitoramento)
- [x] Definir Mais/Menos detalhes

## Histórico

- [x] Modelo HistoricoCorrida (Python)
- [x] Persistência local (Python/JSON)
- [x] Consulta das últimas corridas
- [x] Interface do histórico (somente corridas aceitas)
- [x] Persistência Room no Android

## Planos

- [x] Contrato Free/Beta/Pro
- [x] Controle de recursos financeiros
- [x] Histórico disponível por plano
- [x] Integração Android (ControlePlano + PresentationBuilder)

## Android

- [x] Projeto Android
- [x] MVVM
- [x] Jetpack Compose
- [ ] Room
- [x] Interface compacta / expandida
- [x] Selo flutuante com arrastar e reabrir
- [x] Overlay real sobre apps de transporte
- [x] Configurações persistidas

## Notificações

- [x] NotificationListenerService
- [x] PlatformDetector (Uber, 99, inDrive)
- [x] NotificationExtractor + CorridaParser
- [x] RideNotificationBus → AppViewModel
- [x] Tratamento de notificações não reconhecidas
- [x] Classificação oferta vs aceite (padrões provisórios até teste real)
- [x] Expiração da oferta sem gravar histórico

## Testes

- [x] Pipeline de notificações (Python + Kotlin)
- [x] Contrato AnaliseCorrida (Python + Kotlin)
- [x] ViewModel: ocultar, fechar, detalhes, histórico
- [ ] Testes instrumentados com notificações reais
- [ ] Beta fechado / RC1
