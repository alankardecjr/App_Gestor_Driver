# Gestor Driver
## Fluxograma Oficial do Aplicativo

**Projeto:** Gestor Driver  
**Plataforma:** Android  
**Documento:** Fluxograma funcional  
**Status:** Aprovado  
**Versão:** Fluxo principal

---

# 1. Visão geral

O Gestor Driver funciona como um aplicativo de monitoramento de corridas.

O **selo flutuante** representa o estado principal do aplicativo enquanto o monitoramento está ativo.

O aplicativo:

1. inicia;
2. verifica as permissões necessárias;
3. solicita configurações/permissões quando necessário;
4. inicia o monitoramento;
5. exibe o selo flutuante;
6. monitora notificações;
7. apresenta a corrida atual;
8. permite expansão;
9. permite acesso ao histórico e configurações;
10. permite minimizar novamente para o selo;
11. permite encerrar o aplicativo mediante confirmação.

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
│   COMPACTA    │  │ CORRIDA ATUAL │
└───────┬───────┘  └───────┬───────┘
        │                  │
        └─────────┬────────┘
                  │
                  ▼
           ┌─────────────┐
           │  EXPANDIDA  │
           └──────┬──────┘
                  │
       ┌──────────┼───────────┐
       │          │           │
       ▼          ▼           ▼
   HISTÓRICO  CONFIGURAÇÃO  CORRIDA
       │          │           │
       └──────────┼───────────┘
                  │
                  ▼
              ❎ OCULTAR
                  │
                  ▼
           FECHAR CONTEXTO
                  │
                  ▼
                  ◉

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

          EXPANDIDA
          │
          ├── HISTÓRICO
          │      │
          │      ▼
          │   FECHAR
          │
          ├── CONFIGURAÇÃO
          │      │
          │      ▼
          │   FECHAR
          │
          └── CORRIDA
               │
               ▼
          COMPACTA
               │
               ▼
          ❎ OCULTAR
               │
               ▼
               ◉

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