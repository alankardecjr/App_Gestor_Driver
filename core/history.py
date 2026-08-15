"""Modelo e armazenamento local do historico de corridas."""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from json import dumps, loads
from pathlib import Path
from typing import Iterable, Optional

from core.analysis import AnaliseCorrida
from core.classifier import Classificacao


@dataclass(slots=True)
class HistoricoCorrida:
    """Registro persistido de uma corrida analisada.

    O historico armazena o resultado final da analise, nao recalcula
    os indicadores quando e consultado.
    """

    data_hora: datetime
    plataforma: Optional[str]
    valor_total: float
    km_ate_passageiro: float
    km_viagem: float
    km_total: float
    tempo_estimado: Optional[int]
    nota_passageiro: Optional[float]
    valor_por_km: float
    combustivel_estimado: Optional[float]
    custo_combustivel: Optional[float]
    classificacao: Classificacao

    @classmethod
    def from_analise(cls, analise: AnaliseCorrida) -> "HistoricoCorrida":
        """Cria um registro historico a partir de uma analise consolidada."""
        if analise.data_hora is None:
            raise ValueError("AnaliseCorrida precisa ter data_hora para virar historico")

        return cls(
            data_hora=analise.data_hora,
            plataforma=analise.plataforma,
            valor_total=analise.valor_total,
            km_ate_passageiro=analise.km_ate_passageiro,
            km_viagem=analise.km_viagem,
            km_total=analise.km_total,
            tempo_estimado=analise.tempo_estimado,
            nota_passageiro=analise.nota_passageiro,
            valor_por_km=analise.valor_por_km,
            combustivel_estimado=analise.combustivel_estimado,
            custo_combustivel=analise.custo_combustivel,
            classificacao=analise.classificacao,
        )

    def to_dict(self) -> dict:
        """Serializa o registro para persistencia JSON."""
        return {
            "data_hora": self.data_hora.isoformat(),
            "plataforma": self.plataforma,
            "valor_total": self.valor_total,
            "km_ate_passageiro": self.km_ate_passageiro,
            "km_viagem": self.km_viagem,
            "km_total": self.km_total,
            "tempo_estimado": self.tempo_estimado,
            "nota_passageiro": self.nota_passageiro,
            "valor_por_km": self.valor_por_km,
            "combustivel_estimado": self.combustivel_estimado,
            "custo_combustivel": self.custo_combustivel,
            "classificacao": self.classificacao.name,
        }

    @classmethod
    def from_dict(cls, dados: dict) -> "HistoricoCorrida":
        """Reconstrói um registro historico a partir de dados serializados."""
        return cls(
            data_hora=datetime.fromisoformat(dados["data_hora"]),
            plataforma=dados.get("plataforma"),
            valor_total=dados["valor_total"],
            km_ate_passageiro=dados["km_ate_passageiro"],
            km_viagem=dados["km_viagem"],
            km_total=dados["km_total"],
            tempo_estimado=dados.get("tempo_estimado"),
            nota_passageiro=dados.get("nota_passageiro"),
            valor_por_km=dados["valor_por_km"],
            combustivel_estimado=dados.get("combustivel_estimado"),
            custo_combustivel=dados.get("custo_combustivel"),
            classificacao=Classificacao[dados["classificacao"]],
        )


class HistoricoCorridaRepository:
    """Repositorio local simples para persistir o historico em JSON."""

    def __init__(self, arquivo: str | Path):
        self._arquivo = Path(arquivo)

    def salvar(self, historico: HistoricoCorrida) -> None:
        """Adiciona um registro ao historico persistido."""
        itens = self.listar_todos()
        itens.append(historico)
        self._salvar_itens(itens)

    def salvar_analise(self, analise: AnaliseCorrida) -> HistoricoCorrida:
        """Converte uma analise em historico e persiste o registro."""
        historico = HistoricoCorrida.from_analise(analise)
        self.salvar(historico)
        return historico

    def listar_todos(self) -> list[HistoricoCorrida]:
        """Retorna todos os registros persistidos na ordem em que foram salvos."""
        if not self._arquivo.exists():
            return []

        texto = self._arquivo.read_text(encoding="utf-8").strip()
        if not texto:
            return []

        dados = loads(texto)
        return [HistoricoCorrida.from_dict(item) for item in dados]

    def listar_ultimas(self, quantidade: int = 5) -> list[HistoricoCorrida]:
        """Retorna as ultimas corridas salvas, da mais recente para a mais antiga."""
        registros = self.listar_todos()
        return list(reversed(registros[-quantidade:]))

    def _salvar_itens(self, itens: Iterable[HistoricoCorrida]) -> None:
        """Grava a lista completa de registros no arquivo JSON."""
        self._arquivo.parent.mkdir(parents=True, exist_ok=True)
        payload = [item.to_dict() for item in itens]
        self._arquivo.write_text(dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
