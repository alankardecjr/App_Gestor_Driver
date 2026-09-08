# Gestor Driver
## Fluxograma Oficial do Aplicativo

**Projeto:** Gestor Driver  
**Plataforma:** Android  
**Documento:** Fluxograma funcional  
**Documento oficial de regras:** [`docs/REGRAS_NEGOCIO.md`](REGRAS_NEGOCIO.md)  
**Versão em foco:** Pro 2.0 (`vs-2.0`). Free = mesma UI com calculadora/dashboard ocultos. Beta `1.1.10` congelado em `main`.

**Roteiro ativo:** [`ROTEIRO_PRO.md`](ROTEIRO_PRO.md). UI Beta (02/09/2026) permanece como referência histórica; telas Pro seguem o roteiro oficial 2.0.

---

# 1. Visão geral

O Gestor Driver é um **overlay auxiliar**. Uber, 99 e inDrive continuam visíveis (especialmente o mapa).

O **selo flutuante** (ícone redondo do app) é o estado principal enquanto o monitoramento está ativo.

O aplicativo:

1. inicia;
2. verifica as permissões necessárias;
3. solicita configurações/permissões quando necessário;
4. inicia o monitoramento;
5. exibe o selo flutuante sobre o app de transporte;
6. monitora notificações;
7. na oferta, mostra a **compacta** no topo (R$/Km · Dist. · Tempo · Nota); arrastável; toque nela ou fora **não** a esconde;
8. overlay sobre outros apps = **só** selo · atalhos · compacta (§44);
9. Histórico, Semáforo, Carteira, Despesas, Usuário e Config abrem como **telas nativas** (Activity Compose), todas com o mesmo cabeçalho: ← volta às Opções e selo fecha a tela e retorna ao selo;
10. aceite/expirar/recusar → compacta some; selo permanece no monitoramento;
11. Ocultar e expiração (sem item de histórico selecionado) retornam ao selo na última posição;
12. Fechar encerra o app após confirmação.

---

# 2. Fluxograma principal

```text
┌──────────────────────────────┐
│       USUÁRIO INICIA APP     │
└───────────────┬──────────────┘
                │
                ▼
┌──────────────────────────────┐
│      VERIFICAR PERMISSÕES    │
└───────────────┬──────────────┘
                │
        ┌───────┴───────┐
        │               │
        ▼               ▼
   ┌────────┐      ┌────────────┐
   │   OK   │      │  FALTANDO  │
   └────┬───┘      └──────┬─────┘
        │                  │
        │                  ▼
        │          ┌───────────────┐
        │          │ CONFIGURAÇÃO  │
        │          └───────┬───────┘
        │                  │
        │                  ▼
        │          ┌───────────────┐
        │          │ CONCEDER      │
        │          │ PERMISSÕES    │
        │          └───────┬───────┘
        │                  │
        └──────────────────┘
                │
                ▼
┌──────────────────────────────┐
│    INICIAR MONITORAMENTO     │
└───────────────┬──────────────┘
                │
                ▼
┌──────────────────────────────┐
│       ◉ SELO FLUTUANTE       │
│                              │
│      MONITORAMENTO ATIVO     │
└───────────────┬──────────────┘
                │
        ┌───────┴────────┐
        │                │
        ▼                ▼
   TOQUE NO SELO     NOTIFICAÇÃO
        │                │
        ▼                ▼
┌───────────────┐  ┌───────────────┐
│ EXPANDIDA     │  │ BARRA COMPACTA│
│   (overlay)   │  │   (overlay)   │
└───────┬───────┘  └───────┬───────┘
        │                  │
        └─────────┬────────┘
                  │
                  ▼
       ┌──────────┼───────────┐
       │          │           │
       ▼          ▼           ▼
   HISTÓRICO  CONFIGURAÇÃO  ⓘ / OCULTAR
       │          │           │
   (overlay)  (overlay)       ▼
       │          │          SELO
       └──────────┘
     exclusivos; abaixo da expandida

# 3. Fluxo de uma corrida

          NOTIFICAÇÃO DE OFERTA
               │
               ▼
          CORRIDA ATUAL
               │
               ▼
          INTERFACE COMPACTA
               │
               ▼
     MOTORISTA AGE NA PLATAFORMA
               │
     ┌─────────┴─────────┐
     ▼                   ▼
ACEITE DETECTADO    RECUSA / EXPIRA
     │                   │
     ▼                   ▼
  HISTÓRICO + SELO    SELO (descarta)

Nova oferta substitui a corrida atual. Sem oferta = selo. Histórico só muda no aceite.


# 4. Fluxo de minimização

          ATALHOS (overlay, congelado §44)
          │
          ├── Histórico → tela nativa          (Corridas aceitas)
          ├── Carteira → Dashboard             (Gestor financeiro)
          ├── Despesas → aba Custos/Despesas   (Lançar despesas)
          ├── Semáforo → tela nativa           (Calibrar faixas)
          ├── Usuário → aba Veículo            (Ajustar veiculo)
          ├── Configurar → aba Configurações   (Configurar App)
          └── Fechar → **tela de confirmação** (§44 #6) / encerra  (Encerrar App)

## 4.1 Fechamentos locais e cabeçalho comum

- Todas as abas nativas usam o mesmo cabeçalho visual, com título, seta de retorno e botão com o ícone do selo.
- A seta retorna à aba Opções/Atalhos; o botão selo fecha a tela nativa e retorna ao selo flutuante.
- O card **Detalhes da corrida** usa `X` no cabeçalho. Esse `X` fecha somente o card e devolve o usuário ao Histórico; não encerra o monitoramento.
- A tela compacta usa `X` no cabeçalho superior. Esse `X` fecha somente a compacta e devolve ao selo; não recusa, não aceita, não apaga a oferta e não encerra o monitoramento.
- Toque na área da compacta ou fora dela permanece inerte; somente o `X` executa o fechamento explícito da compacta.

#5. Fluxo de encerramento

           📴 FECHAR
                ↓
            CONFIRMAÇÃO
                 ↓
          ┌───────────────┐
          │               │
          ▼               ▼
       CANCELAR        CONFIRMAR
          │               │
          ▼               ▼
       NADA MUDA      ENCERRA

# 6. Classificação visual (oficial)

Fonte da verdade: R$/KM → faixa → classificação → cor. A interface não escolhe a cor.

Corrida atual (borda grossa, **6 dp**) — **Pro:** 🔴 Ruim · 🟡 Boa · 🟢 Ótima (§40). **Beta histórica:**

- 🔴 Ruim = vermelho
- 🟠 Regular = laranja
- 🟢 Boa = verde
- 🔵 Ótima = azul

Janelas Histórico / Semáforo / Dashboard / Config: borda cinza fina (2 dp), fundo do painel.

Histórico (itens das listas Uber / 99 / inDrive): borda **fina** na cor da classificação. Compacta/expandida com oferta ou item selecionado **mantém a borda colorida grossa**.

Custo (Pro): litros/gasto de combustível + rateio óleo/pneus/IPVA/seguro; lucro = valor − gasto total. Free oculta esses números (🔒). Detalhe: [`REGRAS_NEGOCIO.md`](REGRAS_NEGOCIO.md) §38–40.

Documento completo: `docs/REGRAS_NEGOCIO.md`.