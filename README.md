# Gestor Driver

Assistente Android para motoristas de Uber, 99 e inDrive. Lê a oferta (notificação + tela), calcula **R$/KM** e o custo de combustível, e mostra a decisão em um overlay sobre o mapa. **Não aceita a corrida** — o aceite continua no app da plataforma.

**Status:** Beta `1.1.8` · sem Play Store · não afiliado às plataformas.

| Plano | O que mostra |
| --- | --- |
| **Beta (agora)** | Valor, km, tempo, nota, borda + R$/KM, litros, gasto e lucro de combustível |
| **Pro (depois)** | Beta + custo operacional, R$/km líquido, relatórios |
| **Free (loja)** | Mesma UI; **oculta** R$/KM, litros, gasto e lucro |

---

## Problema e solução

O motorista tem poucos segundos e os números estão espalhados na tela da plataforma. O Gestor junta **R$/KM + classificação por cor + custo do combustível atual** sem tapar o mapa.

Fluxo de uso: **selo** → **barra compacta** (oferta) → **expandida** (distâncias e custos). Histórico e configuração abrem **abaixo** da expandida.

---

## Primeira abertura

1. Verifica permissões obrigatórias: notificações, sobrepor, acessibilidade (leitura do card), bateria.
2. Primeiro uso: pede **conta Google ou e-mail** (identidade local; sem sync).
3. Tutorial em janelas curtas (selo, cabeçalho, expandida, botões, config, histórico) com **SEGUIR** ou **PULAR**.
4. Inicia o monitoramento (selo sobre o mapa).

Se alguma permissão cair depois, o app volta ao passo de permissões antes de monitorar.

---

## Stack e arquitetura

Kotlin · Jetpack Compose · MVVM · Room (histórico de aceites) · DataStore (config + onboarding) · NotificationListener + Accessibility/OCR · overlay (`SYSTEM_ALERT_WINDOW`) · foreground service.

```text
Plataforma → listener / leitura de tela → parser → CalculadoraCorrida → overlay
                                                      ↓
                                              Room (só no aceite)
```

O núcleo de cálculo também existe em Python (`core/`, `tests/`) como referência. O entregável é `android-app/`.

`minSdk` 30 · `applicationId` `br.com.gestordriver`

---

## Como rodar

1. Abra `android-app` no Android Studio (JDK 17+).
2. Instale em aparelho físico Android 11+ (overlay e listener no emulador são limitados).
3. Na primeira abertura, complete permissões, conta e tutorial (ou pule o tutorial).
4. Teste de campo: [`docs/ROTEIRO_BETA.md`](docs/ROTEIRO_BETA.md).

```bash
# núcleo (opcional)
pip install -r requirements.txt && python -m pytest tests/ -v

# Android
cd android-app && ./gradlew :app:testDebugUnitTest
```

---

## Documentação

| Documento | Para quem |
| --- | --- |
| Este README | Visão do produto e como abrir o projeto |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Camadas e fluxo de runtime |
| [docs/REGRAS_NEGOCIO.md](docs/REGRAS_NEGOCIO.md) | Regras oficiais (selo, aceite, histórico, config) |
| [docs/ROTEIRO_BETA.md](docs/ROTEIRO_BETA.md) | Teste no celular |
| [docs/Roadmap.md](docs/Roadmap.md) | Feito / próximo |
| [docs/TESTING_STRATEGY.md](docs/TESTING_STRATEGY.md) | O que os testes cobrem |

Notas de sprint antigas não descrevem o estado atual.

**Ainda em aberto:** calibrar parser/aceite com ofertas reais; Pro; Free na loja; testes instrumentados.

Projeto de portfólio. O Gestor observa a plataforma; a decisão de aceitar é do motorista.
