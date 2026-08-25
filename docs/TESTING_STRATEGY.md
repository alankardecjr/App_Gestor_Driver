# Estratégia de testes

## Objetivo

Proteger o cálculo (R$/KM, classificação, combustível) e as regras de produto (oferta ≠ histórico, aceite, planos).

## O que existe hoje

### Python (`tests/`)

25 casos: classificação, contrato `AnaliseCorrida`, pipeline de notificações, histórico, Free/Beta/Pro, apresentação.

```bash
python -m pytest tests/ -v
```

### Kotlin (`android-app` unit tests)

Espelho do domínio, parser, ViewModel (ocultar, fechar, oferta vs aceite), persistência de configuração, extração de endereço, escolha de destino Maps/Waze.

```bash
# na pasta android-app, via Android Studio ou:
./gradlew :app:testDebugUnitTest
```

No Windows: `gradlew.bat :app:testDebugUnitTest`.

### Ainda não

- Testes instrumentados com NotificationListener e Uber/99 reais.
- Testes de UI Compose no dispositivo.

## Prioridade

1. Regras de cálculo e aceite (unitário) — feito e deve permanecer verde.
2. Fixtures com textos reais anonimizados — depois do piloto (ver `ROTEIRO_BETA.md`).
3. Instrumentados — quando o parser estiver calibrado.
