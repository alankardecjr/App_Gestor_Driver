# Roteiro Pro (beta) — Gestor Driver

Objetivo: fechar a **versão Pro** na branch `vs-2.0` (`2.0.0` / versionCode 13). **Não misturar com o Beta congelado em `main` (1.1.10).** Sem commit e sem push até pedido explícito.

Revisão: **03/09/2026** — telas A/B no código. **Bloco C em andamento** no SM-A145M (`2.0.0` instalado). Fonte oficial de telas: `Roteiro_Oficial_Gestor_Driver_Vs Pro_2.0.txt`.

## Versões do produto (D1 + D12)

| Versão | Papel |
| --- | --- |
| **Free** | Demo grátis. Mesmas telas da Pro. Valores da calculadora / dashboard **ocultos** (🔒). |
| **Pro** | Versão paga (quando estiver ok). Tudo liberado. A antiga Beta vira esta linha. |

`PlanoAcesso.BETA` no código = Pro. Loja / cobrança ficam **fora** desta entrega (D11).

## Decisões aprovadas

| # | Decisão | Status |
| --- | --- | --- |
| D1 | Free = demo com calculadora oculta. Pro = paga, tudo liberado. Beta vira Pro. | Aprovado · no código |
| D2 | Lucro = valor − **todos** os gastos: combustível (selecionado), óleo, pneu, IPVA e seguro. | Aprovado · no código |
| D3 | Abastecimento: ao Salvar, **perguntar** se aplica R$/L e km/L do combustível atual. | Aprovado · no código |
| D4 | Dashboard: Diário / Semanal / Mensal — faturamento, gastos, lucro líquido, médias (km/hora/corrida) e rateio por item. Sem gráficos. | Aprovado · no código (Compose + overlay) |
| D5 | Compacta: `$/Km $/Lucro $/Gasto Nota` + contexto. Sem R$/km líquido. | Aprovado · no código |
| D6 | Overlay = Compose; tema Escuro / Claro / Celular (aba App). | Aprovado · no código |
| D7 | Semáforo **3** faixas: Ruim / Boa / Ótima. | Aprovado · no código |
| D8 | Consumo ou preço 0 → `—` + aviso em Custos. | Aprovado · no código |
| D9 | Lixeira só selecionadas. Sem seleção: **"Selecionar a(s) corrida(s)"**. Confirmação: **"Deseja apagar a(s) corrida(s) selecionada(s)?"** | Aprovado · no código |
| D10 | Voltar em degraus; Home → selo; Recentes reabre a **última tela**. | Aprovado · no código |
| D11 | Não publicar loja. Free só cadeado. | Aprovado |
| D12 | Documentar Free vs Pro. | Aprovado · `REGRAS_NEGOCIO.md` §38 |
| D13 | IPVA: vencimento (Veículo) + **Valor R$** (Custos). | Aprovado · no código |
| D14 | Seguro: **Valor R$** + vencimento (Custos). | Aprovado · no código |
| D15 | Rateio IPVA/seguro: `(valor ÷ km/ano) × km` (melhor prática custo/km). Km/ano = 0 → fora. | Aprovado · no código |
| D16 | Óleo/pneu: `(valor ÷ km) × km da corrida`. Km = 0 → fora. | Aprovado · no código |
| D17 | Faixas padrão: Ruim até **1,59** · Boa **1,60–1,99** · Ótima a partir de **2,00**. | Aprovado · no código |
| D18 | Compacta e overlay seguem o tema escolhido. | Aprovado · no código |
| D19 | Vencimento IPVA em Veículo; valor R$ em Custos. | Aprovado · no código |
| D20 | Km por ano começa em **0**. | Aprovado · no código |
| D21 | Card histórico: linha com **Consumo (L)** e **Gasto (R$)**; botões Embarque / Destino no card. | Aprovado · no código |
| D22 | Alerta óleo: aviso **500 km** antes do vencimento; texto vermelho. | Aprovado · no código |
| D23 | Notificação: expirou/recusou → limpa e volta a **"Monitorando ofertas"**. | Aprovado |

**Fórmula do gasto da oferta / corrida:**

1. Combustível (consumo e preço > 0): litros = km ÷ km/L; gasto = litros × R$/L.  
2. Óleo + pneus: Σ (valor ÷ km base) × km da corrida.  
3. IPVA + seguro: Σ (valor ÷ km/ano) × km da corrida, se km/ano > 0.  
4. **Gasto** = 1+2+3. **Lucro** = valor − gasto.  
5. Parcela zerada ignorada; se nada calculável → `—`.

## O que está no código (03/09/2026)

- Menu: Histórico · Semáforo · Custos · Veículo · Dashboard · Configurações · Fechar  
- Dashboard Compose + overlay (mesmos números via `DashboardNumeros`)  
- Histórico: plataformas, período, DOM–SÁB, card com consumo+gasto, Embarque/Destino, lixeira seletiva  
- Custos: combustível, óleo (+ alerta), pneus, IPVA R$, seguro, km/ano  
- App: permissões, **tema**, Maps/Waze, enviar log, conta  
- Free: cadeados na calculadora / dashboard  

## Blocos

### Bloco A — Calculadora · feito no código

Guarda zero, lucro completo, semáforo 3 faixas, testes (`CalculadoraCustosTest`, `DashboardNumerosTest`).

### Bloco B — Telas · feito no código

Dashboard completo, tema, alerta óleo, confirmação abastecimento, card histórico.

### Bloco C — Rua · em andamento (SM-A145M)

**Aparelho:** Samsung SM-A145M (`RX8WB00BL9H`).  
**Build alvo:** `2.0.0` / versionCode 13 (substituir o Beta `1.1.10` só neste aparelho de teste).  
**Log:** aba App → **ENVIAR LOG** · arquivo `notificacoes_diagnostico.txt` · logcat em `.tmp-diag/`.

#### Preparação

1. Instalar Pro `2.0.0` no SM-A145M.
2. Permissões 🆗 (notificação, sobrepor, acessibilidade, bateria).
3. Conta + tutorial (ou pular) → selo.
4. Preencher Custos (R$/L, km/L > 0) e, se quiser testar rateio, óleo/pneu/IPVA/seguro + km/ano.
5. Abrir Uber Driver / 99 / inDrive logado; Gestor em segundo plano (selo no mapa).

#### Checklist de rua (marque OK / FALHA / N/A)

| # | Passo | Esperado | Resultado |
| --- | --- | --- | --- |
| C1 | Oferta chega | Compacta com $/Km · $/Lucro · $/Gasto · Nota; borda semáforo (3 faixas) | |
| C2 | Recusa / expira | Volta ao selo; **não** entra no histórico; notificação = **"Monitorando ofertas"** | |
| C3 | Aceite na plataforma | Uma linha no Histórico; card com Consumo (L) + Gasto; Embarque/Destino; interface → selo | |
| C4 | Semáforo | Borda Ruim / Boa / Ótima coerente com R$/km e faixas | |
| C5 | Abastecimento | Em Custos, preencher valor+litros (+km) → Salvar → **pergunta** se aplica R$/L e km/L | |
| C6 | Dashboard | Overlay e/ou app: Diário/Semanal/Mensal; Faturamento/Gastos/Lucro; médias; rateios | |
| C7 | Lixeira | Sem seleção → *"Selecionar a(s) corrida(s)"*; com seleção → confirma apagar | |
| C8 | Sistema | Voltar em degraus; Home → selo; Recentes → última tela | |
| C9 | Free 🔒 | Com plano Free: R$/KM, litros, gasto, lucro e dashboard ocultos | |
| C10 | Oferta não leu | App → **ENVIAR LOG** e guardar texto real | |

#### Depois da sessão

Anotar falhas abaixo; se parser falhar, anexar trecho do log. Freeze Pro só com checklist OK.

**Sessão 03/09/2026 (15:57):** `2.0.0` / versionCode 13 instalado e aberto no SM-A145M (`RX8WB00BL9H`). Listener + acessibilidade + overlay OK. Logcat: `.tmp-diag/logcat-bloco-c.txt`. C9 (Free 🔒) sem seletor na UI (build inicia em Pro) — N/A nesta sessão.

Resultados C1–C8 / C10: pendentes (rua).

### Bloco D — Documento e freeze

Regras Free vs Pro, §38–40, README e roadmap atualizados nesta revisão. Freeze Pro só depois da rua. **Commit só com pedido. Não push. Não misturar em `main`.**

## Fora desta entrega

- Play Store / cobrança da Pro  
- Gráficos / metas além do dashboard atual  
- Botão Aceitar no Gestor  
- Mexer no Beta `main` 1.1.10  

## Critério de “Pro fechada”

1. Decisões D1–D23 alinhadas ao app instalado.  
2. Rua no SM-A145M passou.  
3. `REGRAS_NEGOCIO.md` = comportamento real.

Até lá a Pro é beta em fechamento, não produto congelado.
