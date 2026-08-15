# Execution Board - Gestor Driver

Board simplificado para acompanhar a execucao das proximas sprints ate a RC1.
Atualize este arquivo ao final de cada sessao de desenvolvimento.

## To Do

1. [S3] Definir contrato unico de Corrida e regras de classificacao.
2. [S3] Expandir testes do pipeline (truncadas, variacoes regionais, erros).
3. [S3] Implementar NotificationListenerService no Android.
4. [S3] Integrar detector/parser ao fluxo Android com fallback seguro.
5. [S4] Persistir configuracoes do motorista (DataStore/Room).
6. [S4] Finalizar overlay de decisao em tempo real.
7. [S4] Padronizar observabilidade e tratamento de erro.
8. [S4] Medir e otimizar performance e consumo.
9. [S5] Fechar testes Android (unitarios + instrumentados).
10. [S5] Preparar material final para recrutadores (README + demo + release notes).
11. [S5] Rodar beta fechado e consolidar feedback.
12. [S5] Gerar RC1.

## Doing

1. Nenhuma tarefa em andamento no momento.

## Done

1. Sprint 2 concluida: pipeline notifications completo em Python.
2. Testes unitarios iniciais do pipeline criados e aprovados.
3. Demo executavel de notificacoes criada.
4. Documentacao de dominio no core atualizada.
5. Roadmap detalhado de finalizacao registrado.
6. Passo 1 da nova classificacao concluido: enum oficial, limites centralizados, cor por classificacao e teste unitario cobrindo BAIXA.
7. Passo 2 concluido: AnaliseCorrida virou contrato oficial consolidado e a calculadora passou a retornar o objeto tipado.
8. Passo 3 concluido: combustivel estimado e custo estimado integrados ao AnaliseCorrida com base em ConfiguracaoUsuario.
9. Passo 4 concluido: comportamento do aplicativo formalizado como maquina de estados na especificacao.
10. Passo 5 concluido: HistoricoCorrida, persistencia local e consulta das ultimas corridas implementados em Python.
11. Passo 6 concluido: contrato Free/Beta/Pro implementado como camada de controle de recursos no nucleo.
12. Nucleo portado para Kotlin (calculator, classifier, fuel, validator).
13. Parser/extractor de notificacoes portados para Android.
14. NotificationListenerService conectado ao ViewModel via SharedFlow.
15. Ocultar (selo movel + reabrir) e Fechar (confirmacao) implementados na UI.
16. README, Roadmap e requirements.txt atualizados (25 testes Python).
17. Etapa 2: HistoricoItemPresentation estruturado, selecionarHistorico no ViewModel, cards clicaveis.

## Foco da semana (sugestao)

1. Overlay real com SYSTEM_ALERT_WINDOW.
2. Persistencia Room para historico e configuracoes.
3. Testes instrumentados com notificacoes reais.

## Criterios de saida da Sprint 3

1. NotificationListenerService ativo e recebendo notificacoes reais.
2. Notificacao suportada gera Corrida valida sem crash.
3. Erros de plataforma nao suportada tratados com fallback amigavel.
4. Suite de testes da camada de regras verdes localmente.
