# Configuração do ambiente de desenvolvimento

Este documento reúne as principais configurações e boas práticas para facilitar a entrada de novos desenvolvedores no projeto.

## 1. Pré-requisitos

- Python 3.10+
- Android Studio
- JDK 17+
- Git
- Gradle Wrapper (incluído no projeto)

## 2. Configuração do projeto Python

O protótipo inicial usa Python para validar a lógica de negócio e a estrutura de cálculos.

### Executar localmente

```bash
python app/main.py
```

### Estrutura recomendada para evolução

- app/: ponto de entrada da aplicação
- core/: regras de negócio e modelos
- tests/: validações e cenários de teste

## 3. Configuração do projeto Android

O projeto Android está localizado em [android-app](android-app).

### Variáveis e ambiente
- Android SDK configurado via Android Studio
- `local.properties` para caminho local do SDK, quando necessário
- Gradle sincronizado automaticamente pelo ambiente do Android Studio

### Build local

```bash
cd android-app
./gradlew assembleDebug
```

## 4. Boas práticas de desenvolvimento

- manter a lógica de negócio separada da interface;
- priorizar legibilidade e nomes claros;
- documentar alterações relevantes;
- criar testes cobrindo regras principais;
- usar commits claros e consistentes.

## 5. Recomendação de ferramentas

- Android Studio
- GitHub
- Git Flow ou branches curtas por feature
- extensão de lint/format para Kotlin e Python

## 6. Padrões de qualidade

- código limpo;
- arquitetura simples e escalável;
- commits bem descritos;
- PRs com contexto claro e validação mínima.
