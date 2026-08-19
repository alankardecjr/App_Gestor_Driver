Perfeito. A partir de agora, **este será o roteiro oficial do Gestor Driver**. Vamos seguir a ordem abaixo até a conclusão do projeto, sem voltar a introduzir uma lógica de “aceitar corrida” dentro do Gestor Driver.

# Gestor Driver — Roteiro Oficial de Implementação

## 1. Regra de negócio definitiva

O Gestor Driver **não aceita corridas**.

Quem aceita ou recusa a corrida é o motorista diretamente no:

* Uber
* 99
* inDrive

O Gestor Driver apenas:

1. identifica a oferta;
2. apresenta os dados;
3. monitora o que acontece na plataforma;
4. identifica quando a corrida foi efetivamente aceita;
5. somente então registra a corrida no histórico.

### Fluxo oficial

```text
                 UBER / 99 / INDRIVE
                        │
                        │ Nova oferta
                        ▼
              GESTOR DRIVER IDENTIFICA
                        │
                        ▼
                 CORRIDA ATUAL
                        │
                        ▼
                TELA COMPACTA
                        │
             ┌──────────┴──────────┐
             │                     │
             ▼                     ▼
       USUÁRIO ACEITA        USUÁRIO RECUSA
       NA PLATAFORMA         OU IGNORA
             │                     │
             ▼                     ▼
     GESTOR DETECTA ACEITE       DESCARTA
             │
             ▼
       REGISTRA CORRIDA
             │
             ▼
          HISTÓRICO
```

---

# 2. Arquitetura funcional definitiva

Vamos trabalhar com quatro conceitos separados:

```text
CORRIDA ATUAL
    ↓
oferta que está sendo monitorada

CORRIDA ACEITA
    ↓
oferta cujo aceite foi identificado

HISTÓRICO
    ↓
somente corridas aceitas

SELO
    ↓
interface principal do aplicativo
```

Isso evita misturar oferta, corrida aceita e histórico.

---

# 3. ETAPA 1 — Corrigir o ciclo da corrida

### Prioridade 🔴 CRÍTICA

Primeiro vamos corrigir o comportamento atual.

### Hoje

```text
Notificação
    ↓
AnaliseCorrida
    ↓
Histórico
```

### Correto

```text
Notificação
    ↓
AnaliseCorrida
    ↓
Corrida atual
    ↓
aguarda resultado na plataforma
    │
    ├── aceite detectado → histórico
    │
    └── recusa/expiração → descarta
```

### Arquivos principais

```text
AppViewModel.kt
PresentationBuilder.kt
RideNotificationProcessor.kt
RideNotificationBus.kt
```

### Não faremos

```kotlin
fun aceitarCorrida()
```

como botão ou ação da interface.

---

# 4. ETAPA 2 — Detectar o aceite

### Prioridade 🔴 CRÍTICA

Essa é uma das partes mais importantes do projeto.

Precisamos determinar exatamente quais eventos/notificações permitem identificar:

```text
OFERTA
   ↓
ACEITA
```

para cada plataforma.

### Arquitetura

```text
NotificationListenerService
           ↓
RideNotificationProcessor
           ↓
classificação do evento
           ↓
┌──────────┴───────────┐
│                      │
NOVA OFERTA        ACEITE DETECTADO
│                      │
▼                      ▼
Corrida atual      Registrar histórico
```

### Regra fundamental

**Não vamos presumir que qualquer nova notificação significa aceite.**

O sistema só deverá registrar a corrida quando existir um evento suficientemente confiável de aceite.

Se não for possível determinar o aceite por notificação, vamos investigar outra evidência disponível no Android antes de implementar uma regra aproximada.

---

# 5. ETAPA 3 — Corrida atual

### Prioridade 🔴 ALTA

A `analiseAtual` será a representação da oferta atualmente monitorada.

```text
Nova oferta
     ↓
analiseAtual
     ↓
interface compacta
```

Uma nova oferta substitui a anterior:

```text
Oferta A
   ↓
Oferta B
   ↓
Oferta C
```

Somente a corrida cujo aceite for detectado será enviada ao histórico.

---

# 6. ETAPA 4 — Expiração da corrida

### Prioridade 🔴 ALTA

Quando uma oferta deixa de ser válida sem aceite identificado:

```text
Corrida atual
     ↓
expira
     ↓
não entra no histórico
```

Se houver uma corrida aceita anteriormente:

```text
Última corrida aceita
        ↓
continua disponível
```

Assim:

```text
OFERTA ATUAL
     ↓
EXPIRA
     ↓
ÚLTIMA CORRIDA ACEITA
```

Isso atende à nova regra definida.

---

# 7. ETAPA 5 — Histórico

### Prioridade 🔴 ALTA

O histórico passa a ter uma regra simples e rígida:

> **Histórico = somente corridas aceitas e detectadas pelo Gestor Driver.**

Não haverá:

* corrida de demonstração em produção;
* oferta recusada;
* oferta ignorada;
* oferta expirada;
* corrida apenas analisada.

### Fluxo

```text
ACEITE DETECTADO
      ↓
HistoricoItemPresentation
      ↓
persistência
      ↓
Histórico
```

---

# 8. ETAPA 6 — Selo flutuante

### Prioridade 🔴 ALTA

O selo passa a ser oficialmente a **janela principal do Gestor Driver**.

```text
INICIAR APP
     ↓
PERMISSÕES OK
     ↓
MONITORAMENTO
     ↓
SELO FLUTUANTE
```

O selo deverá:

* ser arrastável;
* permanecer sobre outros aplicativos;
* indicar que o monitoramento está ativo;
* responder ao toque;
* abrir a interface compacta.

---

# 9. ETAPA 7 — Notificação → tela compacta

Quando uma nova oferta for detectada:

```text
SELO
 ↓
NOTIFICAÇÃO DE CORRIDA
 ↓
TELA COMPACTA
```

A tela compacta exibirá os dados definidos no layout congelado:

```text
R$/KM | VALOR | DIST. | TEMPO | NOTA | ℹ️

💵2,38 | 💰38,00 | 🛞16,02 | 🕐24,6 | ⭐4,98 | ⬇️/⬆️
```

Não haverá botão de aceite nessa tela.

O motorista continuará utilizando o aplicativo da plataforma para aceitar ou recusar.

---

# 10. ETAPA 8 — Expansão

O usuário poderá tocar no controle:

```text
⬇️
```

para expandir.

A interface expandida poderá disponibilizar:

```text
Corrida
Histórico
Configurações
Ocultar
Fechar
```

A corrida continuará sendo monitorada.

---

# 11. ETAPA 9 — Histórico durante uma corrida

O usuário poderá consultar o histórico sem perder o monitoramento.

```text
Corrida atual
     │
     ├── Histórico
     │
     └── Configurações
```

A corrida atual não será automaticamente adicionada ao histórico.

Somente:

```text
aceite detectado
```

gera persistência.

---

# 12. ETAPA 10 — Configurações

### Prioridade 🟠

Configurações continuarão separadas da corrida atual.

Precisamos implementar:

### Veículo

```text
Marca
Modelo
Versão
Ano
Consumo gasolina
Consumo etanol
Combustível utilizado
Preço gasolina
Preço etanol
```

### Aplicativo

```text
Permissões
Aplicativo de navegação
Classificação das corridas
```

### Persistência

Usaremos:

```text
DataStore
   ↓
configurações
```

e posteriormente:

```text
Room
   ↓
histórico de corridas aceitas
```

---

# 13. ETAPA 11 — Inicialização

Fluxo oficial:

```text
USUÁRIO INICIA APP
        ↓
VERIFICAR PERMISSÕES
        │
   ┌────┴────┐
   │         │
  OK       FALTANDO
   │         │
   │         ▼
   │    CONFIGURAÇÕES
   │         │
   │         ▼
   │   CONCEDER PERMISSÕES
   │         │
   └────┬────┘
        ▼
 MONITORAMENTO
        ↓
   SELO FLUTUANTE
```

Permissões necessárias serão verificadas de forma real, sem assumir que foram concedidas.

---

# 14. ETAPA 12 — Ocultar

A regra oficial permanece:

```text
OCULTAR
   ↓
fecha histórico/configuração
   ↓
fecha interface expandida
   ↓
retorna para selo
```

Resultado:

```text
TELA
 ↓
OCULTAR
 ↓
◉ SELO
```

O monitoramento continua:

```text
monitorando = true
```

---

# 15. ETAPA 13 — Selo → compacta

Ao tocar no selo:

```text
◉
 ↓
TELA COMPACTA
```

Se existir uma corrida atual válida:

```text
SELO
 ↓
CORRIDA ATUAL
 ↓
COMPACTA
```

Se não houver corrida atual:

```text
SELO
 ↓
COMPACTA / ESTADO SEM CORRIDA
```

O comportamento exato dessa tela será validado durante a implementação do overlay.

---

# 16. ETAPA 14 — Fechar

O botão:

```text
📴 Fechar
```

não minimiza.

Ele encerra o aplicativo após confirmação.

```text
📴 FECHAR
    ↓
CONFIRMAÇÃO
    │
    ├── CANCELAR
    │      ↓
    │  nenhuma alteração
    │
    └── CONFIRMAR
           ↓
    monitorando = false
           ↓
       remover selo
           ↓
      finalizar app
```

---

# 17. ETAPA 15 — Persistência

### DataStore

Configurações:

```text
veículo
combustível
preços
navegação
classificação
preferências
```

### Room

Somente histórico:

```text
corridas aceitas
```

Fluxo:

```text
ACEITE DETECTADO
      ↓
Repository
      ↓
Room
      ↓
HISTÓRICO
```

---

# 18. ETAPA 16 — Testes

Antes de considerar o projeto finalizado, teremos que testar:

### Ofertas

* [ ] nova oferta Uber;
* [ ] nova oferta 99;
* [ ] nova oferta inDrive;
* [ ] oferta substituindo oferta anterior;
* [ ] oferta expirando;
* [ ] oferta recusada;
* [ ] oferta ignorada.

### Aceite

* [ ] aceite Uber detectado;
* [ ] aceite 99 detectado;
* [ ] aceite inDrive detectado;
* [ ] corrida aceita entra no histórico;
* [ ] corrida não aceita não entra no histórico.

### Interface

* [ ] selo aparece;
* [ ] selo arrasta;
* [ ] selo abre compacta;
* [ ] compacta expande;
* [ ] histórico abre;
* [ ] configuração abre;
* [ ] ocultar retorna ao selo;
* [ ] monitoramento continua.

### Fechamento

* [ ] Fechar abre confirmação;
* [ ] Cancelar não altera nada;
* [ ] Confirmar remove selo;
* [ ] monitoramento é encerrado.

---

# 19. ETAPA 17 — Teste real

Depois dos testes unitários:

```text
APK DEBUG
   ↓
CELULAR REAL
   ↓
Permissões
   ↓
Selo
   ↓
Uber
   ↓
99
   ↓
inDrive
```

Vamos verificar o comportamento real das notificações.

**Somente depois desses testes definiremos definitivamente como cada plataforma sinaliza o aceite.**

Essa precaução é importante para não criar uma implementação baseada em uma suposição falsa sobre as notificações das plataformas.

---

# 20. ETAPA 18 — Documentação

Depois que o comportamento estiver funcionando:

```text
README.md
docs/Fluxograma.md
docs/Roadmap.md
docs/Gestor_Driver_MVP_Especificacao_v1.0.md
```

serão alinhados à implementação real.

A documentação oficial deverá refletir:

```text
Oferta ≠ corrida aceita
```

e:

```text
Histórico = somente corridas aceitas
```

---

# 21. Ordem oficial definitiva

Esta será nossa sequência de trabalho:

```text
01 🔴 Corrida atual / histórico
          ↓
02 🔴 Detecção de aceite
          ↓
03 🔴 Testes da regra de aceite
          ↓
04 🔴 Expiração da oferta
          ↓
05 🔴 Última corrida aceita
          ↓
06 🔴 OverlayService / selo real
          ↓
07 🔴 Selo → compacta
          ↓
08 🟠 Inicialização / permissões
          ↓
09 🟠 Configurações
          ↓
10 🟠 DataStore
          ↓
11 🟠 Room
          ↓
12 🟠 Histórico persistente
          ↓
13 🟡 Testes completos
          ↓
14 🟡 Testes Uber / 99 / inDrive
          ↓
15 🟡 Documentação
          ↓
16 🟢 APK final / validação
```

## Regra que fica congelada

A partir de agora, **não vamos mais alterar esta decisão sem uma nova aprovação explícita**:

> **O Gestor Driver nunca aceita a corrida pelo usuário. O usuário aceita a corrida diretamente no Uber, 99 ou inDrive. O Gestor Driver identifica o aceite e somente então registra a corrida no histórico.**

Essa separação será a base de toda a implementação daqui até a finalização do projeto.
