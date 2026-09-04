package br.com.gestordriver.model

import br.com.gestordriver.core.AnaliseCorrida

class ControlePlano {

    fun aplicar(
        @Suppress("UNUSED_PARAMETER") analise: AnaliseCorrida,
        plano: PlanoAcesso,
    ): RecursosPlano = RecursosPlano.de(plano)
}
