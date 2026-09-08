# Roadmap

Estado alinhado ao código em `android-app/` e às regras em [`REGRAS_NEGOCIO.md`](REGRAS_NEGOCIO.md).

**Produto:** **Free** (demo, 🔒) + **Pro** (paga, liberada). Branch ativa: `vs-2.0` / `2.0.0`. Beta `1.1.10` congelado em `main`.

## Feito — Beta 1.1.10 (`main`)

- [x] Overlay: selo, compacta, expandida
- [x] Histórico e configuração como painéis overlay
- [x] Custo de combustível atual (km/L + preço do litro)
- [x] Faixas de classificação editáveis (4 faixas na Beta)
- [x] Histórico só no aceite (Room) + DataStore de config
- [x] Listener, parser genérico, log `notificacoes_diagnostico.txt`
- [x] Onboarding: permissões → conta → tutorial → monitoramento

## Agora — Pro 2.0 (`vs-2.0`)

Código (telas A/B): ver [`ROTEIRO_PRO.md`](ROTEIRO_PRO.md).

- [x] Lucro = valor − combustível + óleo + pneus + IPVA + seguro
- [x] Semáforo 3 faixas (padrão 1,59 / 1,60–1,99 / 2,00)
- [x] Dashboard Diário / Semanal / Mensal (Compose + overlay)
- [x] Tema Escuro / Claro / Celular
- [x] Card histórico: Consumo (L) + Gasto; Embarque / Destino
- [x] Alerta óleo 500 km; confirmação de abastecimento ao Salvar
- [x] **Tela Atalhos congelada** (§44, 05/09/2026): Histórico | Carteira | Despesas | Semáforo | Usuário | Configurar | Fechar (sem X; selo abre/fecha; posição dir/esq/acima/abaixo)
- [x] Telas nativas (Compose): Histórico, Semáforo, Carteira/Dashboard, Config (Despesas/Veículo/App)
- [x] Compacta só na oferta; some na hora no aceite/expirar/recusar
- [x] **Compacta Pro:** R$/Km · Dist. · Tempo · Nota + ícone · Parada(s); borda 6 dp; ~4,5×1,7 cm; arrastável; toque não faz nada
- [x] Telas nativas sem rodapé; Opções + ← → Opções; X → selo; Cancelar/Salvar nas abas editáveis; título Carteira
- [x] Esquema Pro congelado (selo · atalhos · compacta · notificação · menu · **confirmação**) — 05/09/2026
- [x] Overlay sobre outros apps: **só** selo · atalhos · compacta (§44)
- [ ] Bloco C — teste de rua (SM-A145M) — **roteiro 06/09/2026** em [`ROTEIRO_PRO.md`](ROTEIRO_PRO.md)
- [ ] Freeze Pro após rua; commit só com pedido

## Depois

- [ ] Cobrança / Play Store (Free na loja = mesmo app com 🔒)
- [ ] Calibração fina de parser / aceite com ofertas reais
- [ ] Testes instrumentados; gráficos / metas além do dashboard atual

## Fora do escopo imediato

Misturar 2.0 em `main`; botão Aceitar; sync em nuvem.
