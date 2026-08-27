# Gestor Driver

Assistente Android para motoristas de aplicativo: lê a oferta na notificação, calcula **R$/KM** e custo de combustível, e ajuda a decidir em segundos — **sem aceitar a corrida pelo usuário**.

O aceite acontece no Uber, 99 ou inDrive. O Gestor Driver só identifica a oferta, mostra a análise, detecta o aceite e grava o histórico.

**Status:** Beta em calibração de campo (parser e textos reais das plataformas). Não é produto publicado na Play Store e **não é afiliado** à Uber, 99 ou inDrive.

---

## Problema

O motorista precisa aceitar ou recusar em poucos segundos, com valor, distância e tempo espalhados na tela do app de transporte. Sem um número único (R$/KM) e sem custo estimado de combustível, a decisão vira feeling.

## O que o app faz hoje

- Overlay compacto sobre o app de transporte (selo + barra horizontal).
- Análise: R$/KM, km total, tempo, combustível estimado, classificação pela **cor da borda**.
- Histórico **somente** de corridas cujo aceite foi detectado (Room).
- Configurações persistidas (veículo, consumo, preços, faixas, Maps ou Waze).
- Abre a rota no Maps/Waze quando a notificação traz origem/destino.
- Planos Free / Beta / Pro no código (a build atual inicia em **Beta**: financeiro visível). Pro (custo operacional completo) ainda não está implementado.

O parser usa padrões de texto (R$, km, min) e frases de aceite. Formatos reais das plataformas ainda precisam de calibração com o log local `notificacoes_diagnostico.txt`.

---

## Interface (contrato visual)

Compacta (sobre o app de transporte):

```text
💵 R$/KM  2,38 │ 💰 R$ 38,00 │ 🛞 KM 16 │ 🕐 MIN 24 │ ⭐ NOTA │ ⓘ
```

Toque no selo abre a **expandida em overlay (~1/3 da tela)**, sem cobrir o mapa. Expandida: mesmas métricas + **LÍQUIDO /km**, colunas Distâncias e Custos (combustível), botões **Config · Ocultar · Fechar · Histórico**. ⓘ retrai para a compacta.

No beta, **gasto** e **lucro estimado** usam só combustível (sem pneus, óleo ou manutenção). Sem oferta o app fica em monitoramento (selo). Ausência de notificação **não é erro**.

---

## Arquitetura

O domínio foi validado em Python e portado para Kotlin. O app de produção é o módulo Android.

```text
GestorDriver/
├── android-app/          # app Kotlin (Compose, Room, overlay, listener)
│   └── .../br/com/gestordriver/
│       ├── core/         # cálculo, classificação, combustível
│       ├── data/         # Room (histórico) + DataStore (config)
│       ├── notification/ # listener, parser, aceite, log diagnóstico
│       ├── overlay/      # SYSTEM_ALERT_WINDOW + foreground service
│       ├── navigation/   # Maps / Waze
│       ├── presentation/ # PresentationBuilder + planos
│       └── ui/           # Compose + ViewModels
├── core/                 # núcleo Python (referência / testes)
├── notifications/        # parsers Python (referência)
├── app/                  # demos Python
├── tests/                # 25 testes Python
└── docs/
```

Fluxo: `NotificationListenerService` → parser → `CalculadoraCorrida` → `AppViewModel` → overlay / histórico.

---

## Stack

| Camada | Tecnologia |
| --- | --- |
| App | Kotlin, Jetpack Compose, MVVM |
| Dados | Room, DataStore Preferences |
| Sistema | NotificationListenerService, Foreground Service, overlay |
| Domínio de referência | Python 3 (cálculo e testes independentes do Android) |

`minSdk` 30 · `applicationId` `br.com.gestordriver`

---

## Como executar

### Android (portfólio / uso no celular)

1. Abra a pasta `android-app` no Android Studio.
2. JDK 17+, instale no aparelho (API 30+) ou emulador.
3. Conceda **acesso a notificações** e **exibir sobre outros apps**.
4. No Android 13+, aceite a permissão de notificação do Gestor.
5. Guia de teste em campo: [`docs/ROTEIRO_BETA.md`](docs/ROTEIRO_BETA.md).

### Testes Python (núcleo)

```bash
pip install -r requirements.txt
python -m pytest tests/ -v
```

Ou: `python -m unittest discover -s tests -p "test_*.py"`

### Testes Android

No Android Studio, rode os unit tests do módulo `app` (`:app:testDebugUnitTest`). Cobre ViewModel, parser, classificação, persistência de config e navegação. Testes instrumentados com notificações reais ainda não existem.

---

## Planos (produto)

| | Free | Beta (atual) | Pro (planejado) |
| --- | --- | --- | --- |
| Valor, km, tempo, nota, cor | sim | sim | sim |
| R$/KM, combustível, gasto | oculto | visível | visível |
| Histórico de aceites | sim | sim | sim + operacional |

---

## Documentação

| Arquivo | Função |
| --- | --- |
| [docs/ROTEIRO_BETA.md](docs/ROTEIRO_BETA.md) | Como testar no celular agora |
| [docs/Gestor_Driver_MVP_Especificacao_v1.0.md](docs/Gestor_Driver_MVP_Especificacao_v1.0.md) | Regras de negócio do MVP |
| [docs/Roadmap.md](docs/Roadmap.md) | O que está feito / pendente |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Camadas |
| [docs/TESTING_STRATEGY.md](docs/TESTING_STRATEGY.md) | Estratégia de testes |
| [docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) | Ambiente de desenvolvimento |

Documentos de sprint e etapas (`EXECUTION_BOARD`, `CLASSIFICACAO_ETAPAS`, `SPRINT2_RELEASE_NOTES`) são histórico de desenvolvimento, não o estado atual.

---

## O que ainda não está no portfólio como “pronto”

- Calibração do parser com notificações reais (Uber / 99 / inDrive).
- Testes instrumentados em dispositivo.
- Versão Pro (pneus, óleo, depreciação, relatórios).
- Publicação na loja.

---

Projeto acadêmico / portfólio. Informação rápida para uma decisão melhor.
