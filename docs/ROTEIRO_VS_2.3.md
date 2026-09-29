# Roteiro VS 2.3 — Fechamento econômico + R$/hora + Monitoramento ON/OFF

Documento-fonte das decisões da versão 2.3. Consolida o Plano Mestre e o
Roteiro de Fechamento com o estado real do código (a arquitetura de runtime —
serviço, overlay, listener, parser, cálculo, histórico, dashboard — **já existe**;
a 2.3 fecha lacunas, não reescreve).

## Tese central: R$/hora

R$/hora é a métrica de decisão mais próxima da realidade do motorista (o ganho
é por tempo ocupado). Complementa o R$/KM: uma corrida com bom R$/KM mas presa
em trânsito pode ter R$/hora ruim. Benchmark de mercado (Rota Pro, CustoKm) e
calculadoras de referência confirmam: usar **tempo total** e **distância total**.

Definição oficial (espelha o R$/KM, que já usa `kmTotal`):

```
R$/hora = valor_total / (tempo_total / 60)
tempo_total = tempo até o passageiro + tempo da viagem
```

Nota de implementação: no Android, `NotificationExtractor` já **soma** as duas
pernas de tempo do card (ex.: `6min (971m)` + `8min (2,9km)` → `tempoEstimado = 14`),
então `tempoEstimado` já é o tempo total. `valorPorHora` é derivado dele.
Quando o card traz só o tempo da viagem, o valor é uma aproximação otimista
(rótulo honesto). Refino futuro: separar as pernas para exibir a quebra na
expandida.

## Escopo da VS 2.3

- **Bloco A** — Domínio: `valorPorHora`/`valor_por_hora` (tempo total) em
  `Corrida` e `AnaliseCorrida` (Kotlin + Python), populado nas calculadoras.
- **Bloco B** — Compacta nova (foco **Pro**):
  `R$/KM · R$/HORA · TEMPO · NOTA` + rodapé `Dist. · Paradas`.
  - Sem VALOR (a plataforma já mostra o valor bruto) — nem na compacta nem na expandida.
  - Cor por célula em R$/KM e R$/HORA; borda do card pela **pior** das duas faixas.
  - Destaque (fonte maior) nas duas métricas-herói; TEMPO/NOTA secundários.
  - Card único (header/métricas/rodapé), borda 2dp na cor da classificação.
  - Campos vazios somem graciosamente (PARADAS só se > 0; NOTA vira "—").
- **Bloco C** — Classificação: reconciliar para **RUIM/BOA/ÓTIMA**
  (vermelho/amarelo/verde) e corrigir o teste Python quebrado do histórico.
  - No Android o motor **já é 3 faixas** (EXCELENTE=Ótima / BOA / RUIM) e a
    calibração do usuário **já é aplicada** no pipeline real de ofertas
    (`RideNotificationProcessor` usa `MotorClassificacao.daConfiguracao`).
  - O núcleo Python produzia uma faixa `REGULAR` a mais → alinhado ao Android
    (3 faixas), com rótulos Ótima/Boa/Ruim e cores verde/amarelo/vermelho.
- **Bloco D** — Terminologia: "lucro" → "resultado operacional".
- **Bloco F** — Monitoramento ON/OFF explícito.
- **Bloco E** — Testes (matriz de domínio + fluxo ON/OFF) verdes + APK.

Fora da 2.3 (2.4+): plano Free, snapshot dos parâmetros de entrada no histórico,
R$/hora online real (`SessaoTrabalho`), Design System / tokens, depreciação.

## Bloco F — Monitoramento ON/OFF (decisões confirmadas)

- **Cold start sempre OFF**: abrir o app não liga o monitoramento; o usuário decide.
- **Botão na aba Opções** com título **Monitorar (ON)** ou **Monitorar (Off)** e subtítulo **Calculadora de ganhos**.
  Ligado: fundo verde translúcido. Desligado: cinza. O mesmo vale na tela de atalhos.
- **Separar** "Desligar monitoramento" (some selo e aviso; o app continua) de
  "Fechar app" (encerra). A notificação tem **Abrir App** (abre o menu) e **Fechar App**
  (confirma, para o monitoramento e fecha o app). Dispensar só o aviso não desliga.
- **Ligar e desligar pedem confirmação.**
- Fechar só o selo ou só o aviso não desliga. Fechar aviso, selo e menu, os três, desliga. Home e Recentes também fecham o menu: com aviso e selo já fechados, o monitoramento desliga. O selo jogado na lixeira não volta no Home.
- Ao ligar, a notificação sobe na hora. O selo fica oculto enquanto o app está aberto e volta quando o usuário sai para o mapa. O toque no selo abre os atalhos.
- Base já existente: `AppState.monitorando`, start/stop reativo do `OverlayService`
  em `MainActivity`, foreground service + notificação persistente, overlays escondem
  quando `!monitorando`, `avaliarInicio()` já inicia OFF.
- Gap a implementar: `ativarMonitoramento()`/`desativarMonitoramento()` como ação
  única do usuário e **desacoplar** `monitorando` dos fluxos de navegação que hoje
  o setam `true` implicitamente.

## VS 2.3.2 — pronta para teste de rua

`2.3.2` / versionCode 24. APK de debug: `dist/GestorDriver-VS-2.3.2.apk`.
Testes unitários: 230, sem falhas. A formatação dos campos acontece **ao salvar**, não a cada tecla.

- Compacta com 4,2 cm de largura. Valores centralizados, sem corte. Borda mais fina. O X fica afastado da borda. Há espaço entre o nome do app e o tempo/km. Vermelho, amarelo e verde do semáforo ficam nas barras e na borda; os números continuam pretos ou brancos.
- Home e Recentes reabrem a última aba. O app não força Opções. Voltar desce até Opções; o próximo Voltar vai para a tela inicial do celular e deixa o app nos Recentes.
- Selo na lixeira só volta quando o Monitorar é desligado e ligado de novo.
- Atalhos abrem a aba correspondente: Histórico, Dashboard, Despesas, Semáforo, Usuário e Sistema.
- No card de resumo do histórico, embarque e destino mostram só a primeira linha. O detalhe da corrida permanece inteiro.
- Ao salvar: `01052026` ou `010526` vira `01/05/2026`. `50` e `50.00` viram `50,00`. `50000` em R$ vira `50.000,00`. `5000` e `50000` em km ou km/L viram `5.000` e `50.000`. Data inválida fica como foi digitada. Zero continua em branco.

## Status de implementação

- [x] Bloco A — R$/hora (tempo total) — Kotlin + Python + testes.
- [x] Bloco B — Compacta nova (Pro) + ✕ para descartar oferta.
- [x] Bloco C — Classificação 3 faixas (Ótima/Boa/Ruim); teste do histórico corrigido.
- [x] Bloco D — Terminologia "Lucro" → "Resultado".
- [x] Bloco F — Monitoramento ON/OFF explícito (cold start OFF, confirmação nos dois sentidos, aviso sem ação Desativar).
- [x] Janela principal — menu na aba Opções, tela cheia. Overlay só selo, atalhos e compacta.
- [x] Menu sem faixa nem deslize. Ordem: Monitorar (ON/Off), Localização, Histórico, Dashboard, Despesas, Semáforo, Usuário, Sistema, Fechar.
- [x] Semáforo, Despesas e Usuário: Salvar grava e permanece. A seta, com edição, pergunta se salva. Sistema: sem Cancelar/Salvar; a seta grava e volta para Opções.
- [x] Recentes e Home mostram Opções. O ícone do app fica fixo antes do título e não abre o selo.
- [x] Dashboard: seguro mensal e IPVA anual fecham no mês e no ano. Óleo e pneu em ícone cinza.
- [x] Notificação com **Abrir App** e **Fechar App**. Oferta ativa mostra R$/km, resultado, litros e nota.
- [x] Atalhos espelham Opções, menores, sem rolagem, saindo do selo para o lado livre. Fechar vermelho. Monitorar ligado: verde translúcido, **Monitorar (ON)**. Histórico, Dashboard, Despesas, Semáforo, Usuário e Sistema abrem essa tela no app.
- [x] Acessibilidade só com o Monitorar. Desliga ao parar ou ao fechar o app. Se o processo recomeçar com o Monitorar ligado, a leitura continua. Com ela desligada, o banco abre. Com o Monitorar ligado, o banco pode recusar.
- [x] Selo do cabeçalho desenha o ícone do app como bitmap. O ícone adaptativo derrubava o processo ao ligar o monitoramento, e selo e aviso não subiam.
- [x] Semáforo: R$/km de 0 a 4, nota de 3,00 a 5,00, marcas com − e +. R$/hora continua de 0 a 99. Ruim, boa e ótima ocupam um terço cada na barra.
- [x] Fechar e desligar confirmam em Opções. A tela expandida antiga não entra nesse caminho.
- [x] Histórico: quantidade de corridas com dois dígitos e Qt alinhado aos outros campos.

## Janela principal e overlays

- Ao iniciar o app, a janela principal é o **menu na aba Opções** (tela cheia).
  Título **Gestor Driver**, sem subtítulo.
- Histórico, Dashboard, Despesas, Semáforo, Usuário e Sistema abrem **dentro do app**. O mesmo toque na tela de atalhos abre essa tela, não a lista de Opções.
- Por cima de outros apps: **selo**, **tela de atalhos** e **compacta**.
- Selo e aviso da barra sobem ao ligar o monitoramento. Com o app aberto o selo fica oculto; ao sair, ele volta, salvo se foi jogado na lixeira. Fechar nos atalhos abre o app em Opções e confirma lá.
  Fechar só o aviso ou só o selo não desliga. Fechar aviso, selo e menu desliga.
- [x] Semáforo — Meta de R$/hora do motorista (`metaGanhoHora`).
- [x] Dashboard anual — já existente (Dia/Semana/Mês/Ano).
- Comparação de mercado e recomendações de UX: `docs/ANALISE_TECNICA_MERCADO.md`.

Validação desta etapa: `AppViewModelTest` e `OnboardingViewModelTest` verdes.
`:app:assembleDebug` gera o APK de debug. Overlay ainda precisa de verificação
visual no aparelho.

## Regra de trabalho (dos anexos)

Por etapa: identificar arquivos → entender → alterar só o escopo → compilar →
testar → corrigir → registrar → próxima. Cada bloco deve deixar
`pytest` e `:app:testDebugUnitTest` verdes antes do commit.
