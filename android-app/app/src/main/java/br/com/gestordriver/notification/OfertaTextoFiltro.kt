package br.com.gestordriver.notification

/**
 * Separa status/promoção de oferta real. 99 e Uber quase nunca
 * colocam valor/km na barra de notificação; o card da corrida fica na tela.
 */
object OfertaTextoFiltro {
    private val promocao = listOf(
        "99abastece",
        "abastece",
        "encher o tanque",
        "missão",
        "missao",
        "missões",
        "missoes",
        "seguro renda",
        "pontos da fase",
        "taxa de finalização",
        "taxa de finalizacao",
        "novas áreas de alta demanda",
        "novas areas de alta demanda",
        "preço dinâmico",
        "preco dinamico",
        "fique online",
        "alerta de ganhos",
        "ganhos mais altos",
    )

    private val status = listOf(
        "você está conectado",
        "voce esta conectado",
        "você está online",
        "voce esta online",
        "você está offline",
        "voce esta offline",
        "desconectado",
        "conectando na",
        "conectando...",
        "continuar conectado",
        "política de cancelamento",
        "politica de cancelamento",
    )

    private val oferta = listOf(
        "solicitação encontrada",
        "solicitacao encontrada",
        "nova corrida",
        "nova viagem",
        "nova solicitação",
        "nova solicitacao",
        "corrida disponível",
        "corrida disponivel",
        "viagem disponível",
        "viagem disponivel",
        "aceitar corrida",
        "aceitar viagem",
        "deslize para aceitar",
        "deslizar para aceitar",
        "arraste para aceitar",
        "até o passageiro",
        "ate o passageiro",
        "até você",
        "km até",
        "km ate",
        "request found",
        "new trip",
        "new ride",
    )

    private val interfaceGestor = listOf(
        "histórico",
        "historico",
        "r$/km",
        "custos estimado",
        "total percorrido",
        "data | hora",
        "lucro estimado",
        "gasto estimado",
        "até o destino",
        "ate o destino",
        "nenhuma corrida aceita",
    )

    private val telaHome = listOf(
        "você está conectado",
        "voce esta conectado",
        "você está online",
        "voce esta online",
        "você está offline",
        "voce esta offline",
        "página inicial",
        "pagina inicial",
        "alta demanda aqui",
    )

    fun ehInterfaceGestor(texto: String): Boolean {
        val normal = normalizar(texto)
        if (normal.isBlank()) {
            return false
        }
        val marcas = interfaceGestor.count { normal.contains(it) }
        return marcas >= 2 || normal.contains("nenhuma corrida aceita")
    }

    fun ehMapaSemCard(texto: String): Boolean {
        if (NotificationPatterns.TRECHO_TEMPO_DISTANCIA.containsMatchIn(texto)) {
            return false
        }
        val normal = normalizar(texto)
        if (normal.contains("aceitar") || Regex("(?m)^\\s*selecionar\\s*$").containsMatchIn(texto)) {
            return false
        }
        return telaHome.any { normal.contains(it) }
    }

    fun cardKmIncompleto(texto: String): Boolean =
        NotificationPatterns.TRECHO_TEMPO_DISTANCIA.findAll(texto).count() < 2

    fun pareceTelaAposAceiteUber(texto: String): Boolean {
        val normal = normalizar(texto)
        if (normal.contains("aceitar") || ehMapaSemCard(texto) || ehInterfaceGestor(texto)) {
            return false
        }
        val produto = listOf("uberx", "uber x", "comfort", "uber black", "priority", "uberxl")
        if (produto.none { normal.contains(it) }) {
            return false
        }
        if (NotificationPatterns.TRECHO_TEMPO_DISTANCIA.containsMatchIn(texto)) {
            return false
        }
        val posAceite = listOf(
            "aceitei por engano",
            "local de partida",
            "cancelar a viagem",
            "continuar viagem",
            "encontro com",
            "deslize para cancelar",
        )
        if (posAceite.any { normal.contains(it) }) {
            return true
        }
        val temNota = NotificationPatterns.NOTA_COM_PARENTESES.containsMatchIn(texto) ||
            NotificationPatterns.NOTA_LINHA_ISOLADA.containsMatchIn(texto)
        return temNota && !NotificationPatterns.VALOR.containsMatchIn(texto)
    }

    fun ehPromocaoOuStatus(texto: String): Boolean {
        val normal = normalizar(texto)
        if (normal.isBlank()) {
            return true
        }
        if (ehInterfaceGestor(texto)) {
            return true
        }
        if (promocao.any { normal.contains(it) }) {
            return true
        }
        if (pareceOferta(texto)) {
            return false
        }
        return status.any { normal.contains(it) }
    }

    fun pareceOferta(texto: String): Boolean {
        if (ehInterfaceGestor(texto)) {
            return false
        }
        val normal = normalizar(texto)
        if (promocao.any { normal.contains(it) }) {
            return false
        }
        if (ehMapaSemCard(texto)) {
            return false
        }
        if (oferta.any { normal.contains(it) }) {
            return true
        }
        val temAceitar = normal.contains("aceitar")
        val temRecusar = normal.contains("recusar") || normal.contains("rejeitar")
        val temValor = NotificationPatterns.VALOR.containsMatchIn(texto)
        val distancias = NotificationPatterns.DISTANCIA_COM_UNIDADE.findAll(texto).count()
        val temTempo = NotificationPatterns.TEMPO.containsMatchIn(texto)
        if (temAceitar && (temValor || distancias >= 1 || temTempo || temRecusar)) {
            return true
        }
        return temValor && (distancias >= 1 || temTempo)
    }

    fun temDadosParseaveis(texto: String): Boolean {
        if (ehInterfaceGestor(texto) || ehMapaSemCard(texto)) {
            return false
        }
        if (!NotificationPatterns.VALOR.containsMatchIn(texto)) {
            return false
        }
        return NotificationPatterns.TRECHO_TEMPO_DISTANCIA.containsMatchIn(texto) ||
            NotificationPatterns.PAR_TEMPO_DISTANCIA.containsMatchIn(texto) ||
            NotificationPatterns.DISTANCIA_COM_UNIDADE.containsMatchIn(texto) ||
            NotificationPatterns.TEMPO.containsMatchIn(texto)
    }

    fun normalizar(texto: String): String =
        texto.lowercase()
            .replace('\u00a0', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
}
