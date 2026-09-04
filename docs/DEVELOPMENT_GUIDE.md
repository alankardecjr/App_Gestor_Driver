# Guia de desenvolvimento

## Ambiente

- Android Studio (abrir o diretório `android-app`)
- JDK 17+
- Python 3.10+ (opcional: núcleo e `tests/`)
- Git
- Aparelho físico Android 11+ recomendado para overlay e notificações

## Fluxo

1. Branch por tarefa.
2. Mudar o mínimo necessário; regras de domínio em `core/` (Kotlin) com teste.
3. Validar unitários; no celular seguir [ROTEIRO_PRO.md](ROTEIRO_PRO.md) (linha ativa) ou [ROTEIRO_BETA.md](ROTEIRO_BETA.md) se for manutenção do freeze em `main`. Não misturar `vs-2.0` em `main`.
4. PR com o *porquê* da mudança.

## Convenções

- O app não aceita corrida; aceite só via detecção da plataforma.
- Não gravar oferta no Room.
- Parser: preferir log real a chute de regex.
