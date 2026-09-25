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
            texto = "Ícone redondo ~60 dp. Toque abre/fecha o menu atalho. Arraste para o X na base para esconder o selo; monitoramento e notificação continuam.",
        ),
        TutorialPasso(
            titulo = "Compacta",
            texto = "Card no topo só na oferta: R$/km, R$/hora, tempo e nota. O X descarta a oferta. Some sozinha quando expira. Toque na compacta ou fora não faz nada.",
        ),
        TutorialPasso(
            titulo = "Menu",
            texto = "Histórico, Dashboard, Despesas, Semáforo, Veiculo, Sistema e Fechar. A seta das abas sempre volta ao atalho.",
        ),
        TutorialPasso(
            titulo = "Notificação",
            texto = "Resumo da oferta. Expira, recusa ou aceite: a compacta some e volta o selo. Arraste para o lugar do mapa. X fecha. Abrir e Desativar. Fechar o app fica só no menu.",
        ),
        TutorialPasso(
            titulo = "Config",
            texto = "Abas em formato de ficheiro: Histórico, Dashboard, Despesas, Semáforo, Veiculo e Sistema. Cancelar descarta. Salvar grava.",
        ),
        TutorialPasso(
            titulo = "Histórico",
            texto = "Só entra corrida aceita. Filtro Todos/Uber/99/inDrive. Semana (DOM–SÁB) com setas. Card: ganhos, R$/km, resultado, gasto e nota.",
        ),
    )
}
