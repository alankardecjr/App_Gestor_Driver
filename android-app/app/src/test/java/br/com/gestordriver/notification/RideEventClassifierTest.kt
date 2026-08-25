package br.com.gestordriver.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class RideEventClassifierTest {
    private fun ofertaUber() = NotificationData(
        packageName = "com.ubercab.driver",
        title = "Nova viagem disponivel",
        text = "R$ 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min",
    )

    @Test
    fun notificacao_parseavel_e_sempre_oferta() {
        val evento = RideEventClassifier.classificar(ofertaUber(), ofertaParseavel = true)
        assertEquals(TipoEventoCorrida.NOVA_OFERTA, evento)
    }

    @Test
    fun aceite_so_com_assinatura_explicita_sem_metricas() {
        val notification = NotificationData(
            packageName = "com.ubercab.driver",
            title = "Viagem aceita",
            text = "Dirija ate o passageiro",
        )
        val evento = RideEventClassifier.classificar(notification, ofertaParseavel = false)
        assertEquals(TipoEventoCorrida.ACEITE_DETECTADO, evento)
    }

    @Test
    fun notificacao_desconhecida_nunca_e_aceite() {
        val notification = NotificationData(
            packageName = "com.ubercab.driver",
            title = "Promocao",
            text = "Ganhe pontos hoje",
        )
        val evento = RideEventClassifier.classificar(notification, ofertaParseavel = false)
        assertEquals(TipoEventoCorrida.IGNORADO, evento)
    }

    @Test
    fun oferta_atualizada_com_aceite_vira_aceite_se_ja_ha_sessao() {
        val notification = NotificationData(
            packageName = "com.ubercab.driver",
            title = "R$ 38,00 • 3,2 km • 12,8 km • 24 min",
            text = "Dirija ate o passageiro",
        )
        val evento = RideEventClassifier.classificar(
            notification = notification,
            ofertaParseavel = true,
            ofertaEmAndamento = true,
        )
        assertEquals(TipoEventoCorrida.ACEITE_DETECTADO, evento)
    }

    @Test
    fun primeira_notificacao_com_aceite_mostra_oferta_e_grava() {
        val notification = NotificationData(
            packageName = "com.ubercab.driver",
            title = "R$ 38,00 • 3,2 km • 12,8 km • 24 min",
            text = "Dirija ate o passageiro",
        )
        val evento = RideEventClassifier.classificar(
            notification = notification,
            ofertaParseavel = true,
            ofertaEmAndamento = false,
        )
        assertEquals(TipoEventoCorrida.OFERTA_E_ACEITE, evento)
    }
}
