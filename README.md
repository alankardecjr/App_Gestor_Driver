# Gestor Driver

Assistente Android para motoristas de aplicativo: lê a oferta na notificação, calcula **R$/KM** e custo de combustível, e ajuda a decidir em segundos — **sem aceitar a corrida pelo usuário**.

O aceite acontece no Uber, 99 ou inDrive. O Gestor Driver só identifica a oferta, mostra a análise, detecta o aceite e grava o histórico.

**Status:** **Beta** — calibração de campo (parser e textos reais das plataformas). Sem Play Store. Não é afiliado à Uber, 99 ou inDrive.

**Versões do produto**

1. **Beta (agora)** — cálculos visíveis: R$/KM, consumo estimado, gasto e lucro só de combustível.
2. **Pro (depois)** — mesmos cálculos + custo operacional (pneus, óleo, manutenção, depreciação), R$/km líquido, relatórios.
3. **Free (lançamento na loja)** — mesma interface e histórico; **não mostra os cálculos** (R$/KM, combustível, gasto, lucro). Valor, km, tempo, nota e cor da classificação continuam visíveis.

---

## Problema

O motorista precisa aceitar ou recusar em poucos segundos, com valor, distância e tempo espalhados na tela do app de transporte. Sem um número único (R$/KM) e sem custo estimado de combustível, a decisão vira feeling.

## O que o app faz hoje

- Overlay sobre o app de transporte: **selo** (estado principal) → **compacta** (oferta) → **expandida** (~1/3 da tela). Histórico e configuração abrem **embaixo da expandida**, não como tela cheia.
- Análise (Beta): R$/KM, valor, distância, tempo, nota; custos pelo **combustível atual** (km/L + preço do litro desse combustível).
- Classificação pela **borda da corrida atual** (🔴 ruim, 🟠 regular, 🟢 boa, 🔵 ótima). Padrão: Ruim até 1,19 · Regular 1,20–1,59 · Boa 1,60–1,99 · Ótima a partir de 2,00. Histórico: borda neutra + marcador colorido.
- Histórico **somente** de corridas cujo aceite foi detectado (Room), com abas Uber / 99 / inDrive.
- Configurações persistidas: veículo, consumo, combustível atual, preços (aba APP), faixas, Maps ou Waze. Abre o app de navegação escolhido (no Pro o trajeto da corrida aceita poderá aparecer no histórico).
- A build inicia em **Beta**. Pro e Free existem no código de planos; Free só entra no lançamento.

O parser usa padrões de texto (R$, km, min) e frases de aceite. Formatos reais das plataformas ainda precisam de calibração com o log local `notificacoes_diagnostico.txt`.

---

## Interface (contrato visual)

Compacta (sobre o mapa; só o cabeçalho):

```text
💵 R$/KM │ 💰 VALOR │ 🛞 DIST. │ 🕐 TEMPO │ ⭐ NOTA │ ℹ️ ⬇️
```

`⬇️` abre a expandida. Recolher (`⬆️`) volta à compacta. Com a compacta visível, um toque **fora** das janelas do Gestor Driver (no mapa ou nos controles da plataforma) recolhe **na hora** para o selo — assim o motorista alcança o recusar da 99, que fica na mesma faixa da barra. Sem esse toque, a compacta ainda volta ao selo **após 5 s**. Aceite detectado também vai ao selo.

Expandida (~1/3 da tela, mapa visível): o mesmo cabeçalho + colunas **Distâncias** e **Custos (combustível)** + botões **Fechar · Config · Ocultar · Histórico**. Histórico e Config são painéis overlay abaixo da expandida (exclusivos).

Gasto e lucro no Beta usam **só combustível atual**. Sem oferta = monitoramento no selo. Ausência de notificação **não é erro**.

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

Ordem de trabalho: **Beta → Pro → Free no lançamento**.

| | Free (loja) | Beta (agora) | Pro (depois) |
| --- | --- | --- | --- |
| Valor, km, tempo, nota, borda | sim | sim | sim |
| R$/KM, litros, gasto, lucro | **oculto** | visível | visível |
| Custo operacional / R$ líquido / relatórios | não | não | sim |
| Histórico de aceites | sim | sim | sim + operacional |

O cálculo existe em todos os planos; no Free a interface **não exibe** os números financeiros.

---

## Documentação

| Arquivo | Função |
| --- | --- |
| [docs/REGRAS_NEGOCIO.md](docs/REGRAS_NEGOCIO.md) | Fluxograma e regras oficiais (selo, aceite, histórico, cores) |
| [docs/ROTEIRO_BETA.md](docs/ROTEIRO_BETA.md) | Como testar no celular agora |
| [docs/Gestor_Driver_MVP_Especificacao_v1.0.md](docs/Gestor_Driver_MVP_Especificacao_v1.0.md) | Regras de negócio do MVP |
| [docs/Roadmap.md](docs/Roadmap.md) | O que está feito / pendente |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Camadas |
| [docs/TESTING_STRATEGY.md](docs/TESTING_STRATEGY.md) | Estratégia de testes |
| [docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) | Ambiente de desenvolvimento |

Documentos de sprint e etapas (`EXECUTION_BOARD`, `CLASSIFICACAO_ETAPAS`, `SPRINT2_RELEASE_NOTES`) são histórico de desenvolvimento, não o estado atual.

---

## O que ainda não está pronto

- Calibração do parser com notificações reais (Uber / 99 / inDrive).
- Versão **Pro**.
- Lançamento **Free** na loja (cálculos ocultos).
- Testes instrumentados em dispositivo.

---

Projeto acadêmico / portfólio. Informação rápida para uma decisão melhor.
