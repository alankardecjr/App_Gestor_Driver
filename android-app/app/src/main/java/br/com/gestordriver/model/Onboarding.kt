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
            titulo = "SELO",
            texto = "Bolinha flutuante sobre o mapa. Toque para abrir a análise. Arraste para o canto que não atrapalhe o recusar.",
        ),
        TutorialPasso(
            titulo = "CABEÇALHO",
            texto = "💵 R$/KM — valor por km. 💰 VALOR — total da oferta. 🛞 DIST. — km total. 🕐 TEMPO — minutos. ⭐ NOTA — passageiro. ⬇️ abre os detalhes.",
        ),
        TutorialPasso(
            titulo = "EXPANDIDA",
            texto = "DISTÂNCIAS: até o passageiro, até o destino e total. CUSTOS: litros, gasto e lucro só do combustível atual (aba CUSTOS + combustível marcado).",
        ),
        TutorialPasso(
            titulo = "BOTÕES",
            texto = "📴 Fechar — encerra o app. ⚙️CONFIG — abre ajustes. ❎ Ocultar — deixa só o selo. 📜 Histórico — corridas aceitas.",
        ),
        TutorialPasso(
            titulo = "CONFIG",
            texto = "Abas VEÍCULO, CUSTOS, CLASSIFICAÇÃO e APP. CANCELAR descarta. SALVAR grava e fecha. Campos 🔒 são da versão Pro.",
        ),
        TutorialPasso(
            titulo = "HISTÓRICO",
            texto = "Só entra corrida aceita na Uber, 99 ou inDrive. Toque na linha para ver de novo. Recusar não grava.",
        ),
    )
}
