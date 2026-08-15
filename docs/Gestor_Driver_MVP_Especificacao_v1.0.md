# Gestor Driver — Especificação do MVP v1.1

Este documento descreve o escopo inicial do produto, seus objetivos, regras de negócio e critérios de aceitação para o MVP.

## 1. Objetivo do produto

Auxiliar motoristas de aplicativos a avaliar rapidamente se uma corrida é financeiramente vantajosa antes de aceitá-la.

## 2. Público-alvo

Motoristas de aplicativos que desejam tomar decisões mais estratégicas e reduzir desperdício de tempo e combustível.

## 3. Problema central

O motorista precisa decidir rapidamente com base em informações incompletas e dispersas. O aplicativo deve consolidar essas informações em uma análise objetiva.

## 4. Funcionalidades do MVP

### 4.1 Análise da corrida

- calcular distância total;
- calcular valor por quilômetro (R$/KM);
- estimar consumo e custo operacional;
- gerar uma classificação visual de rentabilidade.

### 4.2 Configurações do usuário

- veículo: marca, modelo, versão e ano;
- consumo por combustível;
- preço dos combustíveis;
- combustível utilizado.

### 4.3 Interface

- interface compacta com dados principais;
- interface expandida com detalhes adicionais;
- experiência simples, rápida e com foco em decisão imediata.

## 5. Estado e Comportamento do Aplicativo

### Princípio

O Gestor Driver não depende da existência de uma notificação para permanecer ativo.

A ausência de uma oferta de corrida é considerada um estado normal do aplicativo e não um erro.

### Modelo de estado

O aplicativo opera como uma máquina de estados finitos.

Estados oficiais:

- IDLE — aplicativo aguardando atividade;
- MONITORANDO — serviço ativo aguardando notificações;
- CORRIDA_COMPACTA — corrida detectada e interface resumida;
- CORRIDA_DETALHES — informações completas da corrida;
- HISTORICO — últimas corridas armazenadas;
- CONFIGURACOES — configuração do veículo e custos;
- ENCERRADO — aplicativo e monitoramento encerrados.

### Transições principais

| Estado atual | Evento | Próximo estado | Resultado esperado |
| --- | --- | --- | --- |
| IDLE | Abrir aplicativo | MONITORANDO | O aplicativo permanece ativo e aguardando atividade |
| MONITORANDO | Notificação válida detectada | CORRIDA_COMPACTA | Corrida apresentada em formato resumido |
| CORRIDA_COMPACTA | Mais detalhes | CORRIDA_DETALHES | Interface expande as informações |
| CORRIDA_DETALHES | Menos detalhes | CORRIDA_COMPACTA | Interface retorna ao modo resumido |
| CORRIDA_COMPACTA | Histórico | HISTORICO | Histórico fica disponível sem encerrar monitoramento |
| CORRIDA_DETALHES | Histórico | HISTORICO | Histórico substitui a visualização detalhada |
| Qualquer estado de interface | Sair interface | IDLE | A interface é fechada e o monitoramento continua |
| Qualquer estado | Fechar app | ENCERRADO | Aplicativo, monitoramento e overlays são encerrados |

### Regras de permanência

- se não houver notificação válida, o aplicativo permanece em MONITORANDO;
- se o usuário não estiver logado na Uber, 99 ou inDrive, o aplicativo continua em MONITORANDO;
- se a corrida não existir no momento, isso não gera erro nem corrida falsa;
- a ausência de notificação nunca deve forçar a transição para ENCERRADO.

### Ausência de notificações

Quando nenhuma notificação de corrida estiver disponível, o aplicativo deverá permanecer em estado de monitoramento.

Exemplos:

- motorista não está logado na Uber;
- Uber, 99 ou inDrive não estão ativos;
- motorista não possui oferta disponível;
- motorista está utilizando o aplicativo fora do período de trabalho.

A ausência de notificação não deverá ser apresentada como erro.

### Interface da corrida

A interface da corrida deverá ser compacta e horizontal para permitir que o motorista mantenha a visualização do aplicativo de transporte.

A tela de configuração é uma exceção e poderá utilizar uma interface convencional.

### Contrato visual da corrida

Em estados de corrida, a tela deve alternar apenas entre:

- Mais detalhes;
- Menos detalhes.

O comando sempre informa a ação que será executada, não o estado atual da tela.

### Botão de detalhes

Estado compacto:

Mais detalhes

Estado expandido:

Menos detalhes

### Histórico

O botão "Recolher" será substituído por "Histórico".

O histórico apresentará as últimas corridas utilizando o mesmo padrão visual das notificações.

### Saída

"Sair interface" encerra somente a apresentação da interface da corrida.

O monitoramento permanece ativo.

"Fechar app" encerra o aplicativo, incluindo o monitoramento e elementos de interface flutuante.

### Notificação desconhecida

Uma notificação que não possa ser interpretada não deve gerar uma corrida falsa.

O evento pode ser registrado para diagnóstico futuro sem alterar o estado de corrida.

### Notificação não reconhecida

Uma notificação que não possa ser interpretada não deverá gerar uma corrida falsa.

O evento poderá ser registrado para diagnóstico futuro.

## 6. Histórico de corridas

O histórico deve armazenar o resultado da análise, e não recalculá-lo quando for exibido.

### Contrato mínimo persistido

- Data/hora;
- Plataforma;
- Valor total;
- KM até passageiro;
- KM da viagem;
- KM total;
- Tempo;
- Nota;
- R$/KM;
- Combustível estimado;
- Custo combustível;
- Classificação.

### Regras do histórico

- uma corrida recebida deve virar AnaliseCorrida antes de ser persistida;
- o repositório deve salvar o resultado consolidado da análise;
- consultas futuras devem devolver exatamente o resultado salvo;
- alterações futuras em classificação ou combustível não podem modificar corridas antigas retroativamente.

## 7. Planos Free / Beta / Pro

O aplicativo deve controlar a apresentação de recursos sem alterar a análise, o histórico ou o motor de cálculo.

### Free

- Valor total: visível;
- KM: visível;
- Tempo: visível;
- Nota: visível;
- Classificação visual: visível;
- Histórico: disponível;
- R$/KM: oculto;
- Combustível: oculto;
- Gasto: oculto.

### Beta

- Tudo da Free;
- R$/KM: visível;
- Combustível: visível;
- Gasto: visível;
- Histórico financeiro: visível.

### Pro

- Tudo da Beta;
- custos operacionais completos;
- pneus;
- óleo;
- manutenção;
- depreciação;
- R$/KM líquido;
- relatórios;
- estatísticas;
- recursos avançados.

### Regra de acesso

- o controle de plano não pode recalcular a análise;
- o controle de plano não pode alterar o histórico salvo;
- a interface Android apenas consulta o contrato de recursos do plano ativo.

## 8. Regras de negócio

- o valor por quilômetro é calculado com base na distância total e no valor da corrida;
- a classificação visual deve refletir a percepção de rentabilidade;
- os dados de configuração influenciam o cálculo estimado de custos;
- a experiência deve priorizar clareza e agilidade.

## 9. Critérios de aceitação

O MVP será considerado adequado quando:

- o usuário consegue visualizar rapidamente os dados principais da corrida;
- o sistema apresenta uma análise objetiva de rentabilidade;
- a configuração do veículo e do combustível afeta corretamente os cálculos;
- a interface é simples de entender e usar.

## 10. Fluxo principal do usuário

1. o usuário visualiza uma corrida;
2. o sistema exibe os dados principais em interface compacta;
3. o usuário abre a visão expandida para mais detalhes;
4. o sistema apresenta a análise de rentabilidade;
5. o usuário decide aceitar ou não com base nessas informações.

## 11. Próximos passos

- consolidar a lógica de cálculo no app Android;
- melhorar a experiência visual;
- integrar persistência de configurações;
- evoluir para leitura de notificações e overlay.
