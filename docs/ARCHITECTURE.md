# Arquitetura do projeto

## Objetivo

Separar regras de negócio, persistência e integração com o sistema Android para o app poder evoluir sem misturar cálculo com UI.

## Visão geral

| Camada | Onde | Papel |
| --- | --- | --- |
| Core / domínio | `core/` (Python) e `android-app/.../core/` (Kotlin) | Corrida, R$/KM, classificação, combustível |
| Apresentação | `presentation/`, `model/`, Compose em `ui/` | Montar o que a tela mostra (inclui Free/Beta/Pro) |
| Estado | `AppViewModel`, `ConfiguracoesViewModel` | Oferta atual, histórico, overlay, configuração |
| Dados | `data/` | Room (histórico aceito), DataStore (config do motorista) |
| Sistema | `notification/`, `overlay/` | Listener, parser, overlay, foreground service |
| Navegação | `navigation/` | Intent Maps / Waze |

O Python não é o app instalado: é a referência testável do domínio. O entregável é `android-app/`.

## Fluxo em tempo de execução

```text
Uber / 99 / inDrive
        ↓
NotificationListenerService
        ↓
Parser + classificador (oferta / aceite / ignorar)
        ↓
CalculadoraCorrida (usa config persistida)
        ↓
AppViewModel
        ↓
    ┌───┴────┐
Overlay    Room (só no aceite)
```

## Diretrizes

- O Gestor Driver não aceita a corrida; só observa a plataforma.
- Histórico não recalcula corridas antigas se o preço do combustível mudar.
- Parser e aceite devem evoluir com o log diagnóstico, sem inventar corrida quando o texto não fecha.
