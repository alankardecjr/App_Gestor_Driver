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
            texto = "Card no topo só na oferta: $/Km, $/Lucro, $/Gasto e Nota. Some em 1 s (expirou/recusou) ou 2 s (aceitou). Toque na compacta ou fora não faz nada.",
        ),
        TutorialPasso(
            titulo = "Menu",
            texto = "Histórico, Dashboard, Despesas, Semáforo, Veiculo, Configurar e Fechar. A seta das abas sempre volta ao atalho.",
        ),
        TutorialPasso(
            titulo = "Notificação",
            texto = "Resumo da oferta. Expirar ou recusar limpa e volta a Monitorando ofertas. Aceite mantém o resumo até a próxima oferta. Abrir App / Desligar App.",
        ),
        TutorialPasso(
            titulo = "Config",
            texto = "Abas em formato de ficheiro: Histórico, Dashboard, Despesas, Semáforo, Veiculo e Configurar. Cancelar descarta. Salvar grava.",
        ),
        TutorialPasso(
            titulo = "Histórico",
            texto = "Só entra corrida aceita. Filtro Todos/Uber/99/inDrive. Semana (DOM–SÁB) com setas. Card: ganhos, $/Km, lucro, gasto e nota.",
        ),
    )
}
