"""Modelos de dominio centrais da aplicacao de analise de corridas."""

from dataclasses import dataclass
from typing import Optional


@dataclass
class Corrida:
    """Representa uma corrida ofertada por aplicativo.

    Attributes:
        valor_total: Valor bruto oferecido pela plataforma.
        km_ate_passageiro: Distancia ate o ponto de embarque.
        km_viagem: Distancia do trajeto com passageiro.
        tempo_estimado: Duracao prevista da corrida em minutos.
    """

    valor_total: float
    km_ate_passageiro: float
    km_viagem: float
    tempo_estimado: Optional[int] = None

    @property
    def km_total(self) -> float:
        """Retorna a distancia total considerada na tomada de decisao."""
        return self.km_ate_passageiro + self.km_viagem

    @property
    def valor_por_km(self) -> float:
        """Calcula o valor por km evitando divisao por zero."""
        if self.km_total <= 0:
            return 0.0

        return self.valor_total / self.km_total