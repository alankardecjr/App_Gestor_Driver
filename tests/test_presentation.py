"""Testes da apresentacao da corrida e do historico."""

import unittest
from datetime import datetime, timedelta

from core.analysis import AnaliseCorrida
from core.classifier import Classificacao
from core.history import HistoricoCorrida
from core.models import Corrida
from core.plans import ControlePlano, PlanoAcesso
from core.presentation import HistoricoPresentation, ModoApresentacao, PresentationModel


class HistoricoPresentationTestCase(unittest.TestCase):
    """Valida a renderizacao horizontal do historico."""

    def setUp(self):
        agora = datetime(2026, 8, 3, 12, 0)
        self.historico_recente = HistoricoCorrida(
            data_hora=agora,
            plataforma="Uber",
            valor_total=38.0,
            km_ate_passageiro=3.2,
            km_viagem=12.8,
            km_total=16.0,
            tempo_estimado=24,
            nota_passageiro=4.98,
            valor_por_km=2.375,
            combustivel_estimado=1.28,
            custo_combustivel=7.92,
            classificacao=Classificacao.BOA,
        )
        self.historico_antigo = HistoricoCorrida(
            data_hora=agora - timedelta(hours=1),
            plataforma="99",
            valor_total=22.5,
            km_ate_passageiro=1.5,
            km_viagem=7.0,
            km_total=8.5,
            tempo_estimado=18,
            nota_passageiro=None,
            valor_por_km=2.6470588235,
            combustivel_estimado=None,
            custo_combustivel=None,
            classificacao=Classificacao.REGULAR,
        )

    def test_deve_gerar_lista_horizontal_de_historico(self):
        apresentacao = HistoricoPresentation.criar(
            [self.historico_recente, self.historico_antigo]
        )

        self.assertEqual(len(apresentacao.itens), 2)
        self.assertEqual(
            apresentacao.linhas_horizontais[0],
            "2,38 │ R$ 38,00 │ 16 km │ 24 min │ 4,98",
        )
        self.assertEqual(
            apresentacao.linhas_horizontais[1],
            "2,65 │ R$ 22,50 │ 8,5 km │ 18 min │ —",
        )

    def test_deve_expor_classificacao_visual_do_historico(self):
        item = HistoricoPresentation.criar([self.historico_recente]).itens[0]

        self.assertEqual(item.classificacao_visual.rotulo, "Boa")
        self.assertEqual(item.classificacao_visual.cor, "#7CB342")
        self.assertEqual(item.linha_horizontal, "2,38 │ R$ 38,00 │ 16 km │ 24 min │ 4,98")


class PresentationModelContractTestCase(unittest.TestCase):
    """Mantem o contrato da corrida atual para o modo de apresentacao."""

    def test_deve_gerar_apresentacao_compacta_e_expandida(self):
        corrida = Corrida(
            valor_total=40.0,
            km_ate_passageiro=2.0,
            km_viagem=8.0,
            tempo_estimado=18,
        )

        analise = AnaliseCorrida(
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

        compacta = PresentationModel.criar(
            analise,
            PlanoAcesso.FREE,
            controle_plano=ControlePlano(),
        )
        detalhada = PresentationModel.criar(
            analise,
            PlanoAcesso.BETA,
            modo=ModoApresentacao.DETALHES,
            controle_plano=ControlePlano(),
        )

        self.assertEqual(compacta.acao_detalhes, "Mais detalhes")
        self.assertEqual([campo.chave for campo in compacta.campos_visiveis], [
            "valor_por_km",
            "valor_total",
            "km_total",
            "tempo_estimado",
            "nota_passageiro",
        ])
        self.assertEqual(detalhada.acao_detalhes, "Menos detalhes")
        self.assertIn("combustivel_estimado", [campo.chave for campo in detalhada.campos_visiveis])


if __name__ == "__main__":
    unittest.main()