"""Regras de validacao de entrada para dados de corrida."""

class ValidadorCorrida:
    """Centraliza validacoes basicas de integridade dos dados."""

    @staticmethod
    def validar_valor(valor: float):
        """Garante que o valor da corrida seja positivo.

        Args:
            valor: Valor total informado para a corrida.

        Raises:
            ValueError: Quando o valor for menor ou igual a zero.
        """

        if valor <= 0:
            raise ValueError(
                "Valor da corrida inválido."
            )

    @staticmethod
    def validar_km(km: float):
        """Garante que a quilometragem nao seja negativa.

        Args:
            km: Distancia informada para validacao.

        Raises:
            ValueError: Quando a distancia for negativa.
        """

        if km < 0:
            raise ValueError(
                "Quilometragem inválida."
            )