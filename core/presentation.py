"""Camada de apresentacao para corrida, plano e modo visual.

O modelo consolida a analise, o controle de recursos e a regra de
compacto/detalhes sem recalcular a corrida.
"""

from dataclasses import dataclass, field
from enum import Enum
from typing import Iterable

from core.analysis import AnaliseCorrida
from core.classifier import Classificacao
from core.constants import CLASSIFICACAO_CORES
from core.history import HistoricoCorrida
from core.plans import ControlePlano, PlanoAcesso, VisaoPlano


class ModoApresentacao(Enum):
    """Modos disponiveis para a interface da corrida."""

    COMPACTA = "compacta"
    DETALHES = "detalhes"


@dataclass(slots=True)
class CampoApresentacao:
    """Campo pronto para renderizacao na interface."""

    chave: str
    rotulo: str
    valor_formatado: str
    permitido: bool = True
    destaque: bool = False

    @property
    def texto_exibicao(self) -> str:
        """Texto final exibido pela interface."""

        if not self.permitido:
            return "🔒"

        return self.valor_formatado


@dataclass(slots=True)
class ClassificacaoVisual:
    """Classificacao visual centralizada no contrato de apresentacao."""

    classificacao: Classificacao
    rotulo: str
    cor: str


@dataclass(slots=True)
class PresentationModel:
    """Modelo de apresentacao da corrida para a interface."""

    analise: AnaliseCorrida
    visao_plano: VisaoPlano
    modo: ModoApresentacao = ModoApresentacao.COMPACTA
    classificacao_visual: ClassificacaoVisual = field(init=False)
    campos_compactos: tuple[CampoApresentacao, ...] = field(init=False)
    campos_detalhes: tuple[CampoApresentacao, ...] = field(init=False)

    def __post_init__(self) -> None:
        self.classificacao_visual = ClassificacaoVisual(
            classificacao=self.analise.classificacao,
            rotulo=_rotulo_classificacao(self.analise.classificacao),
            cor=self.analise.cor_classificacao,
        )
        self.campos_compactos = self._montar_campos_compactos()
        self.campos_detalhes = self._montar_campos_detalhes()

    @classmethod
    def criar(
        cls,
        analise: AnaliseCorrida,
        plano: PlanoAcesso,
        modo: ModoApresentacao = ModoApresentacao.COMPACTA,
        controle_plano: ControlePlano | None = None,
    ) -> "PresentationModel":
        """Cria a apresentacao a partir da analise consolidada e do plano."""

        controle = controle_plano or ControlePlano()
        visao_plano = controle.aplicar(analise, plano)
        return cls(analise=analise, visao_plano=visao_plano, modo=modo)

    @property
    def plano(self) -> PlanoAcesso:
        """Plano ativo da apresentacao."""

        return self.visao_plano.plano

    @property
    def recursos(self):
        """Recursos liberados pelo plano ativo."""

        return self.visao_plano.recursos

    @property
    def modo_expandido(self) -> bool:
        """Indica se a interface esta no modo expandido."""

        return self.modo == ModoApresentacao.DETALHES

    @property
    def acao_detalhes(self) -> str:
        """Texto dinamico do botao de alternancia da interface."""

        if self.modo_expandido:
            return "Menos detalhes"

        return "Mais detalhes"

    @property
    def campos_visiveis(self) -> tuple[CampoApresentacao, ...]:
        """Campos que a interface deve considerar no modo atual."""

        if self.modo_expandido:
            return self.campos_compactos + self.campos_detalhes

        return self.campos_compactos

    def campo_por_chave(self, chave: str) -> CampoApresentacao | None:
        """Busca um campo pela chave de apresentacao."""

        for campo in self.campos_visiveis:
            if campo.chave == chave:
                return campo

        return None

    def _montar_campos_compactos(self) -> tuple[CampoApresentacao, ...]:
        recursos = self.recursos
        return (
            CampoApresentacao(
                chave="valor_por_km",
                rotulo="R$/KM",
                valor_formatado=_formatar_decimal(self.analise.valor_por_km, 2),
                permitido=recursos.exibe_valor_por_km,
                destaque=True,
            ),
            CampoApresentacao(
                chave="valor_total",
                rotulo="Valor total",
                valor_formatado=_formatar_moeda(self.analise.valor_total),
                permitido=recursos.exibe_valor_total,
            ),
            CampoApresentacao(
                chave="km_total",
                rotulo="KM total",
                valor_formatado=_formatar_km(self.analise.km_total),
                permitido=recursos.exibe_km_total,
            ),
            CampoApresentacao(
                chave="tempo_estimado",
                rotulo="Tempo",
                valor_formatado=_formatar_tempo(self.analise.tempo_estimado),
                permitido=recursos.exibe_tempo,
            ),
            CampoApresentacao(
                chave="nota_passageiro",
                rotulo="Nota",
                valor_formatado=_formatar_decimal_opcional(self.analise.nota_passageiro),
                permitido=recursos.exibe_nota,
            ),
        )

    def _montar_campos_detalhes(self) -> tuple[CampoApresentacao, ...]:
        recursos = self.recursos
        return (
            CampoApresentacao(
                chave="km_ate_passageiro",
                rotulo="Passageiro",
                valor_formatado=_formatar_km(self.analise.km_ate_passageiro),
                permitido=True,
            ),
            CampoApresentacao(
                chave="km_viagem",
                rotulo="Destino",
                valor_formatado=_formatar_km(self.analise.km_viagem),
                permitido=True,
            ),
            CampoApresentacao(
                chave="combustivel_estimado",
                rotulo="Combustível",
                valor_formatado=_formatar_litros_opcional(self.analise.combustivel_estimado),
                permitido=recursos.exibe_combustivel_estimado,
            ),
            CampoApresentacao(
                chave="custo_combustivel",
                rotulo="Gasto estimado",
                valor_formatado=_formatar_moeda_opcional(self.analise.custo_combustivel),
                permitido=recursos.exibe_custo_combustivel,
            ),
        )


def _rotulo_classificacao(classificacao: Classificacao) -> str:
    return classificacao.name.capitalize()


@dataclass(slots=True)
class HistoricoItemPresentation:
    """Representacao horizontal de uma corrida do historico."""

    historico: HistoricoCorrida
    classificacao_visual: ClassificacaoVisual = field(init=False)
    campos_horizontais: tuple[CampoApresentacao, ...] = field(init=False)

    def __post_init__(self) -> None:
        self.classificacao_visual = ClassificacaoVisual(
            classificacao=self.historico.classificacao,
            rotulo=_rotulo_classificacao(self.historico.classificacao),
            cor=CLASSIFICACAO_CORES[self.historico.classificacao.name],
        )
        self.campos_horizontais = (
            CampoApresentacao(
                chave="valor_por_km",
                rotulo="R$/KM",
                valor_formatado=_formatar_decimal(self.historico.valor_por_km, 2),
                permitido=True,
                destaque=True,
            ),
            CampoApresentacao(
                chave="valor_total",
                rotulo="Valor total",
                valor_formatado=_formatar_moeda(self.historico.valor_total),
                permitido=True,
            ),
            CampoApresentacao(
                chave="km_total",
                rotulo="KM total",
                valor_formatado=_formatar_km(self.historico.km_total),
                permitido=True,
            ),
            CampoApresentacao(
                chave="tempo_estimado",
                rotulo="Tempo",
                valor_formatado=_formatar_tempo(self.historico.tempo_estimado),
                permitido=True,
            ),
            CampoApresentacao(
                chave="nota_passageiro",
                rotulo="Nota",
                valor_formatado=_formatar_decimal_opcional(self.historico.nota_passageiro),
                permitido=True,
            ),
        )

    @property
    def linha_horizontal(self) -> str:
        """Linha compacta pronta para renderizacao em lista horizontal."""

        return " │ ".join(campo.texto_exibicao for campo in self.campos_horizontais)


@dataclass(slots=True)
class HistoricoPresentation:
    """Lista de historicos pronta para exibicao horizontal."""

    itens: tuple[HistoricoItemPresentation, ...]

    @classmethod
    def criar(cls, historicos: Iterable[HistoricoCorrida]) -> "HistoricoPresentation":
        return cls(tuple(HistoricoItemPresentation(historico) for historico in historicos))

    @property
    def linhas_horizontais(self) -> tuple[str, ...]:
        return tuple(item.linha_horizontal for item in self.itens)


def _formatar_decimal(valor: float, casas: int) -> str:
    return f"{valor:.{casas}f}".replace(".", ",")


def _formatar_decimal_opcional(valor: float | None, casas: int = 2) -> str:
    if valor is None:
        return "—"

    return _formatar_decimal(valor, casas)


def _formatar_moeda(valor: float) -> str:
    return f"R$ {valor:.2f}".replace(".", ",")


def _formatar_moeda_opcional(valor: float | None) -> str:
    if valor is None:
        return "—"

    return _formatar_moeda(valor)


def _formatar_km(valor: float) -> str:
    texto = _formatar_decimal(valor, 1)
    if texto.endswith(",0"):
        texto = texto[:-2]

    return f"{texto} km"


def _formatar_tempo(valor: int | None) -> str:
    if valor is None:
        return "—"

    return f"{valor} min"


def _formatar_litros_opcional(valor: float | None) -> str:
    if valor is None:
        return "—"

    return f"{_formatar_decimal(valor, 2)} L"