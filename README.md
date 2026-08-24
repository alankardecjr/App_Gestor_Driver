# ⭐ Gestor Driver

> **Um assistente inteligente para motoristas de aplicativos, desenvolvido para analisar ofertas de corrida em tempo real e ajudar na tomada de decisão em poucos segundos.**

---

## 🚗 Sobre o Projeto

O **Gestor Driver** é um aplicativo Android criado para auxiliar motoristas de aplicativos de transporte, oferecendo uma análise rápida da rentabilidade de cada corrida.

Inicialmente, o projeto será compatível com:

- Uber
- 99
- inDrive

A arquitetura foi planejada para permitir novas plataformas futuramente.

O objetivo é simples:

> **Ajudar o motorista a decidir rapidamente se uma corrida vale a pena.**

O Gestor Driver **não aceita corridas**. O motorista aceita ou recusa no Uber, 99 ou inDrive. Este app identifica a oferta, mostra os dados, monitora a plataforma e só grava no histórico quando o aceite é detectado.

O aplicativo foi projetado para funcionar com uma interface horizontal, compacta e de baixa obstrução, permitindo que o motorista mantenha a visão do aplicativo de transporte.

---

## 🎯 Objetivo do MVP

O núcleo do MVP é analisar uma oferta de corrida e apresentar informações relevantes para a tomada de decisão.

O principal indicador financeiro é:

```text
🛞 R$/KM
```

O símbolo de pneu representa movimento e substitui a estrela anteriormente utilizada para representar o valor por quilômetro.

A análise considera:

- valor total da corrida;
- distância até o passageiro;
- distância da viagem;
- distância total;
- tempo estimado;
- nota do passageiro, quando disponível;
- consumo estimado;
- custo estimado de combustível;
- classificação da corrida.

---

## 🖥 Interface

A interface principal foi projetada para ocupar pouco espaço na tela do celular e permitir que o motorista continue visualizando o aplicativo de transporte.

### Interface Compacta

```text
╭──────────────────────────────────────────────────────────────────────╮
│ 🛞 R$/KM 2,38 │ 💰 R$38,00 │ 📍16 km │ ⏱24 min │ ⭐4,98 │ ⓘ │
╰──────────────────────────────────────────────────────────────────────╯
```

### Ordem das informações

1. 🛞 R$/KM
2. 💰 Valor total
3. 📍 KM total
4. ⏱ Tempo estimado
5. ⭐ Nota do passageiro
6. ⓘ Mais detalhes / Menos detalhes

O **R$/KM** possui maior destaque visual.

---

## 🔎 Interface Expandida

Ao selecionar **Mais detalhes**, a interface apresenta informações adicionais.

```text
╭────────────────────────────────────────────────────────────────────────────╮
│ 🛞 R$/KM 2,38 │ 💰 R$38,00 │ 📍16 km │ ⏱24 min │ ⭐4,98 │
├────────────────────────────────────────────────────────────────────────────┤
│ 📍 Passageiro        → 1,6 km                                              │
│ 🏁 Destino           → 12,7 km                                             │
│ ⛽ Combustível       → 1,28 L                                              │
│ 💸 Gasto estimado    → R$ 8,19                                             │
├────────────────────────────────────────────────────────────────────────────┤
│ ⚙ Configurações │ ✕ Sair interface │ ⏻ Fechar app │ 🕘 Histórico           │
╰────────────────────────────────────────────────────────────────────────────╯
```

O botão de informação é dinâmico:

```text
Interface compacta
        ↓
ⓘ Mais detalhes
        ↓
Interface expandida
        ↓
ⓘ Menos detalhes
        ↓
Interface compacta
```

---

## 🎨 Classificação Visual

A classificação da corrida é representada principalmente pela **cor da borda da interface**, evitando ocupar espaço com texto adicional.

```text
🟢 Excelente
🟢 Boa
🟡 Regular
🟠 Baixa
🔴 Ruim
```

A classificação é centralizada no motor oficial do projeto. Limites e cores não devem ser duplicados em diferentes módulos.

---

## ◉ Selo Flutuante

Quando não houver uma corrida disponível, o Gestor Driver poderá permanecer minimizado em um pequeno selo:

```text
        ◉
```

O aplicativo continua preparado para monitorar novas ofertas.

```text
◉
│
Nova corrida
│
▼
Interface Compacta
│
Mais detalhes
▼
Interface Expandida
│
Menos detalhes
▼
Interface Compacta
```

---

## 🔔 Ausência de Notificações

A ausência de uma notificação **não é considerada erro**.

O motorista pode estar:

- sem login na Uber;
- sem login na 99;
- sem login no inDrive;
- com os aplicativos fechados;
- sem oferta disponível;
- fora do período de trabalho;
- utilizando o Gestor Driver apenas para consultar configurações ou histórico.

Nesse cenário, o aplicativo permanece em estado de monitoramento/espera.

```text
Gestor Driver
      ↓
Monitoramento ativo
      ↓
Aguardando oferta...
```

O aplicativo não deve criar uma corrida falsa nem apresentar erro simplesmente porque nenhuma notificação foi recebida.

---

## 🔔 Notificação Não Reconhecida

Quando uma notificação for recebida, mas não puder ser interpretada corretamente:

```text
Notificação
     ↓
Não reconhecida
     ↓
Não criar corrida falsa
```

O evento poderá futuramente ser registrado para diagnóstico e evolução dos parsers.

---

## 🕘 Histórico

O histórico estará disponível nas três versões do aplicativo.

As corridas serão apresentadas utilizando o mesmo padrão visual horizontal das notificações.

```text
╭────────────────────────────────────────────────────────────╮
│ 🛞 2,38 │ 💰 R$38 │ 📍16 km │ ⏱24 min │ ⭐4,98 │
╰────────────────────────────────────────────────────────────╯
```

O histórico deverá armazenar o resultado da análise no momento em que a corrida foi processada.

Uma corrida antiga não deverá ser recalculada automaticamente caso o usuário altere posteriormente:

- preço do combustível;
- consumo;
- critérios de classificação;
- configurações do veículo.

---

## ⚙ Configurações

A tela de configuração é uma exceção à interface horizontal compacta. Ela poderá utilizar uma tela convencional, permitindo melhor organização dos campos.

### Veículo

- Marca
- Modelo
- Versão
- Ano

### Consumo

- Gasolina (km/L)
- Etanol (km/L)

### Combustível em uso

- Gasolina
- Etanol

### Preços

- Valor da gasolina
- Valor do etanol

Essas informações permitem calcular:

- combustível estimado;
- custo estimado de combustível.

A estrutura também será preparada para futuros custos operacionais.

---

## 🧮 Motor de Análise

O núcleo utiliza um contrato consolidado de análise da corrida.

```text
Dados da corrida
       ↓
    Corrida
       ↓
   Calculator
       ↓
 AnaliseCorrida
       ↓
┌──────┴───────────────────────┐
│                              │
R$/KM                    Combustível
│                              │
Classificação            Custo estimado
└──────────────┬───────────────┘
               ↓
           Interface
```

O motor já contempla:

- classificação oficial;
- R$/KM;
- combustível estimado;
- custo de combustível;
- configuração de usuário;
- gasolina;
- etanol.

---

## 💰 Modelo de Versões

O Gestor Driver será desenvolvido em três versões:

```text
🆓 FREE
🧪 BETA
⭐ PRO
```

### 🆓 Free

A versão Free terá os mesmos recursos básicos do MVP, porém alguns valores financeiros serão ocultados.

**Visíveis:**

- 💰 Valor total da corrida;
- 📍 KM total;
- ⏱ Tempo;
- ⭐ Nota do passageiro;
- classificação visual pela cor da borda;
- histórico básico.

**Ocultos:**

- 🛞 R$/KM;
- ⛽ Combustível estimado;
- 💸 Gasto estimado.

Exemplo:

```text
╭────────────────────────────────────────────────────────────╮
│ 🛞 🔒 │ 💰 R$38,00 │ 📍16 km │ ⏱24 min │ ⭐4,98 │
╰────────────────────────────────────────────────────────────╯
```

A Free deve demonstrar claramente que o aplicativo está analisando a corrida.

### 🧪 Beta

A Beta terá os mesmos recursos básicos da Free, porém exibirá os valores financeiros da análise.

```text
╭────────────────────────────────────────────────────────────╮
│ 🛞 R$/KM 2,38 │ 💰 R$38 │ 📍16 km │ ⏱24 min │ ⭐4,98 │
╰────────────────────────────────────────────────────────────╯
```

Nos detalhes:

```text
📍 Passageiro       1,6 km
🏁 Destino          12,7 km
⛽ Combustível      1,28 L
💸 Gasto estimado   R$ 8,19
```

O histórico também exibirá esses valores.

### ⭐ Pro

A versão Pro será a evolução do aplicativo para análise de custo operacional completo.

Funcionalidades previstas:

- combustível;
- pneus;
- óleo;
- manutenção;
- depreciação;
- outros custos operacionais;
- custo operacional total;
- valor líquido da corrida;
- R$/KM líquido;
- histórico avançado;
- estatísticas;
- relatórios;
- metas de rentabilidade;
- comparação entre plataformas;
- múltiplos veículos.

---

## 🧱 Arquitetura

```text
GestorDriver/
│
├── app/                    # demos Python
├── core/                   # núcleo Python (referência)
├── notifications/          # parsers Python (referência)
├── android-app/            # app Android (Kotlin)
│   └── app/src/main/java/br/com/gestordriver/
│       ├── core/           # calculator, classifier, fuel (portado)
│       ├── notification/   # parser, extractor, listener, bus
│       ├── presentation/   # PresentationBuilder
│       └── ui/             # Compose + ViewModel
├── tests/
└── docs/
```

A organização poderá evoluir conforme a implementação Android avance.

---

## 🔄 Máquina de Estados

```text
                ┌──────────────────┐
                │      IDLE        │
                └────────┬─────────┘
                         ↓
                ┌──────────────────┐
                │   MONITORANDO    │
                └────────┬─────────┘
                         │
                    notificação
                         ↓
                ┌──────────────────┐
                │ CORRIDA_COMPACTA │
                └────────┬─────────┘
                         │
                    Mais detalhes
                         ↓
                ┌──────────────────┐
                │ CORRIDA_DETALHES │
                └────────┬─────────┘
                         │
                    Menos detalhes
                         ↓
                 CORRIDA_COMPACTA
```

O histórico e as configurações possuem seus próprios fluxos.

---

## 🛠 Tecnologias Planejadas

- Kotlin
- Android Studio
- Jetpack Compose
- MVVM
- Room Database
- NotificationListenerService
- Foreground Service
- Overlay / SYSTEM_ALERT_WINDOW

O núcleo de regras é desenvolvido e validado de forma independente antes da integração completa com Android.

---

## 🧪 Validação Atual

O núcleo possui testes automatizados cobrindo:

- classificação;
- contrato de análise;
- gasolina;
- etanol;
- combustível;
- custo estimado.

**Estado atual:**

```text
25 testes Python + testes unitários Android
```

Para executar os testes Python:

```bash
pip install -r requirements.txt
python -m pytest tests/ -v
```

O núcleo de regras também foi portado para Kotlin em `android-app/.../core/`, espelhando calculator, classifier e fuel do Python.

---

## 🚀 Roadmap Atual

### Núcleo

- [x] Models
- [x] Calculator
- [x] Validator
- [x] Classificação oficial
- [x] Analysis / AnaliseCorrida
- [x] Testes automatizados

### Custos

- [x] Combustível
- [x] Gasolina
- [x] Etanol
- [x] Consumo estimado
- [x] Custo estimado
- [ ] Custo operacional completo

### Comportamento

- [x] Máquina de estados definida
- [x] Ausência de notificações
- [x] Mais detalhes / Menos detalhes
- [x] Sair interface
- [x] Fechar app
- [x] Notificação não reconhecida

### Histórico

- [x] Definir modelo HistoricoCorrida
- [x] Definir dados persistidos
- [x] Criar armazenamento (Python)
- [x] Salvar análises
- [x] Recuperar últimas corridas
- [x] Testar histórico
- [x] Integrar regras Free/Beta/Pro (Python)
- [ ] Persistência Room no Android

### Android

- [x] Projeto Android
- [x] MVVM
- [x] Jetpack Compose
- [ ] Room
- [x] Interface compacta
- [x] Interface expandida
- [ ] Configurações
- [ ] Overlay real (SYSTEM_ALERT_WINDOW)
- [x] Selo flutuante (modo ocultar + arrastar)

### Notificações

- [x] NotificationListenerService
- [x] Identificação da plataforma
- [x] Parser Uber
- [x] Parser 99
- [x] Parser inDrive
- [x] Tratamento de notificações desconhecidas
- [x] Integração Service → ViewModel (SharedFlow)

### Planos

- [x] Definir Free
- [x] Definir Beta
- [x] Definir Pro
- [x] Definir valores ocultos na Free
- [x] Definir histórico disponível na Free
- [x] Implementar controle de recursos por plano

### Testes

- [ ] Sem login na Uber (instrumentado)
- [x] Sem notificações (unitário)
- [x] Nova corrida (pipeline)
- [x] Expandir detalhes
- [x] Retrair detalhes
- [x] Ocultar interface (selo flutuante)
- [x] Fechar app (confirmação)
- [x] Histórico (UI demo)
- [ ] Nova corrida após ocultar interface
- [x] Notificação desconhecida
- [x] Regras Free
- [x] Regras Beta
- [x] Regras Pro

---

## ⭐ Diferenciais

- Interface horizontal compacta;
- baixa obstrução da tela;
- pensada para motoristas de carros e motocicletas;
- decisão rápida;
- destaque visual para 🛞 R$/KM;
- classificação pela cor da borda;
- análise de combustível;
- histórico;
- funcionamento independente da existência de uma oferta;
- arquitetura preparada para evolução;
- três níveis de produto: Free, Beta e Pro.

---

## 📚 Documentação

A documentação técnica está organizada na pasta `docs/`.

```text
docs/
├── Gestor_Driver_MVP_Especificacao_v1.0.md
├── Roadmap.md
├── EXECUTION_BOARD.md
├── CLASSIFICACAO_ETAPAS.md
├── ARQUITETURA.md
└── UI_GUIDE.md
```

---

## 📌 Status Atual

**Projeto:** Gestor Driver  
**Versão:** MVP v1.0  
**Status:** 🚧 Em desenvolvimento  
**Núcleo:** ✅ Validado  
**Classificação:** ✅ Concluída  
**Análise:** ✅ Concluída  
**Combustível:** ✅ Concluído  
**Estado e comportamento:** ✅ Formalizado  
**Planos Free/Beta/Pro:** ✅ Implementados (Python + Android)  
**Android:** 🚧 UI, núcleo portado, notificações integradas ao ViewModel  
**Próxima etapa:** 🔔 Overlay real + persistência Room + testes instrumentados  

---

> **Gestor Driver — Informação rápida para uma decisão melhor.**