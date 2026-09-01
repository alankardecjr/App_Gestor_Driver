# Roadmap

Estado alinhado ao código em `android-app/` e às regras em [`REGRAS_NEGOCIO.md`](REGRAS_NEGOCIO.md).

**Ordem de produto:** Beta (agora) → Pro (depois) → Free no lançamento da loja (cálculos ocultos).

## Agora — Beta

- [x] Overlay: selo, compacta (cabeçalho), expandida (altura do conteúdo)
- [x] Histórico e configuração como painéis overlay (exclusivos)
- [x] Custo só de combustível atual (km/L + preço do litro)
- [x] Faixas de classificação editáveis (padrão 1,19 / 1,20–1,59 / 1,60–1,99 / 2,00)
- [x] Histórico só no aceite (Room) + DataStore de config
- [x] Listener, parser genérico, log `notificacoes_diagnostico.txt`
- [x] Onboarding: permissões → conta → tutorial (seguir/pular) → monitoramento
- [ ] Calibração de campo (parser / aceite com notificações reais)

## Depois — Pro

- [ ] Custo operacional (pneus, óleo, manutenção, depreciação)
- [ ] Calcular combustível (valor/litros → R$/L; km inicial/final → km/L do combustível atual)
- [ ] Vencimento do IPVA
- [ ] R$/km líquido
- [ ] Relatórios / estatísticas
- [ ] Trajeto da corrida aceita no histórico (Maps/Waze)

## Lançamento — Free

- [ ] Mesma app na loja, plano Free: valor, km, tempo, nota e cor **sem** R$/KM, litros, gasto e lucro
- [ ] Publicação (Play Store)

## Fora do escopo imediato

Testes instrumentados com notificações reais, múltiplos veículos, seletor de plano na UI (a build inicia em Beta).
