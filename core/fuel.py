"""Calculos de consumo e custo de combustivel por corrida."""

from dataclasses import dataclass

@dataclass(slots=True)
class ResultadoCombustivel:
    """Resultado financeiro e volumetrico do combustivel usado.

    Attributes:
        litros: Litros estimados para percorrer o trajeto.
        custo: Custo total estimado com combustivel.
    """

    litros: float

    custo: float


class CalculadoraCombustivel:
    """Executa estimativas de combustivel com base em consumo medio."""

    @staticmethod
    def calcular(
        km_total: float,
        consumo_km_l: float,
        preco_litro: float
    ) -> ResultadoCombustivel:
        """Calcula litros e custo para um percurso informado.

        Args:
            km_total: Distancia total percorrida na corrida.
            consumo_km_l: Rendimento do veiculo em km por litro.
            preco_litro: Preco unitario do combustivel selecionado.

        Returns:
            Objeto com litros estimados e custo correspondente.
        """

        litros = km_total / consumo_km_l
        custo = litros * preco_litro

        return ResultadoCombustivel(
            litros=litros,
            custo=custo
        )