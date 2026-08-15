"""Testes unitarios do pipeline de notificacoes.

Cobre extracao, parse por plataforma e tratamento de plataformas
nao suportadas.
"""

import unittest

from core.models import Corrida
from notifications.exceptions import ExtractionError, UnsupportedPlatform
from notifications.extractor import NotificationExtractor
from notifications.parser import CorridaParser
from notifications.simulator import NotificationSimulator


class NotificationExtractorTestCase(unittest.TestCase):
    """Valida regras de extracao de campos numericos."""

    def test_deve_extrair_campos_padrao(self):
        texto = "R$ 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min"

        campos = NotificationExtractor.extrair_campos_padrao(texto)

        self.assertAlmostEqual(campos.valor_total, 38.0)
        self.assertAlmostEqual(campos.km_ate_passageiro, 3.2)
        self.assertAlmostEqual(campos.km_viagem, 12.8)
        self.assertEqual(campos.tempo_estimado, 24)

    def test_deve_levantar_erro_sem_valor(self):
        texto = "3,2 km ate o passageiro • 12,8 km viagem • 24 min"

        with self.assertRaises(ExtractionError):
            NotificationExtractor.extrair_campos_padrao(texto)

    def test_deve_converter_metros_para_km(self):
        texto = "R$ 18,00 • 350 m ate passageiro • 6,5 km viagem • 12 min"

        campos = NotificationExtractor.extrair_campos_padrao(texto)

        self.assertAlmostEqual(campos.km_ate_passageiro, 0.35)
        self.assertAlmostEqual(campos.km_viagem, 6.5)

    def test_deve_extrair_tempo_em_minutos_por_extenso(self):
        texto = "R$ 20,00 • 2,0 km ate passageiro • 7,0 km viagem • 19 minutos"

        campos = NotificationExtractor.extrair_campos_padrao(texto)

        self.assertEqual(campos.tempo_estimado, 19)


class CorridaParserTestCase(unittest.TestCase):
    """Valida parse de notificacoes para entidade Corrida."""

    def setUp(self):
        self.parser = CorridaParser()

    def test_deve_parsear_notificacao_uber(self):
        notification = NotificationSimulator.uber()

        corrida = self.parser.parse(notification)

        self.assertIsInstance(corrida, Corrida)
        self.assertAlmostEqual(corrida.valor_total, 38.0)
        self.assertAlmostEqual(corrida.km_ate_passageiro, 3.2)
        self.assertAlmostEqual(corrida.km_viagem, 12.8)
        self.assertEqual(corrida.tempo_estimado, 24)

    def test_deve_parsear_notificacao_99(self):
        notification = NotificationSimulator.nove_nove()

        corrida = self.parser.parse(notification)

        self.assertAlmostEqual(corrida.valor_total, 22.5)
        self.assertAlmostEqual(corrida.km_ate_passageiro, 1.5)
        self.assertAlmostEqual(corrida.km_viagem, 7.0)
        self.assertEqual(corrida.tempo_estimado, 18)

    def test_deve_falhar_para_plataforma_desconhecida(self):
        notification = NotificationSimulator.desconhecida()

        with self.assertRaises(UnsupportedPlatform):
            self.parser.parse(notification)


if __name__ == "__main__":
    unittest.main()
