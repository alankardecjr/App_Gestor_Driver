# Roteiro Pro (beta) — Gestor Driver

Objetivo: fechar a **versão Pro** na branch `vs-2.0` (`2.0.0` / versionCode 13). **Não misturar com o Beta congelado em `main` (1.1.10).** Sem commit e sem push até pedido explícito.

Revisão: **05/09/2026** — **tela Atalhos congelada** (§44). **Próximo:** roteiro de rua **06/09/2026** (abaixo). Bloco C no SM-A145M.

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
| D5 | Compacta: `R$/Km Dist. Tempo Nota` + ícone plataforma · Parada(s); borda 6 dp; arrastável; toque não faz nada | Aprovado · no código |
| D6 | Overlay = Compose; tema Escuro / Claro / Celular (aba App). | Aprovado · no código |
| D7 | Semáforo **3** faixas: Ruim / Boa / Ótima. | Aprovado · no código |
| D8 | Consumo ou preço 0 → `—` + aviso em Custos. | Aprovado · no código |
| D9 | Lixeira só selecionadas. Sem seleção: **"Selecionar a(s) corrida(s)"**. Confirmação: **"Deseja apagar a(s) corrida(s) selecionada(s)?"** | Aprovado · no código |
| D10 | Voltar em degraus; Home → selo; Recentes guarda última tela; toque no selo reabre (ex. Histórico). | Aprovado · no código |
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

## Roteiro — 06/09/2026 (amanhã)

Objetivo do dia: **instalar o build atual** (working tree com §44) no SM-A145M e **fechar o Bloco C** na rua. Sem commit/push até pedido. Não misturar em `main`.

### Manhã — build e instalação (~30–45 min)

| # | Ação | Critério |
| --- | --- | --- |
| M1 | Rodar `gradlew.bat :app:testDebugUnitTest` | Verde (ou anotar falhas) |
| M2 | Build APK Pro `2.0.0` / vc 13 do **working tree atual** | APK gerado |
| M3 | Instalar no SM-A145M (substituir build antigo) | App abre; versão 2.0.0 |
| M4 | Permissões 🆗 (notificação, sobrepor, acessibilidade, bateria) | Selo aparece |
| M5 | Conta + pular tutorial se já fez; preencher **Despesas** (R$/L e km/L > 0) | Cálculo não fica `—` |

### Rua — checklist Bloco C (prioridade)

**Ordem sugerida:** C8 (sistema/§44) → C1–C3 (ciclo oferta) → C4–C7 → C9 se der → C10 se falhar leitura.

| # | Foco | Marcar |
| --- | --- | --- |
| C8a | Selo ↔ Atalhos: toque abre/fecha; **selo permanece**; **sem X** no card; card abre dir/esq/acima/abaixo (eixo da borda mais próxima) | |
| C8b | Menu ordem: Histórico \| Carteira \| Despesas \| Semáforo \| Usuário \| Configurar \| Fechar | |
| C8c | Overlay: só selo/atalhos/compacta sobre Uber/99; telas Menu = Activity | |
| C8d | Recentes com Histórico aberto → selo → toque no selo **reabre Histórico** | |
| C8e | Selo no **X** → monitoramento segue → **Abrir App** → selo volta | |
| C8f | Histórico + Recentes + X → **Abrir App** → reabre Menu; sair → selo | |
| C8g | **Desligar App** / Fechar → **tela de confirmação** (Cancelar/Fechar) | |
| C1 | Oferta → compacta R$/Km · Dist. · Tempo · Nota + borda 6 dp | |
| C2 | Recusa/expira → selo; sem histórico; notificação “Monitorando ofertas” | |
| C3 | Aceite → 1 linha Histórico; Consumo/Gasto; Embarque/Destino; selo | |
| C4 | Semáforo (sliders); borda coerente | |
| C5 | Abastecimento → pergunta aplica R$/L e km/L | |
| C6 | Dashboard Dia/Semana/Mês/Ano | |
| C7 | Lixeira seletiva + confirmação Limpar | |
| C9 | Free 🔒 (N/A se build só Pro) | |
| C10 | Oferta não leu → **ENVIAR LOG** + guardar em `.tmp-diag/` | |

### Tarde — fechar a sessão

1. Preencher Resultado (OK / FALHA / N/A) na tabela C1–C10 deste doc.
2. Se **C1–C3** ou **C8** falhar: anotar + trecho do log; **não** freeze.
3. Se parser errar (promo como oferta, aceite fantasma): priorizar calibração depois; UI ok não basta.
4. Se checklist OK: candidata a freeze Pro — **aguardar pedido de commit**.

### Fora do escopo de amanhã

Play Store, cobrança, gráficos, botão Aceitar, merge em `main`, push sem pedido.

---

## O que está no código (05/09/2026) — Atalhos congelados

- **Atalhos (congelado):** card arredondado + ícones; sem botão X; selo abre/fecha e permanece aberto
- **Posição Atalhos:** direita | esquerda | acima | abaixo conforme posição do selo (`AtalhosPosicao`)
- Ordem/cópia fixas: **Histórico | Carteira | Despesas | Semáforo | Usuário | Configurar | Fechar**
  - Histórico → Corridas aceitas → Histórico
  - Carteira → Gestor financeiro → Dashboard
  - Despesas → Lançar despesas → Custos/Despesas
  - Semáforo → Calibrar faixas → Semáforo
  - Usuário → Ajustar veiculo → Veículo
  - Configurar → Configurar App → Configurações
  - Fechar → Encerrar App → confirmação
- Telas nativas (Activity Compose): Histórico, Carteira/Dashboard, Semáforo, Config (Despesas · Veículo · App), **Confirmação (Fechar / Limpar histórico)**
- Compacta só com oferta: **R$/Km · Dist. · Tempo · Nota** + ícone plataforma · Parada(s); borda **6 dp**; ~4,5×1,7 cm; arrastável (posição gravada); toque não faz nada; some no aceite/expirar/recusar
- **Overlay sobre outros apps:** só selo · atalhos · compacta (§44); resto = Activity (inclui confirmação)
- **Carteira** (ex-Dashboard): Dia/Semana/Mês/Ano; atividade + Financeiro + Estimativa de gastos (combustível, óleo, pneus, seguro, IPVA)
- Telas nativas sem rodapé: Menu **Opções** + itens; ← volta a Opções; X em Opções → selo; Cancelar/Salvar em Despesas/Usuário/Configurar/Semáforo
- Histórico: plataformas, semana DOM–SÁB; resumo Faturamento · Distância · Tempo · **Corridas aceitas**; detalhes da corrida; lixeira seletiva
- Despesas: combustível, óleo (+ alerta), pneus, IPVA R$, seguro, km/ano
- Configurar (aba App): permissões, **tema**, Maps/Waze, enviar log, conta
- Free: cadeados na calculadora / dashboard / Carteira  

## Blocos

### Bloco A — Calculadora · feito no código

Guarda zero, lucro completo, semáforo 3 faixas, testes (`CalculadoraCustosTest`, `DashboardNumerosTest`).

### Bloco B — Telas · feito no código

Atalhos congelados (§44), Dashboard/Carteira, tema, alerta óleo, confirmação abastecimento, card histórico.

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
| C1 | Oferta chega | Compacta R$/Km · Dist. · Tempo · Nota; borda 6 dp; arrastável | |
| C2 | Recusa / expira | Volta ao selo; **não** entra no histórico; notificação = **"Monitorando ofertas"** | |
| C3 | Aceite na plataforma | Uma linha no Histórico; card com Consumo (L) + Gasto; Embarque/Destino; interface → selo | |
| C4 | Semáforo | Menu → Semáforo abre tela nativa; sliders Km/Hora/Nota; borda Ruim/Boa/Ótima coerente | |
| C5 | Abastecimento | Em Despesas/Veículo, preencher valor+litros (+km) → Salvar → **pergunta** se aplica R$/L e km/L | |
| C6 | Dashboard | Tela nativa: Dia/Semana/Mês/Ano; Receitas/Despesas/Saldo; médias; receitas por app | |
| C7 | Lixeira | Sem seleção → *"Selecionar a(s) corrida(s)"*; com seleção → confirma apagar | |
| C8 | Sistema | Voltar; Home → selo; Recentes → última tela; X + Abrir App (§44 casos 1–2); Desligar → confirmação | |
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
