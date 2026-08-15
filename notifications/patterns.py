"""
patterns.py

Expressões regulares utilizadas para extrair informações
das notificações de corridas.
"""

import re


class Patterns:
    """Regex utilizadas pelo sistema."""

    # Valor monetário
    VALOR = re.compile(
        r"R\$\s*([\d.,]+)"
    )

    # Distancias com unidade (km ou m), preservando ordem de aparicao
    DISTANCIA_COM_UNIDADE = re.compile(
        r"([\d.,]+)\s*(km|m)\b",
        re.IGNORECASE
    )

    # Tempo estimado (min, mins, minuto, minutos)
    TEMPO = re.compile(
        r"(\d+)\s*(min|mins|minuto|minutos)\b",
        re.IGNORECASE
    )

    # Nota do passageiro
    NOTA = re.compile(
        r"(\d+[.,]\d+)\s*⭐"
    )