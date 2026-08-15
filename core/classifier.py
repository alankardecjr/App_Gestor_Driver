"""Enumeracoes de classificacao de rentabilidade de corrida."""

from enum import Enum
from typing import Mapping

from core.constants import CLASSIFICACAO_CORES, CLASSIFICACAO_LIMITES_R_POR_KM

class Classificacao(Enum):
    """Faixas de desempenho financeiro por corrida."""

    EXCELENTE = "excelente"

    BOA = "boa"

    REGULAR = "regular"

    BAIXA = "baixa"

    RUIM = "ruim"


class MotorClassificacao:
    """Classifica corridas e resolve cor visual por faixa de desempenho."""

    def __init__(
        self,
        limites_r_por_km: Mapping[str, float] | None = None,
        cores: Mapping[str, str] | None = None,
    ) -> None:
        limites = dict(CLASSIFICACAO_LIMITES_R_POR_KM)
        if limites_r_por_km:
            limites.update(limites_r_por_km)

        cores_mapa = dict(CLASSIFICACAO_CORES)
        if cores:
            cores_mapa.update(cores)

        self._limites = {
            Classificacao.EXCELENTE: limites["EXCELENTE"],
            Classificacao.BOA: limites["BOA"],
            Classificacao.REGULAR: limites["REGULAR"],
            Classificacao.BAIXA: limites["BAIXA"],
        }
        self._cores = {
            Classificacao.EXCELENTE: cores_mapa["EXCELENTE"],
            Classificacao.BOA: cores_mapa["BOA"],
            Classificacao.REGULAR: cores_mapa["REGULAR"],
            Classificacao.BAIXA: cores_mapa["BAIXA"],
            Classificacao.RUIM: cores_mapa["RUIM"],
        }

    def classificar_por_valor_km(self, valor_por_km: float) -> Classificacao:
        """Retorna a classificacao oficial para um valor em R$/KM."""
        if valor_por_km >= self._limites[Classificacao.EXCELENTE]:
            return Classificacao.EXCELENTE

        if valor_por_km >= self._limites[Classificacao.BOA]:
            return Classificacao.BOA

        if valor_por_km >= self._limites[Classificacao.REGULAR]:
            return Classificacao.REGULAR

        if valor_por_km >= self._limites[Classificacao.BAIXA]:
            return Classificacao.BAIXA

        return Classificacao.RUIM

    def cor_de(self, classificacao: Classificacao) -> str:
        """Retorna a cor visual associada a classificacao."""
        return self._cores[classificacao]