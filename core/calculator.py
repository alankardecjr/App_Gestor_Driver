"""Motor principal de calculo e classificacao de corridas.

Contem a logica de negocio para transformar uma entidade Corrida em um
resumo pronto para exibicao, incluindo o indicador valor por km.
"""

from datetime import datetime

from core.analysis import AnaliseCorrida
from core.classifier import MotorClassificacao
from core.fuel import CalculadoraCombustivel
from core.models import Corrida
from core.settings import ConfiguracaoUsuario


class CalculadoraCorrida:
    """Orquestra o calculo dos indicadores exibidos ao motorista."""

    def __init__(
        self,
        classificador: MotorClassificacao | None = None,
        calculadora_combustivel: CalculadoraCombustivel | None = None,
        configuracao_usuario: ConfiguracaoUsuario | None = None,
    ):
        """Inicializa dependencias de classificacao."""
        self.classificador = classificador or MotorClassificacao()
        self.calculadora_combustivel = (
            calculadora_combustivel or CalculadoraCombustivel()
        )
        self.configuracao_usuario = configuracao_usuario

    def calcular(
        self,
        corrida: Corrida,
        configuracao_usuario: ConfiguracaoUsuario | None = None,
    ) -> AnaliseCorrida:
        """Consolida os dados da corrida em um contrato de analise.

        Args:
            corrida: Objeto com valor, distancia e tempo estimado.

        Returns:
            Objeto com metricas operacionais, classificacao e campos
            preparados para evolucao do historico.
        """

        valor_por_km = corrida.valor_por_km

        classificacao = self.classificador.classificar_por_valor_km(
            valor_por_km
        )

        configuracao_ativa = configuracao_usuario or self.configuracao_usuario
        resultado_combustivel = None

        if configuracao_ativa is not None:
            resultado_combustivel = self.calculadora_combustivel.calcular(
                km_total=corrida.km_total,
                consumo_km_l=configuracao_ativa.consumo_ativo(),
                preco_litro=configuracao_ativa.preco_ativo(),
            )

        return AnaliseCorrida(
            corrida=corrida,
            valor_total=corrida.valor_total,
            km_ate_passageiro=corrida.km_ate_passageiro,
            km_viagem=corrida.km_viagem,
            tempo_estimado=corrida.tempo_estimado,
            nota_passageiro=None,
            plataforma=None,
            data_hora=datetime.now(),
            km_total=corrida.km_total,
            valor_por_km=valor_por_km,
            combustivel_estimado=(
                resultado_combustivel.litros if resultado_combustivel else None
            ),
            custo_combustivel=(
                resultado_combustivel.custo if resultado_combustivel else None
            ),
            classificacao=classificacao,
            cor_classificacao=self.classificador.cor_de(classificacao),
        )