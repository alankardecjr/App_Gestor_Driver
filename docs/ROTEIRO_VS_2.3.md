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
- **Botão na aba Opções** com legenda que muda conforme o estado
  (🔴 DESATIVADO → ATIVAR / 🟢 ATIVO → DESATIVAR).
- **Separar** "Desativar monitoramento" (pausa o serviço, app continua) de
  "Fechar app" (encerra). A notificação persistente passa a ter **Desativar + Abrir**;
  "Fechar" fica só dentro do app.
- **Desativar pede confirmação**.
- Regra de ouro (já respeitada): fechar o selo ≠ desligar o monitoramento.
- Base já existente: `AppState.monitorando`, start/stop reativo do `OverlayService`
  em `MainActivity`, foreground service + notificação persistente, overlays escondem
  quando `!monitorando`, `avaliarInicio()` já inicia OFF.
- Gap a implementar: `ativarMonitoramento()`/`desativarMonitoramento()` como ação
  única do usuário e **desacoplar** `monitorando` dos fluxos de navegação que hoje
  o setam `true` implicitamente.

## Status de implementação

- [x] Bloco A — R$/hora (tempo total) — Kotlin + Python + testes.
- [x] Bloco B — Compacta nova (Pro) + ✕ para descartar oferta.
- [x] Bloco C — Classificação 3 faixas (Ótima/Boa/Ruim); teste do histórico corrigido.
- [x] Bloco D — Terminologia "Lucro" → "Resultado".
- [x] Bloco F — Monitoramento ON/OFF explícito (cold start OFF, confirmação, notificação Desativar+Abrir).
- [x] Semáforo — Meta de R$/hora do motorista (`metaGanhoHora`).
- [x] Dashboard anual — já existente (Dia/Semana/Mês/Ano).
- Comparação de mercado e recomendações de UX: `docs/ANALISE_TECNICA_MERCADO.md`.

Validação: `pytest` 28/28 · `:app:testDebugUnitTest` 197/0. Overlay precisa de
verificação visual em aparelho (não testável em emulador).

## Regra de trabalho (dos anexos)

Por etapa: identificar arquivos → entender → alterar só o escopo → compilar →
testar → corrigir → registrar → próxima. Cada bloco deve deixar
`pytest` e `:app:testDebugUnitTest` verdes antes do commit.
