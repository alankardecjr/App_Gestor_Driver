"""Testes do historico local de corridas."""

import tempfile
import unittest
from datetime import datetime
from pathlib import Path

from core.analysis import AnaliseCorrida
from core.classifier import Classificacao
from core.history import HistoricoCorrida, HistoricoCorridaRepository
from core.models import Corrida


class HistoricoCorridaRepositoryTestCase(unittest.TestCase):
    """Valida inclusao, serializacao e consulta do historico."""

    def _criar_analise(self, valor_total: float, classificacao: Classificacao) -> AnaliseCorrida:
        corrida = Corrida(
            valor_total=valor_total,
            km_ate_passageiro=2.0,
            km_viagem=8.0,
            tempo_estimado=18,
        )

        return AnaliseCorrida(
            corrida=corrida,
            valor_total=valor_total,
            km_ate_passageiro=2.0,
            km_viagem=8.0,
            tempo_estimado=18,
            nota_passageiro=4.9,
            plataforma="Uber",
            data_hora=datetime(2026, 8, 1, 12, 30),
            km_total=10.0,
            valor_por_km=valor_total / 10.0,
            combustivel_estimado=1.0,
            custo_combustivel=6.19,
            classificacao=classificacao,
            cor_classificacao="#2E7D32",
        )

    def test_deve_salvar_e_recuperar_historico(self):
        with tempfile.TemporaryDirectory() as diretorio_temporario:
            arquivo = Path(diretorio_temporario) / "historico.json"
            repository = HistoricoCorridaRepository(arquivo)

            analise = self._criar_analise(30.0, Classificacao.BOA)
            historico = repository.salvar_analise(analise)

            self.assertIsInstance(historico, HistoricoCorrida)
            self.assertTrue(arquivo.exists())
            self.assertEqual(historico.valor_total, 30.0)
            self.assertEqual(historico.classificacao, Classificacao.BOA)

            registros = repository.listar_todos()
            self.assertEqual(len(registros), 1)
            self.assertEqual(registros[0].plataforma, "Uber")
            self.assertEqual(registros[0].km_total, 10.0)
            self.assertEqual(registros[0].custo_combustivel, 6.19)

    def test_deve_retornar_ultimas_corridas_em_ordem_recente(self):
        with tempfile.TemporaryDirectory() as diretorio_temporario:
            arquivo = Path(diretorio_temporario) / "historico.json"
            repository = HistoricoCorridaRepository(arquivo)

            repository.salvar_analise(self._criar_analise(25.0, Classificacao.REGULAR))
            repository.salvar_analise(self._criar_analise(32.0, Classificacao.BOA))
            repository.salvar_analise(self._criar_analise(40.0, Classificacao.EXCELENTE))

            ultimas = repository.listar_ultimas(2)

            self.assertEqual(len(ultimas), 2)
            self.assertEqual(ultimas[0].valor_total, 40.0)
            self.assertEqual(ultimas[0].classificacao, Classificacao.EXCELENTE)
            self.assertEqual(ultimas[1].valor_total, 32.0)
            self.assertEqual(ultimas[1].classificacao, Classificacao.BOA)


if __name__ == "__main__":
    unittest.main()
