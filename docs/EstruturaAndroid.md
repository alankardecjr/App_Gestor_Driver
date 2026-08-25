# Estrutura Android (código atual)

Pacote `br.com.gestordriver` em `android-app/app/src/main/java/`.

```text
android-app/
├── app/
│   └── src/main/java/br/com/gestordriver/
│       ├── MainActivity.kt
│       ├── GestorDriverApp.kt
│       ├── core/              # cálculo e classificação
│       ├── data/              # Room + DataStore
│       ├── model/
│       ├── navigation/        # Maps / Waze
│       ├── notification/      # listener, parser, log
│       ├── overlay/
│       ├── permission/
│       ├── presentation/
│       └── ui/                # Compose + ViewModels
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

Testes unitários: `app/src/test/java/...` (parser, ViewModel, config, navegação).
