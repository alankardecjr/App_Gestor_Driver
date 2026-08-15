"""Pipeline de parse de notificacoes para entidade Corrida."""

from __future__ import annotations

from abc import ABC, abstractmethod

from core.models import Corrida
from core.validator import ValidadorCorrida
from notifications.exceptions import InvalidNotification, UnsupportedPlatform
from notifications.extractor import NotificationExtractor
from notifications.notification import NotificationData
from notifications.platform_detector import Plataforma, PlatformDetector


class BaseNotificationParser(ABC):
	"""Contrato base para parser de cada plataforma."""

	@abstractmethod
	def parse(self, notification: NotificationData) -> Corrida:
		"""Converte NotificationData em Corrida."""


class _ParserPadrao(BaseNotificationParser):
	"""Implementacao padrao baseada em regex para apps de corrida."""

	def parse(self, notification: NotificationData) -> Corrida:
		campos = NotificationExtractor.extrair_campos_padrao(
			notification.full_text
		)
		return Corrida(
			valor_total=campos.valor_total,
			km_ate_passageiro=campos.km_ate_passageiro,
			km_viagem=campos.km_viagem,
			tempo_estimado=campos.tempo_estimado,
		)


class UberParser(_ParserPadrao):
	"""Parser de notificacoes da Uber Driver."""


class Parser99(_ParserPadrao):
	"""Parser de notificacoes do app 99 Driver."""


class InDriveParser(_ParserPadrao):
	"""Parser de notificacoes do app inDrive."""


class CorridaParser:
	"""Orquestra detector + parser por plataforma + validacao de dominio."""

	def __init__(self):
		self._parsers = {
			Plataforma.UBER: UberParser(),
			Plataforma.NOVE_NOVE: Parser99(),
			Plataforma.INDRIVE: InDriveParser(),
		}

	@staticmethod
	def _validar_notification(notification: NotificationData) -> None:
		if notification is None:
			raise InvalidNotification("Notificacao ausente.")
		if not notification.package_name or not notification.package_name.strip():
			raise InvalidNotification("package_name nao informado.")
		if not notification.title and not notification.text:
			raise InvalidNotification("Notificacao sem titulo e sem texto.")

	@staticmethod
	def _validar_corrida(corrida: Corrida) -> None:
		ValidadorCorrida.validar_valor(corrida.valor_total)
		ValidadorCorrida.validar_km(corrida.km_ate_passageiro)
		ValidadorCorrida.validar_km(corrida.km_viagem)

	def parse(self, notification: NotificationData) -> Corrida:
		"""Executa o fluxo completo de parse para Corrida."""

		self._validar_notification(notification)

		plataforma = PlatformDetector.detectar_ou_erro(notification)
		parser = self._parsers.get(plataforma)
		if parser is None:
			raise UnsupportedPlatform(
				f"Parser nao implementado para: {plataforma.value}."
			)

		corrida = parser.parse(notification)
		self._validar_corrida(corrida)
		return corrida

