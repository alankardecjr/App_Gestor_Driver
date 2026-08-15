"""Contrato oficial do resultado consolidado de uma corrida.

Este modulo concentra tudo o que a interface, o historico e o futuro
armazenamento precisam enxergar depois que uma corrida foi analisada.
"""

from dataclasses import dataclass
from datetime import datetime
from typing import Optional

from core.classifier import Classificacao
from core.models import Corrida


@dataclass(slots=True)
class AnaliseCorrida:
    """Resultado consolidado de uma corrida analisada.

    Attributes:
        corrida: Entidade original recebida do parser.
        valor_total: Valor bruto da corrida.
        km_ate_passageiro: Distancia ate o ponto de embarque.
        km_viagem: Distancia do trecho com passageiro.
        tempo_estimado: Duracao prevista da corrida.
        nota_passageiro: Nota do passageiro, quando disponivel.
        plataforma: Nome da plataforma de origem, quando disponivel.
        data_hora: Momento em que a analise foi gerada.
        km_total: Distancia total considerada na analise.
        valor_por_km: Indicador principal de rentabilidade.
        combustivel_estimado: Litros estimados para o trajeto.
        custo_combustivel: Custo estimado com combustivel.
        classificacao: Classificacao oficial da corrida.
        cor_classificacao: Cor visual associada a classificacao.
    """

    corrida: Corrida

    valor_total: float
    km_ate_passageiro: float
    km_viagem: float
    tempo_estimado: Optional[int]
    nota_passageiro: Optional[float]
    plataforma: Optional[str]
    data_hora: Optional[datetime]

    km_total: float
    valor_por_km: float

    combustivel_estimado: Optional[float]
    custo_combustivel: Optional[float]

    classificacao: Classificacao
    cor_classificacao: str