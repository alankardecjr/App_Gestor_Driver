# Roteiro Beta — Gestor Driver

Objetivo: o app funcionar no celular de trabalho. Parser e aceite se calibram com o que o teste real mostrar.

**Versão alvo: Beta.** Cálculos visíveis (R$/KM, litros, gasto e lucro de combustível). Sem Pro. Sem Play Store / Free.

**Interfaces congeladas até o Pro.** Não pedir nem aplicar novos ajustes de layout/tamanho nas telas. O restante da Beta é calibração (oferta/aceite) e correção de erro de funcionamento.

## O que entra agora

1. Detectar oferta e **aceite** (Uber / 99 / inDrive).
2. Gravar no **histórico só corrida aceita**.
3. Overlay: compacta → expandida; Config e Histórico **embaixo** da expandida.
4. Custo pelo **combustível atual** + preços da aba **CUSTOS**.
5. Log local das notificações para ajustar o parser.

## O que fica para depois

Pro (custo operacional, R$ líquido, relatórios). Free na loja (mesma UI, **sem** mostrar os cálculos). Testes instrumentados.

## Como testar (você)

1. Instale o APK debug no celular que usa para dirigir.
2. Abra o Gestor Driver e conceda **Notificações**, **Sobressair** e **Localização**.
3. No Android 13+, aceite a permissão de notificação do próprio Gestor.
4. Desative otimização de bateria para o Gestor Driver (senão o overlay some).
5. Aba VEÍCULO: consumo km/L e combustível atual. Aba CUSTOS: preços R$/L. Aba APP: permissões, Maps/Waze e faixas.
6. Feche e reabra — os valores precisam continuar iguais.
7. Abra Uber Driver, 99 ou inDrive **logado**.
8. Deixe o Gestor em segundo plano (selo visível sobre o mapa).
9. Espere uma oferta real.

### Sucesso mínimo desta sessão

| Passo | Esperado |
| --- | --- |
| Oferta chega | Compacta: R$/KM, VALOR, DIST., TEMPO, NOTA, borda da classificação |
| Recusa / some a oferta | Volta ao selo, **não** entra no histórico |
| `⬇️` ou toque no selo | Expandida com Distâncias + Custos; mapa visível atrás |
| Config / Histórico | Painel abaixo da expandida (não tela cheia) |
| Aceita na plataforma | Uma linha no histórico; interface vai ao **selo** |
| Recolher (`⬆️`) | Compacta; toque fora das janelas do Gestor → selo na hora (senão, 5 s) |
| Compacta + toque no mapa / recusar da 99 | Selo imediato; o toque chega na plataforma |
| Fecha e reabre | Configurações iguais |

Se a oferta **não aparecer**, o teste ainda vale: `notificacoes_diagnostico.txt` guarda o texto real.

## Onde está o log

`/data/data/br.com.gestordriver/files/notificacoes_diagnostico.txt`

No Android Studio: Device File Explorer, ou `adb pull` desse caminho. Use só para calibrar.

## Ordem se algo falhar

1. Overlay some → bateria / overlay / notificação permanente.
2. Oferta não lê → trecho do log (package + título + texto).
3. Aceite não grava histórico → texto da notificação **depois** de aceitar.
4. Custo estranho → conferir combustível atual, km/L e preços na aba APP.
5. Rota / Maps → nesta versão só abre o app escolhido; endereço depende do texto da notificação.
