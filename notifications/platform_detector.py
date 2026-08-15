"""
platform_detector.py

Identifica de qual plataforma uma notificação foi recebida.
"""

from enum import Enum

from notifications.exceptions import InvalidNotification, UnsupportedPlatform
from notifications.notification import NotificationData


class Plataforma(Enum):
    """Plataformas suportadas."""

    UBER = "Uber"
    NOVE_NOVE = "99"
    INDRIVE = "inDrive"
    DESCONHECIDA = "Desconhecida"


PACKAGES = {
    "com.ubercab.driver": Plataforma.UBER,
    "com.taxis99.driver": Plataforma.NOVE_NOVE,
    "sinet.startup.inDriver": Plataforma.INDRIVE,
}


class PlatformDetector:
    """Detecta a plataforma com base no package name."""

    @staticmethod
    def detectar(notification: NotificationData) -> Plataforma:
        if notification is None:
            raise InvalidNotification("Notificacao ausente.")
        if not notification.package_name or not notification.package_name.strip():
            raise InvalidNotification("package_name nao informado.")

        return PACKAGES.get(
            notification.package_name,
            Plataforma.DESCONHECIDA
        )

    @staticmethod
    def detectar_ou_erro(notification: NotificationData) -> Plataforma:
        """Detecta a plataforma e falha explicitamente se nao suportada."""

        plataforma = PlatformDetector.detectar(notification)
        if plataforma == Plataforma.DESCONHECIDA:
            raise UnsupportedPlatform(
                f"Plataforma nao suportada: {notification.package_name}."
            )
        return plataforma