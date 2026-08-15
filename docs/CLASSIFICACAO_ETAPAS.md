# Classificacao por Etapas - Gestor Driver

Documento de controle para executar a revisao da classificacao de corridas sem misturar responsabilidades de dominio, calculo, interface e persistencia.

## Objetivo do ajuste

Unificar a classificacao de rentabilidade em um unico fluxo:

R$/KM -> faixa -> classificacao -> cor

O MVP continua usando R$/KM como referencia principal, mas com limites configuraveis e estrutura pronta para evoluir depois.

## Problemas identificados

1. Havia classificacao duplicada.
2. A calculadora ainda carregava regras proprias em texto puro.
3. Os limites estavam espalhados em mais de um arquivo.
4. A classificacao nao tinha um ponto unico para associar cor.
5. O nivel BAIXA existia em parte da modelagem, mas nao estava garantido como parte do fluxo oficial.

## Decisao aplicada no Passo 1

1. Usar Classificacao como enum oficial.
2. Centralizar limites em core/constants.py.
3. Criar MotorClassificacao como responsavel unico por decidir a faixa.
4. Associar classificacao -> cor no mesmo motor.
5. Deixar os limites parametrizaveis para ajuste futuro.

## Passo 1 concluido

Implementado em:

1. core/constants.py
2. core/classifier.py
3. core/calculator.py
4. tests/test_classification_engine.py

Validacao executada:

1. python -m compileall core
2. python -m unittest discover -s tests -p "test*.py"

## Proximos passos planejados

### Passo 2 - Atualizar AnaliseCorrida

Expandir o contrato de saida para incluir todos os dados que a interface precisa exibir com consistencia.

Status: concluido.

Implementacao realizada:

1. core/analysis.py passou a definir o contrato oficial de analise consolidada.
2. core/calculator.py passou a retornar AnaliseCorrida em vez de dicionario.
3. app/main.py e app/notifications_demo.py foram atualizados para consumir o contrato tipado.
4. testes de contrato e integracao foram ajustados para validar o novo formato.

### Passo 3 - Criar modelo de historico

Integrar combustivel estimado e custo estimado ao contrato de AnaliseCorrida usando a configuracao do motorista.

Status: concluido.

Implementacao realizada:

1. core/settings.py passou a expor consumo e preco ativos com base no combustivel selecionado.
2. core/calculator.py passou a consumir ConfiguracaoUsuario e CalculadoraCombustivel.
3. core/analysis.py passou a carregar combustivel_estimado e custo_combustivel como parte do contrato.
4. app/main.py e app/notifications_demo.py passaram a mostrar os novos campos.
5. testes passaram a cobrir gasolina e etanol.

### Passo 4 - Criar armazenamento local

Preparar a persistencia inicial da logica em Python antes da implementacao Android com Room.

### Passo 5 - Atualizar a especificacao MVP

Documentar a nova identidade visual e o comportamento da interface expandida, historico e estados de navegacao.

### Passo 6 - Testar o fluxo completo no Python

Garantir que multiplas corridas produzam analises consistentes e que o historico possa ser montado sem perda de contexto.

### Passo 7 - Levar o contrato consolidado para Android

Integrar NotificationListenerService, parser, analise, historico e overlay apenas depois do dominio estar fechado.
