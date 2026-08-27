# Roteiro Beta — Gestor Driver

Objetivo: o app começar a funcionar no seu celular. Depois calibramos parser e aceite com o que o teste real mostrar.

Versão alvo: **Beta** (R$/KM, combustível, gasto e lucro visíveis). Sem Pro, sem Play Store. Gasto e lucro usam **somente combustível**.

## O que entra agora

1. Detectar oferta e **aceite** (Uber / 99 / inDrive).
2. Gravar no **histórico só corrida aceita**.
3. Abrir o **roteiro** no Maps ou Waze (o app escolhido em Configurações).
4. **Configurações salvas** (veículo, preços, faixas, navegação).
5. Log local das notificações para ajustar o parser sem adivinhar.

## O que fica para depois

Custo operacional Pro, estatísticas, múltiplos veículos, testes instrumentados, RC1.

## Como testar (você)

1. Instale o APK debug no celular que usa para dirigir.
2. Abra o Gestor Driver → Configurações.
3. Conceda **Notificações** e **Overlay**.
4. No Android 13+, aceite a permissão de notificação do próprio Gestor.
5. Desative otimização de bateria para o Gestor Driver (Senão o overlay some).
6. Preencha consumo/preço e escolha Maps ou Waze. Volte — os valores precisam continuar iguais depois de fechar o app.
7. Abra Uber Driver, 99 ou inDrive **logado**.
8. Deixe o Gestor em segundo plano (selo/overlay visível sobre Uber/99).
9. Espere uma oferta real.

### Sucesso mínimo desta sessão

| Passo | Esperado |
| --- | --- |
| Oferta chega | Barra compacta no topo (R$/KM, valor, km, min, nota) |
| Recusa / some a oferta | Volta ao selo na mesma posição, **não** entra no histórico |
| Toque no selo | Expandida overlay ~1/3 da tela; o mapa da plataforma continua visível |
| Aceita no app da plataforma | Histórico ganha **uma** linha |
| Expandida | Distâncias + custos (combustível) + lucro; botões Config / Ocultar / Fechar / Histórico |
| Fecha e reabre o Gestor | Configurações iguais às que você salvou |

Se a oferta **não aparecer**, o teste ainda vale: o arquivo `notificacoes_diagnostico.txt` (armazenamento interno do app) guarda o texto real. Com isso ajustamos o parser na próxima rodada.

## Onde está o log

`/data/data/br.com.gestordriver/files/notificacoes_diagnostico.txt`

No Android Studio: Device File Explorer, ou `adb pull` desse caminho. Não precisa enviar dado de passageiro para ninguém — use só para calibrar.

## Ordem se algo falhar

1. Overlay some → bateria / overlay / notificação permanente.
2. Oferta não lê → mandar o trecho do log (package + título + texto).
3. Aceite não grava histórico → mandar o texto da notificação **depois** de aceitar.
4. Rota não abre → a notificação não veio com origem/destino; o botão avisa. Aí extraímos o formato no próximo ajuste.
