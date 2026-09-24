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

    def test_abaixo_da_boa_deve_ser_ruim(self):
        # Modelo de 3 faixas: entre RUIM e BOA nao existe mais REGULAR.
        self.assertEqual(
            self.motor.classificar_por_valor_km(1.30),
            Classificacao.RUIM,
        )

    def test_deve_classificar_com_boa(self):
        self.assertEqual(
            self.motor.classificar_por_valor_km(1.70),
            Classificacao.BOA,
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

        # 2,10 < BOA(2,50) -> RUIM no modelo de 3 faixas.
        self.assertEqual(motor.classificar_por_valor_km(2.10), Classificacao.RUIM)
        self.assertEqual(motor.classificar_por_valor_km(2.60), Classificacao.BOA)
        self.assertEqual(motor.classificar_por_valor_km(3.10), Classificacao.EXCELENTE)

    def test_deve_retornar_cor_por_classificacao(self):
        self.assertEqual(self.motor.cor_de(Classificacao.EXCELENTE), "#2E7D32")
        self.assertEqual(self.motor.cor_de(Classificacao.BOA), "#F9A825")
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

        # 20,00 / 14 km = 1,43 -> abaixo da BOA(1,60) -> RUIM (3 faixas).
        self.assertIsInstance(resultado, AnaliseCorrida)
        self.assertEqual(resultado.classificacao.name, "RUIM")
        self.assertEqual(resultado.cor_classificacao, "#C62828")


if __name__ == "__main__":
    unittest.main()
