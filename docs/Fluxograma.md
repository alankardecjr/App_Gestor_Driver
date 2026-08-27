# Gestor Driver
## Fluxograma Oficial do Aplicativo

**Projeto:** Gestor Driver  
**Plataforma:** Android  
**Documento:** Fluxograma funcional  
**Status:** Aprovado  
**Versão:** Fluxo principal

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
7. na oferta, mostra a **barra compacta** no topo;
8. no toque do selo, abre a **expandida em overlay (~1/3 da tela)**;
9. Histórico e Configurações abrem a Activity do Gestor; ao sair ou tocar no selo, voltam ao overlay;
10. Ocultar e expiração da oferta retornam ao selo na última posição;
11. Fechar encerra o app após confirmação.

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
│ EXPANDIDA 1/3 │  │ BARRA COMPACTA│
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
   (Activity) (Activity)      ▼
       │          │          SELO
       └──────────┘
         ao sair → SELO

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
  HISTÓRICO           DESCARTA
     │
     ▼
ÚLTIMA CORRIDA ACEITA PERMANECE

          NOTIFICAÇÃO
               │
               ▼
          CORRIDA ATUAL
               │
               ▼
          INTERFACE COMPACTA
               │
               ▼
          USUÁRIO ANALISA
               │
               ├─────────────────┐
               │                 │
               ▼                 ▼
          ACEITA            NÃO ACEITA
               │                 │
               ▼                 ▼
          HISTÓRICO          DESCARTA
               │
               ▼
          CORRIDA ACEITA
               │
               ▼
          NOTIFICAÇÃO EXPIRA
               │
               ▼
          ÚLTIMA CORRIDA ACEITA
               │
               ▼
          NOVA NOTIFICAÇÃO?
               │
          ┌───┴────┐
          │        │
          NÃO      SIM
          │        │
          ▼        ▼
     AGUARDA  NOVA CORRIDA


# 4. Fluxo de minimização

          EXPANDIDA (overlay 1/3)
          │
          ├── 📜 HISTÓRICO → Activity → ao sair / selo → overlay
          ├── ⚙️ CONFIG → Activity → ao sair / selo → overlay
          ├── ⓘ retrai para compacta (3 s sem oferta → selo)
          └── ❎ OCULTAR → selo (fecha histórico e config)

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