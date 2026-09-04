"""Testes do contrato consolidado de analise de corrida."""

import unittest

from core.analysis import AnaliseCorrida
from core.calculator import CalculadoraCorrida
from core.classifier import Classificacao
from core.models import Corrida
from core.settings import Combustivel, ConfiguracaoUsuario


class AnaliseCorridaContractTestCase(unittest.TestCase):
    """Valida o formato oficial da analise consolidada."""

    def test_deve_retornar_objeto_de_analise_consolidada(self):
        corrida = Corrida(
            valor_total=38.0,
            km_ate_passageiro=3.2,
            km_viagem=12.8,
            tempo_estimado=24,
        )

        configuracao = ConfiguracaoUsuario(
            marca="Toyota",
            modelo="Corolla",
            versao="XEi",
            ano=2021,
            consumo_gasolina=12.5,
            consumo_etanol=9.0,
            preco_gasolina=6.19,
            preco_etanol=4.39,
            combustivel=Combustivel.GASOLINA,
        )

        resultado = CalculadoraCorrida(configuracao_usuario=configuracao).calcular(
            corrida
        )

        self.assertIsInstance(resultado, AnaliseCorrida)
        self.assertEqual(resultado.corrida, corrida)
        self.assertAlmostEqual(resultado.valor_total, 38.0)
        self.assertAlmostEqual(resultado.km_ate_passageiro, 3.2)
        self.assertAlmostEqual(resultado.km_viagem, 12.8)
        self.assertEqual(resultado.tempo_estimado, 24)
        self.assertAlmostEqual(resultado.km_total, 16.0)
        self.assertAlmostEqual(resultado.valor_por_km, 2.375)
        self.assertAlmostEqual(resultado.combustivel_estimado, 1.28)
        self.assertAlmostEqual(resultado.custo_combustivel, 7.9232)
        self.assertEqual(resultado.classificacao, Classificacao.EXCELENTE)
        self.assertEqual(resultado.cor_classificacao, "#2E7D32")
        self.assertIsNotNone(resultado.data_hora)
        self.assertIsNone(resultado.nota_passageiro)
        self.assertIsNone(resultado.plataforma)

    def test_deve_calcular_combustivel_com_etanol(self):
        corrida = Corrida(
            valor_total=32.0,
            km_ate_passageiro=2.0,
            km_viagem=14.0,
            tempo_estimado=20,
        )

        configuracao = ConfiguracaoUsuario(
            marca="Honda",
            modelo="Civic",
            versao="Touring",
            ano=2020,
            consumo_gasolina=11.5,
            consumo_etanol=9.0,
            preco_gasolina=6.19,
            preco_etanol=4.39,
            combustivel=Combustivel.ETANOL,
        )

        resultado = CalculadoraCorrida(configuracao_usuario=configuracao).calcular(
            corrida
        )

        self.assertAlmostEqual(resultado.combustivel_estimado, 1.7777777778)
        self.assertAlmostEqual(resultado.custo_combustivel, 7.8044444444)
        self.assertEqual(resultado.classificacao, Classificacao.EXCELENTE)


if __name__ == "__main__":
    unittest.main()
