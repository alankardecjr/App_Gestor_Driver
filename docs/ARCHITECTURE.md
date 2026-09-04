# Arquitetura do projeto

## Objetivo

Separar regras de negócio, persistência e integração com o sistema Android para o app poder evoluir sem misturar cálculo com UI.

## Visão geral

| Camada | Onde | Papel |
| --- | --- | --- |
| Core / domínio | `core/` (Python) e `android-app/.../core/` (Kotlin) | Corrida, R$/KM, classificação, combustível + custos operacionais |
| Apresentação | `presentation/`, `model/`, overlay | O que a tela mostra; Free oculta números; Pro libera tudo (`PlanoAcesso.BETA` = Pro no código) |
| Estado | `AppViewModel`, `ConfiguracoesViewModel` | Oferta atual, histórico, overlay, configuração |
| Dados | `data/` | Room (histórico aceito), DataStore (config do motorista) |
| Sistema | `notification/`, `overlay/` | Listener, parser, overlay, foreground service |
| Navegação | `navigation/` | Intent Maps / Waze |

O Python não é o app instalado: é a referência testável do domínio. O entregável é `android-app/`.

## Fluxo em tempo de execução

```text
Primeira abertura
  permissões → conta (Google/e-mail) → tutorial (seguir/pular) → selo
        ↓
Uber / 99 / inDrive
        ↓
NotificationListener + leitura de tela (OCR se preciso)
        ↓
Parser + classificador (oferta / aceite / ignorar)
        ↓
CalculadoraCorrida (config persistida)
        ↓
AppViewModel → overlay  |  Room só no aceite
```

## Diretrizes

- O Gestor Driver não aceita a corrida; só observa a plataforma.
- Custo da oferta (Pro): combustível atual + óleo + pneus + IPVA + seguro. Histórico não recalcula se o preço mudar depois.
- Produto: Free (demo 🔒) + Pro (paga). Beta `1.1.10` congelado em `main`; linha ativa `vs-2.0`.
- Parser e aceite devem evoluir com o log diagnóstico, sem inventar corrida quando o texto não fecha.
