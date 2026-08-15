"""Extracao de informacoes estruturadas a partir de texto de notificacao."""

from __future__ import annotations

from dataclasses import dataclass

from notifications.exceptions import ExtractionError
from notifications.patterns import Patterns


@dataclass(slots=True)
class CamposExtraidos:
    """Campos extraidos para construcao de uma corrida."""

    valor_total: float
    km_ate_passageiro: float
    km_viagem: float
    tempo_estimado: int | None


class NotificationExtractor:
	"""Responsavel por extrair dados numericos de notificacoes."""

	@staticmethod
	def _normalizar_numero(texto: str) -> float:
		"""Converte formatos monetarios/decimais comuns para float.

		Suporta padroes como "1.234,56", "1234,56", "1234.56" e "12".
		"""

		valor = texto.strip().replace(" ", "")
		if not valor:
			raise ExtractionError("Valor numerico vazio na notificacao.")

		if "," in valor and "." in valor:
			valor = valor.replace(".", "").replace(",", ".")
		elif "," in valor:
			valor = valor.replace(",", ".")

		try:
			return float(valor)
		except ValueError as exc:
			raise ExtractionError(
				f"Nao foi possivel converter numero: '{texto}'."
			) from exc

	@classmethod
	def extrair_valor(cls, texto: str) -> float:
		"""Extrai o valor total da corrida."""

		match = Patterns.VALOR.search(texto)
		if not match:
			raise ExtractionError("Valor da corrida nao encontrado.")
		return cls._normalizar_numero(match.group(1))

	@classmethod
	def extrair_distancias(cls, texto: str) -> list[float]:
		"""Extrai distancias e converte para km quando necessario."""

		distancias_km: list[float] = []
		for match in Patterns.DISTANCIA_COM_UNIDADE.finditer(texto):
			valor_texto = match.group(1)
			unidade = match.group(2).lower()
			valor = cls._normalizar_numero(valor_texto)
			if unidade == "m":
				valor = valor / 1000
			distancias_km.append(valor)

		if not distancias_km:
			raise ExtractionError("Nenhuma distancia encontrada.")

		return distancias_km

	@staticmethod
	def extrair_tempo(texto: str) -> int | None:
		"""Extrai tempo estimado em minutos, quando presente."""

		match = Patterns.TEMPO.search(texto)
		if not match:
			return None
		return int(match.group(1))

	@classmethod
	def extrair_campos_padrao(cls, texto: str) -> CamposExtraidos:
		"""Extrai campos principais no formato padrao de corrida.

		Regra para distancias:
		- 2+ distancias: primeira = ate passageiro, segunda = viagem.
		- 1 distancia: assume corrida direta (0 ate passageiro).
		"""

		valor_total = cls.extrair_valor(texto)
		distancias = cls.extrair_distancias(texto)
		tempo_estimado = cls.extrair_tempo(texto)

		if len(distancias) >= 2:
			km_ate_passageiro = distancias[0]
			km_viagem = distancias[1]
		else:
			km_ate_passageiro = 0.0
			km_viagem = distancias[0]

		return CamposExtraidos(
			valor_total=valor_total,
			km_ate_passageiro=km_ate_passageiro,
			km_viagem=km_viagem,
			tempo_estimado=tempo_estimado,
		)

