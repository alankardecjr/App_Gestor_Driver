"""
notification.py

Representa uma notificação recebida de um aplicativo de transporte.

Este objeto padroniza os dados recebidos, permitindo que o restante
do sistema trabalhe de forma independente da origem da notificação.
"""

from dataclasses import dataclass
from datetime import datetime
from typing import Optional


@dataclass(slots=True)
class NotificationData:
    """
    Representa uma notificação recebida.

    Attributes:
        package_name: Nome do pacote Android.
        title: Título da notificação.
        text: Conteúdo principal da notificação.
        received_at: Data e hora do recebimento.
    """

    package_name: str
    title: str
    text: str
    received_at: Optional[datetime] = None

    def __post_init__(self):
        """Define automaticamente o horário caso não seja informado."""

        if self.received_at is None:
            self.received_at = datetime.now()

    @property
    def full_text(self) -> str:
        """
        Retorna título + texto em um único bloco.
        Facilita o trabalho dos parsers.
        """

        return f"{self.title}\n{self.text}".strip()

    def __str__(self) -> str:
        return (
            f"{self.package_name} | "
            f"{self.received_at:%d/%m/%Y %H:%M:%S}"
        )