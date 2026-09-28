# Gestor Driver

Assistente Android para motoristas de Uber, 99 e inDrive. Lê a oferta (notificação + tela), calcula **R$/KM** e o custo da corrida, e mostra a decisão em um overlay sobre o mapa. **Não aceita a corrida** — o aceite continua no app da plataforma.

**Linha ativa:** VS `2.3` (versionCode 22) · **Beta congelado:** `1.1.10` · sem Play Store · não afiliado às plataformas.

| Plano | Papel |
| --- | --- |
| **Free** | Demo grátis. Mesmas telas; R$/KM, litros, gasto, lucro e dashboard **ocultos** (🔒). |
| **Pro** | Paga (quando estiver ok). Tudo liberado: combustível + óleo/pneu/IPVA/seguro, dashboard, tema. A antiga Beta vira esta linha. |

Detalhe Free vs Pro: [`docs/REGRAS_NEGOCIO.md`](docs/REGRAS_NEGOCIO.md) §38 · fechamento: [`docs/ROTEIRO_PRO.md`](docs/ROTEIRO_PRO.md).

---

## Problema e solução

O motorista tem poucos segundos e os números estão espalhados na tela da plataforma. O Gestor junta **R$/KM + classificação por cor + custo da corrida** sem tapar o mapa.

Fluxo de uso: ao abrir o app, a janela principal é o menu na aba **Opções**. Ligar o monitoramento sobe a notificação; o **selo** fica oculto enquanto o app está aberto e volta ao sair para o mapa. O toque no selo abre a **tela de atalhos** (espelho de Opções, menor, saindo do selo para o lado livre). Histórico, Dashboard, Despesas, Semáforo, Usuário e Sistema, nessa tela, abrem a mesma tela dentro do app. O menu só abre pelo ícone. Por cima de outros apps ficam o selo, os atalhos e a **compacta**.

---

## Primeira abertura

1. Permissões para seguir: notificações, sobrepor e bateria, cada uma com o motivo. A acessibilidade não fica ligada aqui.
2. E-mail obrigatório e, se tiver, a chave da versão Pro (`GestorDrivePro`). Chave vazia deixa o app em Free.
3. Tutorial em seis passos. **Pular** conclui. O último botão é **Começar**.
4. Abre Opções com o monitoramento desligado.

A acessibilidade só é pedida ao ligar o Monitorar, e fica ligada enquanto ele está ligado. Ao desligar o Monitorar ou fechar o app, ela se desliga sozinha, para o app de banco abrir. Com o Monitorar ligado, o banco ainda pode recusar.

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
4. Teste Pro: [`docs/ROTEIRO_PRO.md`](docs/ROTEIRO_PRO.md). Referência Beta (congelada): [`docs/ROTEIRO_BETA.md`](docs/ROTEIRO_BETA.md).

O APK de debug desta VS 2.3 fica em `dist/GestorDriver-VS-2.3.apk`.

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
| [docs/REGRAS_NEGOCIO.md](docs/REGRAS_NEGOCIO.md) | Regras oficiais (selo, aceite, Free/Pro, custos) |
| [docs/ROTEIRO_PRO.md](docs/ROTEIRO_PRO.md) | Fechamento Pro 2.0 e teste de rua |
| [docs/ROTEIRO_BETA.md](docs/ROTEIRO_BETA.md) | Teste Beta (histórico / `main`) |
| [docs/Roadmap.md](docs/Roadmap.md) | Feito / próximo |
| [docs/TESTING_STRATEGY.md](docs/TESTING_STRATEGY.md) | O que os testes cobrem |

Notas de sprint antigas não descrevem o estado atual.

**Ainda em aberto:** Bloco C (rua no SM-A145M); cobrança Play Store; testes instrumentados.

Projeto de portfólio. O Gestor observa a plataforma; a decisão de aceitar é do motorista.
