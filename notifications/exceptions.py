"""Excecoes de dominio para pipeline de leitura de notificacoes.

Essas excecoes tornam os erros explicitos entre detector, extractor e
parsers, evitando falhas silenciosas e facilitando testes automatizados.
"""


class NotificationError(Exception):
    """Erro generico relacionado ao processamento de notificacoes."""


class UnsupportedPlatform(NotificationError):
    """Sinaliza plataforma de corrida ainda nao suportada."""


class InvalidNotification(NotificationError):
    """Indica que a notificacao recebida esta incompleta ou invalida."""


class ExtractionError(NotificationError):
    """Representa falha ao extrair campos da mensagem da notificacao."""

