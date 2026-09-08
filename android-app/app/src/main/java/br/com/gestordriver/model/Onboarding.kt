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
            texto = "Card no topo só na oferta: R$/Km, Dist., Tempo e Nota. Arrastável (guarda posição). Borda na cor da classificação. Toque no corpo ou fora não faz nada; X fecha a compacta e retorna ao selo.",
        ),
        TutorialPasso(
            titulo = "Menu / Atalhos",
            texto = "Toque no selo abre Atalhos (selo some). X ou toque fora fecha e devolve o selo. Itens: Histórico | Carteira | Despesas | Semáforo | Usuário | Configurar | Fechar.",
        ),
        TutorialPasso(
            titulo = "Notificação",
            texto = "Resumo da oferta. Expirar ou recusar limpa e volta a Monitorando ofertas. Aceite mantém o resumo até a próxima oferta. Abrir App / Desligar App.",
        ),
        TutorialPasso(
            titulo = "Configurar",
            texto = "Atalho Configurar abre ajustes do app (permissões, tema, conta). Despesas e Usuário abrem Custos e Veículo. Cancelar descarta. Salvar grava.",
        ),
        TutorialPasso(
            titulo = "Histórico",
            texto = "Só entra corrida aceita. Filtro Todos/Uber/99/inDrive. Semana (DOM–SÁB) com setas. Card: ganhos, $/Km, lucro, gasto e nota.",
        ),
    )
}
