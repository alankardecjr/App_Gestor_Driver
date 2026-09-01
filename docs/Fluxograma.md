# Gestor Driver
## Fluxograma Oficial do Aplicativo

**Projeto:** Gestor Driver  
**Plataforma:** Android  
**Documento:** Fluxograma funcional  
**Documento oficial de regras:** [`docs/REGRAS_NEGOCIO.md`](REGRAS_NEGOCIO.md)  
**Versão em foco:** Beta (cálculos visíveis). Pro depois. Free (cálculos ocultos) no lançamento.

**Interfaces congeladas** até a versão Pro (selo, compacta, expandida, histórico, configuração, confirmação de fechar). Sem novos ajustes visuais na Beta.

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
7. na oferta, mostra a **barra compacta** no topo (só o cabeçalho); toque fora dela (mapa/plataforma) recolhe ao selo na hora;
8. `⬇️` ou toque no selo abre a **expandida em overlay** (altura do conteúdo, não 1/3 fixo);
9. Histórico e Configuração abrem **embaixo da expandida** (painéis overlay, exclusivos);
10. Recolher (`⬆️`) volta à compacta; toque **fora** das janelas do Gestor Driver (ou **após 5 s**) retorna ao selo. Aceite detectado também vai ao selo;
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

          EXPANDIDA (overlay, altura do conteúdo)
          │
          ├── 📜 Histórico → painel overlay abaixo (⤴️ Histórico recolhe; ⬅️➡️, deslize ou clique no rótulo trocam Uber/99/inDrive)
          ├── ⚙️ Config → painel overlay abaixo (abas VEÍCULO / CUSTOS / CALIBRAR / APP; ⬅️➡️, deslize ou clique no rótulo; mais alto que o Histórico)
          ├── ⬆️ retrai para compacta (toque fora → selo imediato; senão 5 s → selo, mesmo com oferta)
          └── ❎ Ocultar → selo (fecha histórico e config)

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

Corrida atual (borda grossa, 5 dp):

- 🔴 Ruim = vermelho
- 🟠 Regular = laranja
- 🟢 Boa = verde
- 🔵 Ótima = azul

Janelas Histórico e Configuração: borda cinza fina (2 dp), fundo semitransparente.

Histórico (borda neutra **somente nos itens das listas** Uber / 99 / inDrive): a classificação aparece no marcador colorido. Compacta/expandida com oferta ou item selecionado **mantém a borda colorida**.

Custo (Beta): litros = km total ÷ km/L do combustível atual; gasto = litros × preço desse litro.

Documento completo: `docs/REGRAS_NEGOCIO.md`.