# Estrutura Android proposta

A estrutura abaixo representa uma organização limpa e profissional para o aplicativo Android, com foco em separação de responsabilidades e evolução do projeto.

```text
android-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/br/com/gestordriver/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ui/
│   │   │   │   ├── navigation/
│   │   │   │   ├── viewmodel/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   └── utils/
│   │   │   └── res/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Camadas sugeridas

### UI
Responsável pela experiência visual, composição e interação com o usuário.

### ViewModel
Controla o estado da tela e o fluxo de dados entre UI e regras de negócio.

### Domain / Core
Contém as regras principais da aplicação, como cálculo de corrida e avaliação de rentabilidade.

### Data / Repository
Concentrará armazenamento e consumo de dados persistidos no futuro.

### Service
Responsável por integrações futuras com notificações, overlay e mecanismos de contexto.

## Princípios de organização

- separação entre interface e lógica;
- componentes reutilizáveis;
- estrutura preparada para crescimento;
- código mais fácil de testar e manter.
