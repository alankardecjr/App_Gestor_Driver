"""Gerador de notificacoes simuladas para testes manuais e demos."""

from __future__ import annotations

from notifications.notification import NotificationData


class NotificationSimulator:
	"""Fornece exemplos representativos de notificacoes por plataforma."""

	@staticmethod
	def uber() -> NotificationData:
		return NotificationData(
			package_name="com.ubercab.driver",
			title="Nova viagem disponivel",
			text="R$ 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min",
		)

	@staticmethod
	def nove_nove() -> NotificationData:
		return NotificationData(
			package_name="com.app99.driver",
			title="Corrida 99",
			text="Ganhe R$ 22,50 em 1,5 km + 7,0 km. Tempo estimado: 18 min",
		)

	@staticmethod
	def indrive() -> NotificationData:
		return NotificationData(
			package_name="sinet.startup.inDriver",
			title="Pedido inDrive",
			text="Oferta R$ 31,40 | 2,0 km coleta | 10,3 km destino | 21 min",
		)

	@staticmethod
	def desconhecida() -> NotificationData:
		return NotificationData(
			package_name="com.exemplo.outroapp",
			title="Oferta",
			text="R$ 20,00 5 km 10 min",
		)

	@classmethod
	def todas(cls) -> list[NotificationData]:
		"""Retorna um conjunto de notificacoes para smoke test local."""

		return [
			cls.uber(),
			cls.nove_nove(),
			cls.indrive(),
			cls.desconhecida(),
		]

