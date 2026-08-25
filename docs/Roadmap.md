# Roadmap

Estado alinhado ao código em `android-app/` (Beta em calibração de campo).

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
- [ ] Custo operacional completo (Pro)

## Comportamento

- [x] Estados do aplicativo
- [x] Sem notificações = monitoramento, não erro
- [x] Mais / menos detalhes
- [x] Ocultar (selo) e fechar app
- [x] Abrir rota no Maps ou Waze (quando há endereço na notificação)

## Histórico

- [x] Modelo e persistência Python
- [x] Interface (somente corridas aceitas)
- [x] Room no Android

## Planos

- [x] Contrato Free / Beta / Pro no código
- [x] Controle de recursos na apresentação
- [ ] Seletor de plano na UI (hoje a build inicia em Beta)

## Android

- [x] Compose + MVVM
- [x] Room (histórico) + DataStore (configurações)
- [x] Overlay real (`SYSTEM_ALERT_WINDOW`) + foreground service
- [x] Selo flutuante (arrastar / reabrir)
- [x] Tela de configurações persistida

## Notificações

- [x] NotificationListenerService
- [x] Detector Uber / 99 / inDrive (package names)
- [x] Parser genérico (R$, km, min) + extração de endereços quando o texto traz Origem/Destino
- [x] Oferta vs aceite (padrões; calibrar com log real)
- [x] Expiração sem gravar histórico
- [x] Log diagnóstico local

## Testes e release

- [x] Unitários Python + Kotlin
- [ ] Instrumentados com notificações reais
- [ ] Calibração de campo (parser / aceite)
- [ ] RC1 / publicação
