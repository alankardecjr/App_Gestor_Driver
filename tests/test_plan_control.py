"""Testes do controle de planos Free, Beta e Pro."""

import unittest
from datetime import datetime

from core.analysis import AnaliseCorrida
from core.classifier import Classificacao
from core.models import Corrida
from core.plans import ControlePlano, PlanoAcesso, VisaoPlano


class ControlePlanoTestCase(unittest.TestCase):
    """Valida a exposicao de recursos por plano."""

    def setUp(self):
        corrida = Corrida(
            valor_total=40.0,
            km_ate_passageiro=2.0,
            km_viagem=8.0,
            tempo_estimado=18,
        )

        self.analise = AnaliseCorrida(
            corrida=corrida,
            valor_total=40.0,
            km_ate_passageiro=2.0,
            km_viagem=8.0,
            tempo_estimado=18,
            nota_passageiro=4.8,
            plataforma="Uber",
            data_hora=datetime(2026, 8, 1, 12, 45),
            km_total=10.0,
            valor_por_km=4.0,
            combustivel_estimado=0.9,
            custo_combustivel=5.45,
            classificacao=Classificacao.EXCELENTE,
            cor_classificacao="#2E7D32",
        )
        self.controle = ControlePlano()

    def test_free_mantem_basico_e_oculta_financeiro(self):
        visao = self.controle.aplicar(self.analise, PlanoAcesso.FREE)

        self.assertIsInstance(visao, VisaoPlano)
        self.assertIs(visao.analise, self.analise)
        self.assertTrue(visao.recursos.exibe_valor_total)
        self.assertTrue(visao.recursos.exibe_km_total)
        self.assertTrue(visao.recursos.exibe_tempo)
        self.assertTrue(visao.recursos.exibe_nota)
        self.assertTrue(visao.recursos.exibe_classificacao_visual)
        self.assertTrue(visao.recursos.historico_disponivel)
        self.assertFalse(visao.recursos.exibe_valor_por_km)
        self.assertFalse(visao.recursos.exibe_combustivel_estimado)
        self.assertFalse(visao.recursos.exibe_custo_combustivel)
        self.assertFalse(visao.recursos.historico_financeiro)
        self.assertFalse(visao.recursos.recursos_avancados)

    def test_beta_exibe_financeiro_e_hist_orico_financeiro(self):
        visao = self.controle.aplicar(self.analise, PlanoAcesso.BETA)

        self.assertTrue(visao.recursos.exibe_valor_total)
        self.assertTrue(visao.recursos.exibe_km_total)
        self.assertTrue(visao.recursos.exibe_tempo)
        self.assertTrue(visao.recursos.exibe_nota)
        self.assertTrue(visao.recursos.exibe_classificacao_visual)
        self.assertTrue(visao.recursos.historico_disponivel)
        self.assertTrue(visao.recursos.exibe_valor_por_km)
        self.assertTrue(visao.recursos.exibe_combustivel_estimado)
        self.assertTrue(visao.recursos.exibe_custo_combustivel)
        self.assertTrue(visao.recursos.historico_financeiro)
        self.assertFalse(visao.recursos.recursos_avancados)

    def test_pro_exibe_recursos_avancados(self):
        visao = self.controle.aplicar(self.analise, PlanoAcesso.PRO)

        self.assertTrue(visao.recursos.exibe_valor_por_km)
        self.assertTrue(visao.recursos.exibe_combustivel_estimado)
        self.assertTrue(visao.recursos.exibe_custo_combustivel)
        self.assertTrue(visao.recursos.historico_financeiro)
        self.assertTrue(visao.recursos.exibe_custos_operacionais_completos)
        self.assertTrue(visao.recursos.exibe_pneus)
        self.assertTrue(visao.recursos.exibe_oleo)
        self.assertTrue(visao.recursos.exibe_manutencao)
        self.assertTrue(visao.recursos.exibe_depreciacao)
        self.assertTrue(visao.recursos.exibe_r_km_liquido)
        self.assertTrue(visao.recursos.exibe_relatorios)
        self.assertTrue(visao.recursos.exibe_estatisticas)
        self.assertTrue(visao.recursos.recursos_avancados)


if __name__ == "__main__":
    unittest.main()
