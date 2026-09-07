# Estratégia de testes

## Objetivo

Proteger o cálculo (R$/KM, classificação, combustível) e as regras de produto (oferta ≠ histórico, aceite, planos).

## O que existe hoje

### Python (`tests/`)

25 casos: classificação, contrato `AnaliseCorrida`, pipeline de notificações, histórico, Free/Pro (enum legado `BETA` = Pro), apresentação.

```bash
python -m pytest tests/ -v
```

### Kotlin (`android-app` unit tests)

Espelho do domínio, parser, ViewModel (ocultar, fechar, oferta vs aceite, Semáforo como tela nativa do menu, abas Config 0–2), persistência de configuração, extração de endereço, escolha de destino Maps/Waze.

```bash
# na pasta android-app, via Android Studio ou:
./gradlew :app:testDebugUnitTest
```

No Windows: `gradlew.bat :app:testDebugUnitTest`.

Casos relevantes de UI/estado: `AppViewModelTest` (Semáforo, Recentes, selo no X, confirmação Fechar, selo↔Atalhos). **Tela Atalhos congelada** §44 (05/09/2026): Histórico | Carteira | Despesas | Semáforo | Usuário | Configurar | Fechar. Posição: `AtalhosPosicaoTest` (dir/esq/acima/abaixo). **Compacta:** R$/Km · Dist. · Tempo · Nota; borda 6 dp; arrastável; toque não faz nada. Debug no aparelho: `adb logcat -s GestorAtalhos:D`.

### Ainda não

- Testes instrumentados com NotificationListener e Uber/99 reais.
- Testes de UI Compose no dispositivo.

## Prioridade

1. Regras de cálculo e aceite (unitário) — feito e deve permanecer verde.
2. Fixtures com textos reais anonimizados — depois do piloto (ver `ROTEIRO_BETA.md`).
3. Instrumentados — quando o parser estiver calibrado.
4. Pro e Free de loja — fora desta fase de testes.
