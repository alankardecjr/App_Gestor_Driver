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
            titulo = "O que o app faz",
            texto = "O Gestor Driver lê a oferta da Uber, da 99 e da inDrive e mostra se ela vale a pena antes de você aceitar. Ele compara o valor com o seu custo e pinta a borda de vermelho, amarelo ou verde.",
        ),
        TutorialPasso(
            titulo = "Selo",
            texto = "Com o monitoramento ligado, um selo redondo fica sobre o mapa. Ele some enquanto o Gestor está aberto e volta quando você sai para o mapa. O toque abre só os atalhos. Arrastar o selo até a lixeira esconde o selo; o aviso da barra continua.",
        ),
        TutorialPasso(
            titulo = "Oferta",
            texto = "Quando chega uma corrida, um cartão compacto aparece com R$/km, R$/hora, tempo e nota. A borda usa a pior cor entre km e hora. O X descarta aquela oferta. O cartão some sozinho quando a oferta expira.",
        ),
        TutorialPasso(
            titulo = "Menu",
            texto = "O ícone do app abre o menu em Opções. Histórico, Dashboard, Despesas, Semáforo, Usuário e Sistema abrem dentro do Gestor. A seta volta para Opções. Fechar pede confirmação e desliga o monitoramento.",
        ),
        TutorialPasso(
            titulo = "Aviso",
            texto = "A notificação fica na barra o tempo todo em que o monitoramento está ligado. Ela resume a oferta. Fechar só o aviso, ou só o selo, não desliga o app. O monitoramento desliga quando o aviso, o selo e o menu são fechados juntos.",
        ),
        TutorialPasso(
            titulo = "Semáforo e histórico",
            texto = "No Semáforo você marca o que é ruim, bom e ótimo em R$/km, R$/hora e nota. Só a corrida aceita entra no Histórico, no dia em que você aceitou. O Dashboard junta o dinheiro do dia. Monitorar, em Opções, liga e desliga tudo isso.",
        ),
    )
}
