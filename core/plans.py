"""Contrato de planos e controle de recursos da aplicacao."""

from dataclasses import dataclass
from enum import Enum

from core.analysis import AnaliseCorrida


class PlanoAcesso(Enum):
    """Planos disponiveis para o Gestor Driver."""

    FREE = "FREE"
    BETA = "BETA"
    PRO = "PRO"


@dataclass(frozen=True, slots=True)
class RecursosPlano:
    """Permissoes de visibilidade e recursos por plano."""

    exibe_valor_total: bool = True
    exibe_km_total: bool = True
    exibe_tempo: bool = True
    exibe_nota: bool = True
    exibe_classificacao_visual: bool = True
    historico_disponivel: bool = True
    exibe_valor_por_km: bool = False
    exibe_combustivel_estimado: bool = False
    exibe_custo_combustivel: bool = False
    historico_financeiro: bool = False
    exibe_custos_operacionais_completos: bool = False
    exibe_pneus: bool = False
    exibe_oleo: bool = False
    exibe_manutencao: bool = False
    exibe_depreciacao: bool = False
    exibe_r_km_liquido: bool = False
    exibe_relatorios: bool = False
    exibe_estatisticas: bool = False
    recursos_avancados: bool = False


@dataclass(frozen=True, slots=True)
class VisaoPlano:
    """Resultado do controle de plano aplicado a uma analise."""

    analise: AnaliseCorrida
    plano: PlanoAcesso
    recursos: RecursosPlano


class ControlePlano:
    """Resolve recursos disponiveis com base no plano ativo."""

    _RECURSOS_FREE = RecursosPlano()

    _RECURSOS_BETA = RecursosPlano(
        exibe_valor_por_km=True,
        exibe_combustivel_estimado=True,
        exibe_custo_combustivel=True,
        historico_financeiro=True,
    )

    _RECURSOS_PRO = RecursosPlano(
        exibe_valor_por_km=True,
        exibe_combustivel_estimado=True,
        exibe_custo_combustivel=True,
        historico_financeiro=True,
        exibe_custos_operacionais_completos=True,
        exibe_pneus=True,
        exibe_oleo=True,
        exibe_manutencao=True,
        exibe_depreciacao=True,
        exibe_r_km_liquido=True,
        exibe_relatorios=True,
        exibe_estatisticas=True,
        recursos_avancados=True,
    )

    def recursos_do(self, plano: PlanoAcesso) -> RecursosPlano:
        """Retorna o conjunto de recursos disponiveis para o plano."""

        if plano == PlanoAcesso.FREE:
            return self._RECURSOS_FREE

        if plano == PlanoAcesso.BETA:
            return self._RECURSOS_BETA

        return self._RECURSOS_PRO

    def aplicar(self, analise: AnaliseCorrida, plano: PlanoAcesso) -> VisaoPlano:
        """Aplica o plano a uma analise sem recalcular dados."""

        return VisaoPlano(
            analise=analise,
            plano=plano,
            recursos=self.recursos_do(plano),
        )