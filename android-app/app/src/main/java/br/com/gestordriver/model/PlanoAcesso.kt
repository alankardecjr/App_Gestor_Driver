package br.com.gestordriver.model

enum class PlanoAcesso {
    FREE,
    BETA,
    PRO,
    ;

    val ehPro: Boolean
        get() = this != FREE

    val travaCalculadora: Boolean
        get() = !ehPro
}
