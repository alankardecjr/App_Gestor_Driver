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

1. Instale o APK no celular que usa para dirigir.
2. Na **primeira abertura** o app confere permissões (notificações, sobrepor, acessibilidade, bateria). Autorize cada ❎ até virar 🆗 e toque **SEGUIR**.
3. Conecte **conta Google** ou **e-mail** (identidade local). Sem conta o tutorial não abre.
4. Tutorial: **SEGUIR** em cada janela ou **PULAR**. Depois disso o selo sobe e o monitoramento começa.
5. Abra Config: VEÍCULO (consumo), CUSTOS (R$/L + combustível atual), CALIBRAR (−/+ das faixas), APP (título **Configurar aplicativo**, depois **ENVIAR LOG** se a oferta não ler). Localização é opcional.
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
| Config / Histórico | Painel abaixo da expandida, **mesma altura compacta**; o que não couber rola; abas por deslize, setas ou clique |
| Aceita na plataforma | Uma linha no histórico (`R$` / `Km` / `Min`, borda fina da classificação); interface vai ao **selo** |
| Recolher (`⬆️`) | Compacta; toque fora das janelas do Gestor → selo na hora (senão, 5 s) |
| Compacta + toque no mapa / recusar da 99 | Selo imediato; o toque chega na plataforma |
| Fecha e reabre | Configurações iguais |

Se a oferta **não aparecer**, o teste ainda vale: `notificacoes_diagnostico.txt` guarda o texto real.

## Onde está o log

Na aba APP: **ENVIAR LOG**. Também em `/data/data/br.com.gestordriver/files/notificacoes_diagnostico.txt` (adb / Device File Explorer).

## Ordem se algo falhar

1. Overlay some → BATERIA + SOBREPOR + notificação permanente.
2. Oferta não lê → ACESSIB. ligada + **ENVIAR LOG** (package + título + texto).
3. Aceite não grava histórico → texto da notificação **depois** de aceitar (também no log).
4. Custo estranho → conferir combustível atual, km/L e preços na aba CUSTOS.
5. Rota / Maps → nesta versão só abre o app escolhido; endereço depende do texto da notificação.
