"""Testes do motor de classificacao e integracao com calculadora."""

import unittest

from core.analysis import AnaliseCorrida
from core.calculator import CalculadoraCorrida
from core.classifier import Classificacao, MotorClassificacao
from core.models import Corrida


class MotorClassificacaoTestCase(unittest.TestCase):
    """Valida limites, enum oficial e mapeamento de cores."""

    def setUp(self):
        self.motor = MotorClassificacao()

    def test_deve_classificar_com_baixa(self):
        self.assertEqual(
            self.motor.classificar_por_valor_km(1.30),
            Classificacao.REGULAR,
        )

    def test_deve_classificar_com_ruim(self):
        self.assertEqual(
            self.motor.classificar_por_valor_km(1.10),
            Classificacao.RUIM,
        )

    def test_deve_respeitar_limites_customizados(self):
        motor = MotorClassificacao(
            limites_r_por_km={
                "EXCELENTE": 3.00,
                "BOA": 2.50,
                "REGULAR": 2.00,
                "BAIXA": 1.60,
            }
        )

        self.assertEqual(motor.classificar_por_valor_km(2.10), Classificacao.REGULAR)

    def test_deve_retornar_cor_por_classificacao(self):
        self.assertEqual(self.motor.cor_de(Classificacao.EXCELENTE), "#1E88E5")
        self.assertEqual(self.motor.cor_de(Classificacao.BOA), "#2E7D32")
        self.assertEqual(self.motor.cor_de(Classificacao.REGULAR), "#EF6C00")
        self.assertEqual(self.motor.cor_de(Classificacao.RUIM), "#C62828")
        self.assertEqual(self.motor.cor_de(Classificacao.BAIXA), "#EF6C00")


class CalculadoraCorridaClassificacaoTestCase(unittest.TestCase):
    """Valida saida da calculadora usando o motor oficial."""

    def test_deve_expor_nome_da_classificacao_e_cor(self):
        corrida = Corrida(
            valor_total=20.0,
            km_ate_passageiro=4.0,
            km_viagem=10.0,
            tempo_estimado=22,
        )

        resultado = CalculadoraCorrida().calcular(corrida)

        self.assertIsInstance(resultado, AnaliseCorrida)
        self.assertEqual(resultado.classificacao.name, "REGULAR")
        self.assertEqual(resultado.cor_classificacao, "#EF6C00")


if __name__ == "__main__":
    unittest.main()
