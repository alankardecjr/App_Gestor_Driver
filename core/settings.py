"""Configuracoes de veiculo e combustivel usadas na analise de custo."""

from dataclasses import dataclass
from enum import Enum


class Combustivel(Enum):
    """Tipos de combustivel suportados pelo calculo."""

    GASOLINA = "gasolina"

    ETANOL = "etanol"


@dataclass(slots=True)
class ConfiguracaoUsuario:
    """Dados de perfil do motorista e parametros do veiculo.

    Attributes:
        marca: Fabricante do veiculo.
        modelo: Modelo comercial do veiculo.
        versao: Versao ou motorizacao do modelo.
        ano: Ano de fabricacao/modelo.
        consumo_gasolina: Rendimento medio com gasolina (km/l).
        consumo_etanol: Rendimento medio com etanol (km/l).
        preco_gasolina: Preco local da gasolina por litro.
        preco_etanol: Preco local do etanol por litro.
        combustivel: Combustivel atualmente utilizado no app.
    """

    marca: str

    modelo: str

    versao: str

    ano: int

    consumo_gasolina: float

    consumo_etanol: float

    preco_gasolina: float

    preco_etanol: float

    combustivel: Combustivel

    def consumo_ativo(self) -> float:
        """Retorna o consumo correspondente ao combustivel em uso."""
        if self.combustivel == Combustivel.GASOLINA:
            return self.consumo_gasolina

        return self.consumo_etanol

    def preco_ativo(self) -> float:
        """Retorna o preco correspondente ao combustivel em uso."""
        if self.combustivel == Combustivel.GASOLINA:
            return self.preco_gasolina

        return self.preco_etanol