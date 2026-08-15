# Projeto Gestor Driver --- Planejamento e Continuidade

**Data de criação:** 20/07/2026\
**Próxima sessão planejada:** 21/07/2026\
**Status:** Ideia definida --- iniciar desenvolvimento do MVP

------------------------------------------------------------------------

## 1. Visão do Projeto

O projeto consiste em criar um aplicativo para motoristas de aplicativos
como:

-   Uber
-   99
-   inDrive

O objetivo é permitir que o motorista tenha uma visão mais realista da
rentabilidade de cada corrida.

O aplicativo deverá analisar:

-   Valor total da corrida;
-   Distância até o passageiro;
-   Distância da viagem com o passageiro até o destino;
-   Distância total realmente percorrida;
-   Valor bruto por quilômetro;
-   Custos operacionais;
-   Valor líquido real;
-   Valor líquido por quilômetro.

------------------------------------------------------------------------

## 2. Exemplo do Funcionamento

Uma notificação pode informar:

``` text
Até o passageiro: 3,2 km
Viagem com passageiro: 12,8 km
Valor da corrida: R$ 38,00
```

O sistema deverá calcular:

``` text
Distância total = 3,2 km + 12,8 km
Distância total = 16 km
```

### Valor bruto por KM

``` text
R$ 38,00 ÷ 16 km = R$ 2,37/km
```

------------------------------------------------------------------------

## 3. Objetivo Principal

O grande objetivo do projeto é responder:

> Quanto realmente sobra para o motorista por cada quilômetro rodado?

O sistema deverá separar:

### Valor bruto

O valor recebido pela corrida.

### Custos operacionais

Exemplos:

-   Combustível;
-   Pneus;
-   Óleo;
-   Manutenção;
-   Depreciação;
-   Outros custos.

### Resultado líquido

``` text
Valor da corrida
- Custos operacionais
= Valor líquido real
```

Depois:

``` text
Valor líquido ÷ KM total
= Valor líquido por KM
```

------------------------------------------------------------------------

## 4. Diferencial do Projeto

O aplicativo não será apenas um calculador de R\$/KM.

A proposta é criar um gestor de rentabilidade para motoristas.

### Exemplo:

``` text
Valor da corrida:      R$ 38,00
Distância total:       16 km
Valor bruto por KM:    R$ 2,37/km

Custos totais:         R$ 15,04

Valor líquido:         R$ 22,96
Valor líquido por KM:  R$ 1,43/km
```

O motorista poderá entender a diferença entre:

``` text
O que a plataforma paga
        ↓
Valor bruto por KM

O que realmente sobra
        ↓
Valor líquido por KM
```

------------------------------------------------------------------------

## 5. Sistema de Custos por Quilômetro

Uma das funcionalidades mais importantes será transformar os custos do
veículo em um custo estimado por quilômetro.

### Exemplo de configuração:

#### Combustível

``` text
Preço do litro: R$ 6,00
Consumo médio: 10 km/L

Custo:
R$ 6,00 ÷ 10 km = R$ 0,60/km
```

#### Pneus

``` text
Valor do jogo: R$ 2.000
Vida útil estimada: 40.000 km

Custo:
R$ 2.000 ÷ 40.000 km = R$ 0,05/km
```

#### Óleo

``` text
Valor da troca: R$ 300
Intervalo: 10.000 km

Custo:
R$ 300 ÷ 10.000 km = R$ 0,03/km
```

#### Manutenção

``` text
Custo anual estimado: R$ 3.000
Quilometragem anual: 50.000 km

Custo:
R$ 3.000 ÷ 50.000 km = R$ 0,06/km
```

O sistema poderá somar os custos:

``` text
Combustível:   R$ 0,60/km
Pneus:         R$ 0,05/km
Óleo:          R$ 0,03/km
Manutenção:    R$ 0,06/km
Depreciação:   R$ 0,20/km
--------------------------------
Custo total:   R$ 0,94/km
```

------------------------------------------------------------------------

# 6. Estrutura Planejada

``` text
Gestor Driver
│
├── Corrida Atual
│   ├── Valor da corrida
│   ├── KM até o passageiro
│   ├── KM da viagem
│   ├── KM total
│   ├── R$/KM bruto
│   ├── Custos
│   ├── Valor líquido
│   └── R$/KM líquido
│
├── Meu Veículo
│   ├── Combustível
│   ├── Pneus
│   ├── Óleo
│   ├── Manutenção
│   ├── Depreciação
│   └── Outros custos
│
├── Histórico
│   ├── Corridas
│   ├── Ganhos
│   ├── Custos
│   └── Lucro
│
└── Relatórios
    ├── Diário
    ├── Semanal
    └── Mensal
```

------------------------------------------------------------------------

# 7. Plano de Desenvolvimento

## Fase 1 --- Motor de Cálculo

Primeiro desenvolver e validar a lógica de negócio.

Entrada:

``` text
Valor da corrida
KM até o passageiro
KM da viagem
Custo operacional por KM
```

Saída:

``` text
KM total
Valor bruto por KM
Custo total
Valor líquido
Valor líquido por KM
```

### Fórmulas principais

``` text
km_total = km_ate_passageiro + km_viagem
```

``` text
valor_bruto_por_km = valor_corrida / km_total
```

``` text
custo_total = km_total * custo_operacional_por_km
```

``` text
valor_liquido = valor_corrida - custo_total
```

``` text
valor_liquido_por_km = valor_liquido / km_total
```

------------------------------------------------------------------------

## Fase 2 --- Aplicativo Android

Tecnologias inicialmente planejadas:

-   Kotlin;
-   Android Studio;
-   Jetpack Compose;
-   Arquitetura MVVM;
-   Room Database.

------------------------------------------------------------------------

## Fase 3 --- Configuração de Custos

Criar uma área onde o motorista poderá informar seus custos.

Exemplos:

``` text
Preço do combustível
Consumo do veículo
Valor dos pneus
Vida útil dos pneus
Valor do óleo
Intervalo de troca
Manutenção
Depreciação
Outros custos
```

O sistema transformará essas informações em custo por quilômetro.

------------------------------------------------------------------------

## Fase 4 --- Leitura Automática de Notificações

Utilizar o recurso do Android:

``` text
NotificationListenerService
```

Fluxo:

``` text
Uber / 99 / inDrive
        ↓
Notificação recebida
        ↓
Aplicativo lê o conteúdo
        ↓
Parser identifica os dados
        ↓
Valor + KM até passageiro + KM da viagem
        ↓
Cálculo automático
```

### Arquitetura planejada para os parsers

``` text
NotificationParser
│
├── UberParser
├── NoveNoveParser
└── InDriveParser
```

Cada aplicativo pode utilizar um formato diferente de notificação. Por
isso, o sistema deverá tratar cada formato de maneira específica.

------------------------------------------------------------------------

## Fase 5 --- Histórico e Relatórios

Registrar cada corrida:

``` text
Data
Aplicativo
Valor bruto
KM até passageiro
KM da viagem
KM total
Custos
Valor líquido
Valor bruto por KM
Valor líquido por KM
```

Exemplo de relatório:

``` text
Hoje

12 corridas
R$ 320,00 bruto
R$ 105,00 custos
R$ 215,00 líquido

Total rodado: 180 km

Média líquida:
R$ 1,19/km
```

------------------------------------------------------------------------

# 8. Estratégia para o Desenvolvimento

A recomendação é não começar imediatamente pela leitura das
notificações.

A sequência ideal será:

``` text
1. Desenvolver a lógica de cálculo
          ↓
2. Testar o motor de cálculo
          ↓
3. Criar uma interface Android simples
          ↓
4. Adicionar custos configuráveis
          ↓
5. Criar banco de dados local
          ↓
6. Implementar leitura de notificações
          ↓
7. Criar parsers para cada aplicativo
          ↓
8. Criar histórico
          ↓
9. Criar relatórios
```

------------------------------------------------------------------------

# 9. Próximo Passo Para 21/07/2026

Retomar o projeto começando pela **Fase 1 --- Motor de Cálculo**.

## Primeira tarefa

Definir e implementar a estrutura inicial da lógica de negócio.

Sugestão:

``` text
Projeto:
Gestor Driver

Primeiro módulo:
calculadora_corrida

Responsabilidade:
Receber os dados da corrida e calcular a rentabilidade.
```

O primeiro objetivo será criar um cálculo confiável e testável antes de
iniciar a interface Android.

------------------------------------------------------------------------

# 10. Decisões Importantes do Projeto

-   O foco será a rentabilidade real por quilômetro.
-   A distância até o passageiro será considerada no cálculo.
-   A distância da viagem com o passageiro também será considerada.
-   O valor bruto e o valor líquido serão apresentados separadamente.
-   Os custos do veículo serão convertidos em custo por quilômetro.
-   O sistema deverá ser preparado para múltiplos aplicativos de
    transporte.
-   O desenvolvimento será feito de forma incremental.
-   A lógica de negócio será validada antes da integração com
    notificações.
-   O projeto poderá começar com testes da lógica em Python e
    posteriormente evoluir para Android com Kotlin.

------------------------------------------------------------------------

## Status Atual

**Ideia:** Definida\
**Objetivo:** Definido\
**Arquitetura inicial:** Definida\
**MVP:** Definido\
**Desenvolvimento:** Ainda não iniciado

**Próxima sessão:** iniciar o motor de cálculo da corrida.
