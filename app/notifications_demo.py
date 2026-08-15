"""Demo de ponta a ponta do pipeline de notificacoes.

Fluxo:
NotificationData -> Detector -> Parser -> Corrida -> CalculadoraCorrida
"""

from pathlib import Path
import sys

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from core.calculator import CalculadoraCorrida
from notifications.exceptions import NotificationError
from notifications.parser import CorridaParser
from notifications.simulator import NotificationSimulator
from core.settings import Combustivel, ConfiguracaoUsuario


def main() -> None:
    parser = CorridaParser()
    configuracao = ConfiguracaoUsuario(
        marca="Hyundai",
        modelo="HB20",
        versao="Platinum",
        ano=2022,
        consumo_gasolina=13.0,
        consumo_etanol=9.4,
        preco_gasolina=6.19,
        preco_etanol=4.39,
        combustivel=Combustivel.GASOLINA,
    )
    calculadora = CalculadoraCorrida(configuracao_usuario=configuracao)

    print("GESTOR DRIVER - DEMO NOTIFICACOES")
    print("=" * 45)

    for notification in NotificationSimulator.todas():
        print(f"Origem: {notification.package_name}")
        print(f"Mensagem: {notification.full_text}")

        try:
            corrida = parser.parse(notification)
            resultado = calculadora.calcular(corrida)
            print(f"Valor: R$ {resultado.valor_total:.2f}")
            print(f"KM total: {resultado.km_total:.1f}")
            print(f"Tempo: {resultado.tempo_estimado} min")
            print(f"R$/KM: {resultado.valor_por_km:.2f}")
            print(f"Classificacao: {resultado.classificacao.name}")
            print(f"Combustivel estimado: {resultado.combustivel_estimado:.2f} L")
            print(f"Custo estimado: R$ {resultado.custo_combustivel:.2f}")
        except NotificationError as exc:
            print(f"Erro no parse: {exc}")

        print("-" * 45)


if __name__ == "__main__":
    main()
