"""Testes da camada de apresentacao da corrida."""

import unittest
from datetime import datetime

from core.analysis import AnaliseCorrida
from core.classifier import Classificacao
from core.models import Corrida
from core.plans import ControlePlano, PlanoAcesso
from core.presentation import ModoApresentacao, PresentationModel


class PresentationModelTestCase(unittest.TestCase):
    """Valida a montagem da interface por plano e modo."""

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

    def test_free_mantem_compacto_e_bloqueia_financeiro(self):
        modelo = PresentationModel.criar(
            self.analise,
            PlanoAcesso.FREE,
            controle_plano=self.controle,
        )

        self.assertEqual(modelo.modo, ModoApresentacao.COMPACTA)
        self.assertEqual(modelo.acao_detalhes, "Mais detalhes")
        self.assertEqual(modelo.classificacao_visual.rotulo, "Excelente")
        self.assertEqual(modelo.classificacao_visual.cor, "#2E7D32")

        campos = [campo.chave for campo in modelo.campos_visiveis]
        self.assertEqual(
            campos,
            ["valor_por_km", "valor_total", "km_total", "tempo_estimado", "nota_passageiro"],
        )

        self.assertFalse(modelo.campo_por_chave("valor_por_km").permitido)
        self.assertEqual(modelo.campo_por_chave("valor_por_km").texto_exibicao, "🔒")
        self.assertEqual(modelo.campo_por_chave("valor_total").texto_exibicao, "R$ 40,00")
        self.assertEqual(modelo.campo_por_chave("km_total").texto_exibicao, "10,0 KM")
        self.assertEqual(modelo.campo_por_chave("nota_passageiro").texto_exibicao, "4,80")

    def test_beta_expande_e_libera_financeiro(self):
        modelo = PresentationModel.criar(
            self.analise,
            PlanoAcesso.BETA,
            modo=ModoApresentacao.DETALHES,
            controle_plano=self.controle,
        )

        self.assertTrue(modelo.modo_expandido)
        self.assertEqual(modelo.acao_detalhes, "Menos detalhes")

        campos = [campo.chave for campo in modelo.campos_visiveis]
        self.assertEqual(
            campos,
            [
                "valor_por_km",
                "valor_total",
                "km_total",
                "tempo_estimado",
                "nota_passageiro",
                "km_ate_passageiro",
                "km_viagem",
                "combustivel_estimado",
                "custo_combustivel",
            ],
        )

        self.assertTrue(modelo.campo_por_chave("valor_por_km").permitido)
        self.assertEqual(modelo.campo_por_chave("valor_por_km").texto_exibicao, "4,00")
        self.assertEqual(modelo.campo_por_chave("combustivel_estimado").texto_exibicao, "0,90 L")
        self.assertEqual(modelo.campo_por_chave("custo_combustivel").texto_exibicao, "R$ 5,45")

    def test_pro_mantem_apresentacao_e_recursos_avancados(self):
        modelo = PresentationModel.criar(
            self.analise,
            PlanoAcesso.PRO,
            modo=ModoApresentacao.DETALHES,
            controle_plano=self.controle,
        )

        self.assertTrue(modelo.recursos.recursos_avancados)
        self.assertTrue(modelo.recursos.exibe_relatorios)
        self.assertTrue(modelo.recursos.exibe_estatisticas)
        self.assertEqual(modelo.classificacao_visual.cor, "#2E7D32")
        self.assertEqual(modelo.campo_por_chave("custo_combustivel").texto_exibicao, "R$ 5,45")


if __name__ == "__main__":
    unittest.main()