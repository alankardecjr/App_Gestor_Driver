package br.com.gestordriver.overlay

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BarraSistemaTest {
    @Test
    fun home_samsung_recolhe() {
        assertTrue(
            BarraSistema.deveRecolherParaSelo(
                pacote = "com.sec.android.app.launcher",
                classe = "Launcher",
                tipoJanelaAlterada = true,
            ),
        )
    }

    @Test
    fun recents_systemui_recolhe() {
        assertTrue(
            BarraSistema.deveRecolherParaSelo(
                pacote = "com.android.systemui",
                classe = "com.android.systemui.recents.RecentsActivity",
                tipoJanelaAlterada = true,
            ),
        )
    }

    @Test
    fun 99_nao_recolhe() {
        assertFalse(
            BarraSistema.deveRecolherParaSelo(
                pacote = "com.app99.driver",
                classe = "HomePageActivity",
                tipoJanelaAlterada = true,
            ),
        )
    }

    @Test
    fun conteudo_da_janela_nao_recolhe() {
        assertFalse(
            BarraSistema.deveRecolherParaSelo(
                pacote = "com.sec.android.app.launcher",
                classe = "Launcher",
                tipoJanelaAlterada = false,
            ),
        )
    }
}
