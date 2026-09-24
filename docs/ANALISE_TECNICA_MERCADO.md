# Análise técnica e comparação de mercado — Gestor Driver (VS 2.3)

Documento de análise: onde o Gestor está, como se compara aos apps de ponta e
quais ajustes deixam o produto seguro, funcional e atraente. Baseado em pesquisa
de mercado (Rota Pro, CustoKm, DashDecide, estudos de UX de apps de motorista) e
na leitura do código atual.

## 1. Posicionamento

O Gestor Driver é um **copiloto de decisão**: lê a oferta de Uber/99/inDrive
(notificação + acessibilidade/OCR) e mostra, num overlay sobre o mapa, se a
corrida vale a pena — sem aceitar por você. É a mesma categoria do **Rota Pro**
(semáforo sobre a oferta) e do **CustoKm** (calculadora de lucro real). O
diferencial defensável do Gestor: cálculo de custo real (combustível + óleo +
pneus + IPVA/seguro rateados), histórico com snapshot no aceite e dashboard
financeiro próprio.

## 2. Comparação com apps de ponta

| Recurso | Rota Pro | CustoKm | DashDecide | **Gestor (VS 2.3)** |
| --- | --- | --- | --- | --- |
| Overlay/semáforo na oferta | ✅ | ⚠️ (print) | ✅ | ✅ |
| R$/km | ✅ | ✅ | ✅ | ✅ |
| **R$/hora (tempo total)** | ✅ | ✅ | ⚠️ | ✅ (novo) |
| Faixas configuráveis (verde/amarelo/vermelho) | ✅ | — | ✅ | ✅ |
| **Meta de R$/hora do motorista** | ⚠️ | ⚠️ | ✅ (thresholds) | ✅ (novo) |
| Custo real por km (manutenção/depreciação) | ✅ | ✅ | ⚠️ | ✅ |
| Histórico de aceitas com detalhes | ✅ | — | — | ✅ |
| Dashboard diário/semanal/mensal/anual | ✅ | — | — | ✅ |
| Anúncio por voz (mãos livres) | ⚠️ | — | ✅ | ❌ (gap) |
| Card → pílula no canto (two-stage) | ✅ | — | ✅ | ⚠️ (parcial) |
| Contagem regressiva/expiração | ✅ | — | ✅ | ✅ (expira) |

Legenda: ✅ tem · ⚠️ parcial · ❌ não tem.

## 3. Princípios de UX validados na pesquisa

O card de oferta é lido **a distância de para-brisa, em 1–2 segundos**. Consenso
dos estudos de UX de apps de motorista:

- **No máximo ~4 números** no card; o resto é detalhe sob toque. (VP0, PickMe UX)
- **Tipografia grande, alto contraste (WCAG AA+), alvos de toque ≥ 48 dp** — o
  motorista está em movimento, sol na tela. (PickMe UX)
- **Uma métrica-herói**: a pesquisa mostra que motoristas escaneiam primeiro
  **uma** variável. No nosso caso, R$/km e R$/hora são as duas de decisão.
- **Veredito por cor** (verde/amarelo/vermelho) resolve na hora. (DashDecide)
- **Overlay em dois estágios**: card grande transitório → **pílula no canto** que
  não cobre o mapa. (DashDecide) 
- **Decisão sem toque quando possível**: anúncio por voz do veredito. (DashDecide)
- **Não cobrir o mapa nem o botão Recusar** da plataforma; ancorar por
  window bounds, respeitando notch/gestos. (DashDecide, Android)

## 4. O que a VS 2.3 já endereça

- **R$/hora com tempo total** (até o passageiro + viagem) — alinhado ao que
  CustoKm e Rota Pro fazem; complementa o R$/km.
- **Compacta enxuta** `R$/KM · R$/HORA · TEMPO · NOTA` (4 números), sem VALOR
  (a plataforma já mostra), com borda pela classificação.
- **✕ para descartar** a oferta + sumiço automático na expiração.
- **Classificação em 3 faixas** (Ótima/Boa/Ruim = verde/amarelo/vermelho),
  calibrável pelo usuário.
- **Meta de R$/hora** no semáforo — o app passa a entender o objetivo por tempo.
- **Monitoramento ON/OFF** explícito — controle e previsibilidade.
- **Histórico** com card simples que amplia ao toque; **dashboard** diário/
  semanal/mensal/anual.

## 5. Ajustes recomendados (priorizados)

### Alto impacto / baixo-médio esforço
1. **Colorir a célula R$/HORA pela meta** (verde ≥ meta, vermelho abaixo). A
   lógica já existe (`atingeMetaGanhoHora`); falta levar `metaGanhoHora` e
   `valorPorHora` para o `OverlaySnapshot` e pintar a célula.
2. **Modo pílula no canto** (two-stage overlay): após alguns segundos, encolher a
   compacta para uma pílula com o veredito colorido, liberando o mapa. Grande
   ganho de não-intrusão citado pela pesquisa.
3. **Contraste e tamanho na compacta**: subir o R$/km e R$/hora para display-size,
   TEMPO/NOTA secundários; garantir AA+ e alvos ≥ 48 dp no ✕.

### Médio impacto
4. **Anúncio por voz do veredito** (TTS): "Ótima, R$ 2,38 por km, R$ 95 por hora"
   — operação mãos-livres, forte diferencial de segurança.
5. **Quebra do tempo na expandida**: mostrar "X min até você + Y min viagem"
   (hoje somamos silenciosamente; separar as pernas melhora a transparência).
6. **Onboarding do custo real**: assistente rápido para preencher consumo/preço/
   manutenção — é o que dá precisão ao R$/km líquido (vantagem sobre concorrentes).

### Base técnica / segurança
7. **Snapshot completo no aceite**: gravar também os parâmetros de entrada
   (preço combustível, consumo, custos) no histórico, não só os resultados, para
   permitir recomputar/auditar depois.
8. **Design tokens únicos**: consolidar cores/tipografia/spacing (evitar paletas
   espalhadas) para uma UX moderna e consistente clara/escura.
9. **Testes instrumentados** do overlay/parser em aparelho real (Uber/99/inDrive)
   — hoje a cobertura é unitária; o parser é o ponto frágil.

## 6. Riscos e limites

- **Parser dependente de layout** das plataformas: manter fixtures de textos
  reais anonimizados e degradar com honestidade (R$/hora "estimado" quando falta
  o tempo de embarque).
- **Overlay/permissões**: acessibilidade + sobreposição + foreground service
  exigem fluxo de permissões claro e verificação em vários fabricantes.
- **Verificação visual**: as telas de overlay não são testáveis em emulador;
  exigem teste de rua (sol, vibração, direção).
