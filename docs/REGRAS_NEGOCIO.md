FLUXOGRAMA_REGRAS_NEGOCIO.

# Gestor Driver — Fluxograma e Regras de Negócio

> Documento de referência oficial para o funcionamento do aplicativo.
>
> **Esta especificação deve ser utilizada como regra-base para as próximas etapas de desenvolvimento.**
>
> **Versão em foco: Beta.** Pro vem depois. Free (cálculos ocultos) só no lançamento na loja. Ver seção 38.
>
> **Interfaces congeladas até a versão Pro.** Ver seção 43.

---

# 1. Objetivo do aplicativo

O **Gestor Driver** é um aplicativo Android destinado a motoristas de aplicativos de transporte.

Sua função principal é:

- monitorar notificações de plataformas de corrida;
- identificar novas ofertas de corrida;
- apresentar as informações da corrida de forma compacta;
- acompanhar a situação da corrida;
- identificar quando o usuário aceita uma corrida na plataforma original;
- armazenar no histórico somente as corridas efetivamente aceitas;
- permitir acesso às configurações e ao histórico;
- manter uma interface principal mínima através do **selo flutuante**.

Plataformas iniciais consideradas:

- Uber
- 99
- inDrive

---

# 2. Regra principal da interface

A partir desta versão:

> **O selo flutuante é a janela principal do Gestor Driver.**

O aplicativo não deve permanecer permanentemente exibindo a tela compacta nem expandida.

O comportamento esperado é:


Aplicativo iniciado
       ↓
Permissões verificadas
       ↓
Monitoramento iniciado
       ↓
◉ Selo flutuante

Quando uma nova corrida for identificada:

◉ Selo
   ↓
Nova notificação de corrida
   ↓
Tela compacta

3. Fluxograma principal
                         ┌─────────────────┐
                         │   INICIAR APP  	 │
                         └────────┬────────┘
                                     │
                                     ▼
                    ┌──────────────────────────┐
                    │ VERIFICAR PERMISSÕES    		│
                    └────────────┬─────────────┘
                                     │
                    ┌────────────┴───────────┐
                    │                                │
                   OK                             FALTANDO
                    │                                │
                    │                                ▼
                    │                    ┌─────────────────┐
                    │                    │    CONFIGURAÇÕES      │
                    │                    └────────┬────────┘
                    │                                │
                    │                                ▼
                    │                        CONCEDER PERMISSÕES
                    │                                │
                    └────────────┬───────────┘
                                     │
                                     ▼
                     ┌──────────────────────┐
                     │        MONITORAMENTO        │
                     │             ATIVO           │
                     └──────────┬───────────┘
                                    │
                                    ▼
                         ┌────────────┐
                         │        ◉      │
                         │       SELO     │
                         └─────┬──────┘
                                 │
                 ┌─────────────┴─────────────┐
                 │                     		      │
                 ▼                       	      ▼
          TOQUE NO SELO           	      NOVA NOTIFICAÇÃO
                 │                  	              │
                 ▼                          	      ▼
          TELA EXPANDIDA           	       TELA COMPACTA
                 │                   		      │
                 │                                    │
        ┌────────┼────────┐                     │
        │        │   		│                     │
        ▼        ▼             ▼                    ▼
    HISTÓRICO CONFIG.  	     CORRIDA            CORIDA ATUAL
        │        │             │                     │
        └────────┼────────┘                     │
                 │                                    │
                 └─────────────┬─────────────┘
                                   │
                                   ▼
                                OCULTAR
                                   │
                                   ▼
                         ┌────────────┐
                         │        ◉       │
                         │      SELO      │
                         └────────────┘

4. Fluxo de inicialização
4.1 Usuário inicia o aplicativo

Ao iniciar:

INICIAR APP
    ↓
VERIFICAR PERMISSÕES

As permissões necessárias devem ser verificadas antes do início efetivo do monitoramento.

5. Permissões necessárias

O aplicativo deverá verificar, conforme os recursos implementados:

localização;
acesso/leitura de notificações;
permissão para sobrepor outros aplicativos;
demais permissões necessárias ao funcionamento do monitoramento.
Regra
Todas as permissões disponíveis
Permissões OK
     ↓
Iniciar monitoramento
     ↓
Exibir selo flutuante
Alguma permissão ausente
Permissão ausente
      ↓
Abrir Configurações
      ↓
Usuário concede permissão
      ↓
Verificar novamente
      ↓
Todas OK?
      ↓
Iniciar monitoramento

O monitoramento não deve ser considerado plenamente ativo enquanto as permissões necessárias não estiverem disponíveis.

6. Selo flutuante

O selo flutuante é a interface principal do aplicativo quando não existe uma corrida sendo apresentada.

Representação:

◉

Características:

permanece visível enquanto o monitoramento estiver ativo;
pode ser arrastado pelo usuário;
deve manter sua posição;
pode ser tocado;
ao ser tocado, abre a interface expandida **logo abaixo do topo**, com altura **ajustada ao conteúdo** (cabeçalho + distâncias/custos + botões). Não usa mais 1/3 fixo da tela, para não cobrir o mapa dos apps de corrida.

Fluxo:

◉
 │
 └── toque
       ↓
   TELA EXPANDIDA

7. Nova corrida

Quando o Gestor Driver identificar uma nova notificação de corrida:

NOTIFICAÇÃO
    ↓
INTERPRETAR DADOS
    ↓
CRIAR CORRIDA ATUAL
    ↓
EXIBIR TELA COMPACTA

A tela compacta é **somente o cabeçalho** (sem distâncias nem custos). Fica no topo, sobre o mapa.

Cabeçalho oficial:

R$/KM | VALOR | DIST. | TEMPO | NOTA | ℹ️ ⬇️

Exemplo: 💵 2,38 | 💰 38,00 | 🛞 16,0 KM | 🕐 28 min | ⭐ 4,9 | ⬇️

A compacta **não** tem botões Fechar / Config / Ocultar / Histórico. `⬇️` (ou toque **na própria barra**) abre a expandida. Sem oferta, os valores são `—` e a borda é neutra.

**Toque fora da compacta → selo:** enquanto a barra compacta estiver visível — por oferta recém-detectada ou por retração a partir da expandida — um toque em qualquer região da tela que **não** pertença às janelas do Gestor Driver recolhe a interface **imediatamente** para o selo flutuante. O toque segue para o aplicativo da plataforma (Uber, 99 ou inDrive), liberando controles cobertos pela barra, em especial o recusar da 99. A oferta permanece em monitoramento. Toque **sobre** a compacta continua expandindo o painel.

**Oferta expirada → selo na hora:** se a leitura da tela voltar ao mapa/home da plataforma (`Você está online/conectado/offline`, sem o par `N min (X km)` do card e sem `Aceitar`), a compacta some **na primeira leitura**. Não espera várias capturas. O botão **Aceitar** da oferta **não** grava histórico. Histórico só com assinatura de aceite real (ex.: `Aceitei por engano`, `local de partida`, ponto de encontro). Card Uber típico: valor `R$`, taxa `/km` ignorada, bônus `+R$` ignorado, nota `4,99 (165)`, embarque `5 min (1.2 km)` e destino `5 minutos (1.3 km)` — o destino muitas vezes só no OCR; nós da acessibilidade sozinhos não bastam.

8. Regra da corrida atual

A corrida recebida inicialmente é considerada:

CORRIDA ATUAL

Ela não entra automaticamente no histórico.

Isso é uma regra fundamental do aplicativo.

9. Aceite da corrida

O usuário não aceita a corrida dentro do Gestor Driver.

O aceite acontece no aplicativo original:

Uber
99
inDrive

Exemplo:

Gestor Driver identifica oferta
             ↓
       Tela compacta
             ↓
Usuário interage com Uber/99/inDrive
             ↓
Usuário ACEITA a corrida
             ↓
Gestor Driver identifica o aceite
             ↓
Registrar corrida aceita no histórico
             ↓
Ocultar automaticamente → selo (mapa da plataforma livre)

10. Regra oficial do histórico

O histórico armazenará somente corridas que forem efetivamente aceitas pelo usuário na plataforma original.

Portanto:

Corrida recebida
      ↓
NÃO aceita
      ↓
NÃO entra no histórico

E:

Corrida recebida
      ↓
Usuário aceita na Uber/99/inDrive
      ↓
Gestor Driver identifica o aceite
      ↓
ENTRA NO HISTÓRICO

11. Responsabilidade do Gestor Driver no aceite

O Gestor Driver deverá:

monitorar os eventos/notificações disponíveis;
identificar a corrida atual;
detectar evidências de que o usuário aceitou aquela corrida;
associar o aceite à corrida correta;
registrar a corrida no histórico;
evitar duplicidade.

O Gestor Driver não executa o aceite em nome do usuário.

12. Histórico

O histórico contém exclusivamente:

CORRIDAS ACEITAS

Abre como **painel overlay abaixo da expandida** (não tela cheia). Abas: Uber | 99 | inDrive. Mais recente primeiro. Toque no item preenche a expandida (sem data/hora no painel da corrida).

Cabeçalho oficial de cada item:

DATA | HORA | R$/KM | VALOR | KM | TEMPO | NOTA | ⭐

Não adicionar outros campos ao cabeçalho sem alteração formal desta especificação.

13. Histórico e corrida atual são conceitos diferentes

Corrida atual =

Representa a oferta/corrida que está sendo acompanhada neste momento.

notificação → corrida atual

Histórico =

Representa corridas que já tiveram aceite identificado.

aceite identificado → histórico

Portanto:

                    ┌─────────────────┐
                    │    CORRIDA ATUAL 	    │
                    └────────┬────────┘
                                │
                         aceite detectado
                                │
                                ▼
                    ┌─────────────────┐
                    │       HISTÓRICO       │
                    └─────────────────┘

14. Expansão da interface

A tela compacta pode ser expandida durante a exibição de corrida atual.

COMPACTA
   ↓
  ⬇️
   ↓
EXPANDIDA (altura do conteúdo, overlay)

Na expandida: Distâncias (até o passageiro, até o destino, total) e Custos (consumo estimado em litros, gasto, lucro) + botões Fechar · Config · Ocultar · Histórico. Cabeçalho com a mesma fonte da compacta; colunas alinhadas (rótulo à esquerda, valor à direita).

E:

EXPANDIDA
   ↓
  ⬆️
   ↓
COMPACTA
   ↓
toque fora das janelas do Gestor Driver → SELO (imediato)
   ou
após 5 segundos sem esse toque → SELO

(mesmo que ainda exista oferta). Toque no selo reabre a expandida. Toque na própria compacta reabre a expandida.

15. Comportamento durante uma corrida

Quando uma notificação de corrida estiver ativa:

Nova notificação
       ↓
Tela compacta
       ↓
Usuário pode expandir
       ↓
Tela expandida

Se o usuário expandir a interface, a corrida atual deve continuar sendo exibida enquanto o estado da corrida permitir.

16. Expiração da corrida

Quando a corrida atual deixar de estar disponível (recusa / some a notificação) **sem aceite**:

 CORRIDA ATUAL
      ↓
    EXPIRA
      ↓
não grava histórico

Se a expandida estiver aberta **com um item do histórico selecionado**, restaura essa corrida na expandida.

Caso contrário, volta ao **selo** (não reabre automaticamente a última aceita).

Se chegar uma nova corrida:

Nova notificação
       ↓
Atualizar corrida atual
       ↓
Exibir nova corrida 

17. Nova notificação após corrida anterior

Quando uma nova corrida for detectada:

CORRIDA ANTERIOR
       ↓
NOVA NOTIFICAÇÃO
       ↓
NOVA CORRIDA ATUAL
       ↓
ATUALIZAR INTERFACE

A nova corrida deve substituir a corrida atual apresentada.

O histórico somente será alterado caso o aceite da nova corrida seja posteriormente identificado.

18. Ocultar

O botão:

❎ Ocultar

não encerra o aplicativo.

Sua função oficial é:

Minimizar o Gestor Driver para o selo flutuante.

Fluxo:

TELA EXPANDIDA
      ↓
❎ OCULTAR
      ↓
fechar Histórico, se aberto
      ↓
fechar Configuração, se aberta
      ↓
fechar estado expandido
      ↓
MONITORAMENTO CONTINUA
      ↓
◉ SELO FLUTUANTE

19. Ocultar com histórico aberto

Se o histórico estiver aberto:

HISTÓRICO
    ↓
❎ OCULTAR
    ↓
FECHAR HISTÓRICO
    ↓
FECHAR INTERFACE EXPANDIDA
    ↓
    ◉

20. Ocultar com configuração aberta

Se a configuração estiver aberta:

CONFIGURAÇÃO
     ↓
❎ OCULTAR
     ↓
FECHAR CONFIGURAÇÃO
     ↓
FECHAR INTERFACE EXPANDIDA
     ↓
     ◉

21. Ocultar nunca encerra o monitoramento

Após ocultar:

interface = selo
monitoramento = ativo

O Gestor Driver continua podendo receber e processar novas notificações.

22. Reabrir pelo selo

Quando o usuário tocar no selo:

 ◉
 ↓
TOQUE
 ↓
ABRIR INTERFACE
 ↓
TELA EXPANDIDA

O monitoramento continua ativo.

23. Configurações

A configuração abre como **painel overlay abaixo da expandida** (mesmo recorte do histórico). Abas:

- **VEÍCULO** — marca, modelo, versão, ano, final da placa, consumo gasolina (km/L), consumo etanol (km/L), combustível atual (um checkbox).
- **CLASSIFICAÇÃO** — faixas R$/km encadeadas (Ruim MIN fixo, Ótima MAX fixo).
- **APP** — permissões, **preços** gasolina/etanol (R$/L), Maps ou Waze.

Permissão faltando: abrir a aba APP e destacar o que falta.

Custo da corrida usa **combustível atual + km/L desse combustível + preço do litro na aba APP**. Não misturar gasolina e etanol na mesma conta.

**CANCELAR** fecha o painel e **descarta** o rascunho (os campos voltam ao que está gravado). Só **SALVAR** persiste. Fechar Config pelo botão ⤴️ Config também descarta, igual ao Cancelar.

Fluxo:

 EXPANDIDA
 ↓
⚙️ Config
 ↓
painel CONFIGURAÇÃO (abaixo)


24. Histórico e configuração

Histórico e configuração são **painéis overlay distintos**, abaixo da expandida. Não abrem ao mesmo tempo.

Com histórico aberto, o botão vira **⤴️ Histórico** (recolhe o painel).

HISTÓRICO ABERTO
      ↓
CONFIGURAÇÃO
      ↓
FECHAR HISTÓRICO
      ↓
ABRIR CONFIGURAÇÃO

E:

CONFIGURAÇÃO ABERTA
      ↓
HISTÓRICO
      ↓
FECHAR CONFIGURAÇÃO
      ↓
ABRIR HISTÓRICO

25. Botão Fechar

O botão:

📴 Fechar

possui comportamento diferente de Ocultar.

Regra

Fechar encerra o aplicativo/monitoramento.

Antes de encerrar, o aplicativo deve solicitar confirmação.

Fluxo:

📴 FECHAR
     ↓
CONFIRMAÇÃO
     ↓
┌──────────────┐
│ Deseja fechar?    │
└───────┬──────┘
          │
   ┌────┴────┐
   │            │
   ▼            ▼
CANCELAR     CONFIRMAR
   │            │
   ▼           ▼
NÃO ALTERA   ENCERRA
   │           │
   │           ├── monitoramento = false
   │           ├── remover selo
   │           └── finalizar app
   │
   ▼
PERMANECE
COMO ESTÁ

26. Cancelar fechamento

Se o usuário selecionar:

Cancelar

nenhuma alteração deve ser realizada.

Deve permanecer:

monitoramento = ativo

e:

selo = visível

quando aplicável.

27. Confirmar fechamento

Se o usuário confirmar:

Confirmar
    ↓
monitorando = false
    ↓
remover selo
    ↓
encerrar interface
    ↓
finalizar aplicativo

28. Diferença entre Ocultar e Fechar

Ação		Resultado		Monitoramento
❎ Ocultar	Minimiza para selo	Continua ativo
◉ Toque no selo	Reabre interface	Continua ativo
📴 Fechar + Cancelar	Nenhuma alteração	Continua ativo
📴 Fechar + Confirmar	Encerra aplicativo	Encerrado

29. Fluxo completo do negócio

                            INICIAR APP
                                 │
                                 ▼
                        VERIFICAR PERMISSÕES
                                  │
                   ┌──────────┴──────────┐
                   │                            │
                  OK                         FALTANDO
                   │                            │
                   │                            ▼
                   │                       CONFIGURAÇÃO
                   │                            │
                   │                    CONCEDER PERMISSÕES
                   │                            │
                   └──────────┬──────────┘
                                 │
                                 ▼
                        MONITORAMENTO ATIVO
                                 │
                                 ▼
                             ◉ SELO
                                 │
               ┌────────────┴──────────────┐
               │                                    │
               ▼                                    ▼
         TOQUE NO SELO                         NOTIFICAÇÃO
               │                       		    │
               ▼                                   ▼
          EXPANDIDA                           CORRIDA ATUAL
               │                                    │
               ▼                                   ▼
            OPÇÕES                           USUÁRIO ANALISA
               │                                   │
       ┌───────┼────────┐                    │
       │         │           │                    ▼
       ▼         ▼          ▼                ACEITA NA
   HISTÓRICO   CONFIG.    CORRIDA             PLATAFORMA
       │         │           │                    │
       │         │           │                    ▼
       │         │           │             GESTOR DETECTA
       │         │           │                    │
       │         │           │                    ▼
       │         │           │               HISTÓRICO
       │         │           │
       └───────┴────────┘
               │
               ▼
           ❎ OCULTAR
               │
               ▼
               ◉
               │
               └──────→ monitoramento continua

30. Fluxo de fechamento completo
                    📴 FECHAR
                        │
                        ▼
                  CONFIRMAÇÃO
                        │
                ┌───────┴───────┐
                │               │
                ▼               ▼
             CANCELAR        CONFIRMAR
                │               │
                ▼               ▼
        NENHUMA ALTERAÇÃO   monitorando = false
                │               │
                ▼               ▼
          CONTINUA ATIVO     remover selo
                                │
                                ▼
                         encerrar aplicativo

31. Regras de estado

O aplicativo deverá manter estados coerentes.

Monitoramento
monitorando = true

significa que o Gestor Driver continua ativo no monitoramento.

monitorando = false

significa que o monitoramento foi encerrado.

Selo

Quando:

monitorando = true

e a interface estiver minimizada:

selo = visível
Ocultar

> Ocultar deve:

fechar telas auxiliares
↓
minimizar interface
↓
mostrar selo
↓
manter monitoramento

> Fechar deve:

Fechar confirmado deve:

encerrar monitoramento
↓
remover selo
↓
finalizar aplicativo

32. Regras de integridade do histórico

O sistema deve garantir:

Regra 1

Oferta recebida não significa aceite.

oferta ≠ aceite
Regra 2

Somente o aceite identificado deve gerar histórico.

aceite detectado → histórico
Regra 3

Não duplicar corrida aceita.

mesma corrida aceita
       ↓
não inserir novamente

Regra 4

A corrida atual não deve ser confundida com o histórico.

corrida atual ≠ histórico

33. Princípios fundamentais do projeto

As seguintes regras são consideradas fundamentais:

O selo flutuante é a janela principal.
O monitoramento ocorre em segundo plano enquanto estiver ativo.
A tela compacta aparece para apresentar uma nova corrida.
Com a compacta visível, toque fora das janelas do Gestor Driver volta ao selo na hora.
O usuário aceita a corrida na Uber, 99 ou inDrive.
O Gestor Driver identifica o aceite.
Somente corridas aceitas entram no histórico.
Ocultar minimiza para o selo.
Ocultar não encerra o monitoramento.
O selo pode reabrir a interface expandida.
Fechar sempre solicita confirmação.
Cancelar fechamento não altera o estado.
Confirmar fechamento encerra monitoramento e aplicativo.
Histórico e configuração devem respeitar a exclusividade de telas.
Uma nova corrida substitui a corrida atual.
A implementação deve evitar duplicidade no histórico.

34. Regra de ouro

O Gestor Driver acompanha a oferta, mas somente registra no histórico aquilo que o usuário efetivamente aceitou na plataforma de origem e cujo aceite foi identificado pelo Gestor Driver.

35. Estado final esperado

Quando o aplicativo estiver funcionando corretamente:

USUÁRIO
  │
  ▼
INICIA APP
  │
  ▼
PERMISSÕES
  │
  ├── faltando → CONFIGURAÇÃO
  │
  └── OK
       │
       ▼
MONITORAMENTO
       │
       ▼
      ◉
   SELO FLUTUANTE
       │
       ├── toque → COMPACTA / EXPANDIDA
       │
       └── corrida → CORRIDA ATUAL
                           │
                           ▼
                  usuário aceita na
                  Uber/99/inDrive
                           │
                           ▼
                  aceite identificado
                           │
                           ▼
                       HISTÓRICO
                           │
                           ▼
                       ❎ OCULTAR
                           │
                           ▼
                          ◉

36. A cor da borda da interface da corrida deve representar visualmente a classificação calculada pelo Gestor Driver, permitindo que o motorista identifique rapidamente se a oferta é ruim ou boa, sem precisar ler todos os dados.

Classificação por cor da borda

Cor da borda		Significado

🔴 Ruim	Vermelha	Corrida pouco vantajosa
🟠 Regular		Laranja	Corrida aceitável, mas abaixo do ideal
🟢 Boa	Verde		Corrida vantajosa
🔵 Ótima		Azul	Corrida muito vantajosa

Regra visual

A cor da borda  externa (mais expessa), da tela compacta/expandida da corrida atual deve assumir a cor correspondente à classificação:

Regra importante

A cor não deve ser escolhida manualmente pela interface.

O fluxo deve ser:

Dados da corrida
       ↓
Cálculo R$/KM
       ↓
Regras de classificação
       ↓
RUIM / REGULAR / BOA / ÓTIMA
       ↓
Cor correspondente
       ↓
Borda da interface

Assim, a classificação é a fonte da verdade e a cor é apenas sua representação visual.

Também vale para a interface compacta

A mesma classificação deve controlar a borda da janela compacta/expandida:

Nova corrida
     ↓
Calcula R$/KM
     ↓
Classifica
     ↓
┌─────────────────────┐
│                    	     │ ← borda colorida
│   dados da corrida         │
│                            │
└─────────────────────┘

Isso mantém uma linguagem visual consistente entre corrida atual, tela expandida e histórico, sem depender exclusivamente de texto.

Regra para nossa documentação:

🔴 Ruim = vermelho | 🟠 Regular = laranja | 🟢 Boa = verde | 🔵 Ótima = azul.

37.A diferença entre a cor da borda da corrida atual e historico, é porque a corrida atual é uma oferta em análise, enquanto o histórico representa uma corrida já aceita.

Corrida atual

A borda é dinâmica e colorida conforme a classificação:

🔴 Ruim → borda vermelha
🟠 Regular → borda laranja
🟢 Boa → borda verde
🔵 Ótima → borda azul

A borda funciona como alerta visual imediato para ajudar o motorista a decidir sobre a oferta.

Histórico

No histórico, as corridas já aceitas são exibidas **em listas** (Uber | 99 | inDrive). Portanto, a borda de cada **item da lista** não deve ter o mesmo destaque da corrida atual.

A borda neutra **não** se aplica ao painel compacta/expandida quando uma corrida do histórico está selecionada: esse painel continua com a borda colorida da classificação.

Recomendo:

borda neutra/padrão;
classificação indicada por um pequeno marcador ou texto colorido;
manter a cor da classificação sem transformar cada item histórico em uma caixa fortemente colorida.

Exemplo:

╭────────────────────────────╮
│ R$/KM │ R$/TOTAL │ KM │ TEMPO │ ⭐│
│ 2,38  │ R$ 38,00 │16,0│ 24min │ 🟢│
╰────────────────────────────╯

Regra visual definitiva

                 CORRIDA ATUAL
                       │
                       ▼
                 classificação
                       │
                       ▼
                BORDA COLORIDA
                       │
        ┌──────────┼──────────────┐
        ▼             ▼         ▼	      ▼
      🔴 Ruim   🟠 Regular   🟢 Boa  🔵 Ótima
                                             
                    


                  HISTÓRICO
                       │
                       ▼
                borda neutra
                       │
                       ▼
             classificação indicada
               por marcador/cor

Em resumo:

Painel compacta/expandida (oferta atual ou corrida do histórico selecionada) = borda colorida e destacada.
Itens das listas do Histórico = borda neutra, com a classificação no marcador/texto colorido.

38. Versões do produto (Beta, Pro, Free)

Ordem: **Beta agora → Pro depois → Free no lançamento**.

| Recurso | Free (loja) | Beta | Pro |
| --- | --- | --- | --- |
| Overlay, selo, histórico de aceites | sim | sim | sim |
| Valor, DIST., tempo, nota, cor da borda | sim | sim | sim |
| R$/KM, litros, gasto, lucro (combustível) | **não mostra** | mostra | mostra |
| Custo operacional, R$/km líquido, relatórios | não | não | sim |

O motor calcula sempre; no Free a UI oculta os números financeiros.

39. Custo estimado (combustível)

Litros = km total ÷ km/L do **combustível atual**.  
Gasto = litros × **preço do litro** desse combustível.  
Lucro estimado (Beta) = valor da corrida − gasto.

Gasolina: litro mais caro, mais km/L. Etanol: litro mais barato, menos km/L. Os dois entram na conta via combustível marcado + preços da aba **CUSTOS**. Snapshot no momento da oferta; mudar preço depois não recalcula histórico.

40. Faixas padrão de classificação (R$/km)

Sem sobreposição, passo 0,01. Ruim MIN e Ótima MAX são rótulos fixos.

| Faixa | MIN | MAX | Borda |
| --- | --- | --- | --- |
| Ruim | MIN | 1,19 | vermelha |
| Regular | 1,20 | 1,59 | laranja |
| Boa | 1,60 | 1,99 | verde |
| Ótima | 2,00 | MAX | azul |

Editar um limite ajusta o vizinho. O motorista pode alterar as faixas na aba **APP**.

41. Pacotes monitorados (Beta)

O listener só processa notificações destes apps de **motorista** (não o app de passageiro):

| Plataforma | Pacotes reconhecidos |
| --- | --- |
| Uber | `com.ubercab.driver` |
| 99 | `com.app99.driver`, `com.taxis99.driver`, `com.taxis99` |
| inDrive | `sinet.startup.inDriver`, `com.sis.android.indriver`, `com.indrive.android` |

Eles estão declarados em `<queries>` no manifesto (Android 11+) para o Gestor poder **ver se estão instalados**. A aba APP mostra UBER / 99 / INDRIVE com 🆗 (instalado) ou ❎ (não encontrado). Sem o app de motorista instalado, não haverá ofertas.

42. Tratamento de exceções (Beta)

- Parser de notificação: falha vira “não reconhecida”, sem crash.
- Mapper de extras da notificação: extras inválidos são ignorados.
- Listener: qualquer falha ao processar uma postagem é registrada no log diagnóstico (`EXCECAO`) e o serviço segue.
- Overlay: `addView` / `startForegroundService` / `stopService` em `runCatching`.
- Configuração (DataStore): falha ao carregar usa valores padrão; falha ao salvar não derruba o overlay.

O aceite **não** é feito pelo Gestor. Duplicidade de histórico é bloqueada pela chave da corrida.

43. Congelamento das interfaces (até a versão Pro)

As telas da Beta estão **congeladas**. Não alterar layout, tamanhos, espaçamentos, cores, textos visíveis, abas ou gestos da interface até o início da versão Pro.

Telas congeladas:

- selo flutuante
- compacta
- expandida (cabeçalho, DISTÂNCIAS, CUSTOS (ESTIMADO), botões)
- histórico (abas Uber / 99 / inDrive, deslize horizontal)
- configuração (VEÍCULO / CUSTOS / APP, deslize horizontal, CANCELAR / SALVAR)
- confirmação de fechar

Permitido na Beta, sem mudar a UI: calibrar parser e aceite, correção de crash/bug de funcionamento, persistência e monitoramento.

Mudança visual só volta no **Pro** (óleo, pneus, trajeto no histórico, R$ líquido, relatórios).

