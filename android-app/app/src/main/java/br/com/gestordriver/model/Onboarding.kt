package br.com.gestordriver.model

enum class OnboardingEtapa {
    NENHUMA,
    PERMISSOES,
    CONTA,
    TUTORIAL,
}

data class TutorialPasso(
    val titulo: String,
    val texto: String,
)

object TutorialConteudo {
    val passos: List<TutorialPasso> = listOf(
        TutorialPasso(
            titulo = "Selo",
            texto = "Bolinha flutuante sobre o mapa. Toque para abrir a análise. Arraste para o canto que não atrapalhe o recusar.",
        ),
        TutorialPasso(
            titulo = "Cabeçalho",
            texto = "💵 R$/KM — valor por km. 💰 VALOR — total da oferta. 🛞 DIST. — km total. 🕐 TEMPO — minutos. ⭐ NOTA — passageiro. ⬇️ abre os detalhes.",
        ),
        TutorialPasso(
            titulo = "Expandida",
            texto = "DISTÂNCIAS: até o passageiro, até o destino e total. CUSTOS: litros, gasto e lucro só do combustível atual (aba CUSTOS + combustível marcado).",
        ),
        TutorialPasso(
            titulo = "Botões",
            texto = "📴 Fechar — encerra o app. ⚙️ Config — abre ajustes. ❎ Ocultar — deixa só o selo. 📜 Histórico — corridas aceitas.",
        ),
        TutorialPasso(
            titulo = "Config",
            texto = "Abas VEÍCULO, CUSTOS, CALIBRAR e APP. Cancelar descarta. Salvar grava e fecha. Campos 🔒 são da versão Pro.",
        ),
        TutorialPasso(
            titulo = "Histórico",
            texto = "Só entra corrida aceita na Uber, 99 ou inDrive. Toque na linha para ver de novo. Recusar não grava.",
        ),
    )
}
