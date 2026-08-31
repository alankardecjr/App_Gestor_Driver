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

    @Test
    fun tela_99_ponto_de_encontro_e_aceite() {
        val notification = NotificationData(
            packageName = "com.app99.driver",
            title = "Ponto de encontro",
            text = "Estou no local",
        )
        val evento = RideEventClassifier.classificar(notification, ofertaParseavel = false)
        assertEquals(TipoEventoCorrida.ACEITE_DETECTADO, evento)
    }

    @Test
    fun tela_99_chegue_antes_e_aceite() {
        val notification = NotificationData(
            packageName = "com.app99.driver",
            title = "JARDIM TARUMA",
            text = "9 min 2,3 km\nChegue antes de 06:50",
        )
        val evento = RideEventClassifier.classificar(notification, ofertaParseavel = false)
        assertEquals(TipoEventoCorrida.ACEITE_DETECTADO, evento)
    }

    @Test
    fun tela_99_chegada_prevista_e_aceite() {
        val notification = NotificationData(
            packageName = "com.app99.driver",
            title = "",
            text = "9 min 2,3 km\nChegada prevista: 06:46",
        )
        val evento = RideEventClassifier.classificar(notification, ofertaParseavel = false)
        assertEquals(TipoEventoCorrida.ACEITE_DETECTADO, evento)
    }

    @Test
    fun botao_aceitar_nao_e_aceite() {
        val notification = NotificationData(
            packageName = "com.ubercab.driver",
            title = "Priority",
            text = "R$ 11,74\n5 min (1.2 km)\nAceitar\nSelecionar",
        )
        assertEquals(
            TipoEventoCorrida.NOVA_OFERTA,
            RideEventClassifier.classificar(notification, ofertaParseavel = true),
        )
    }

    @Test
    fun overlay_do_gestor_nao_e_aceite() {
        val notification = NotificationData(
            packageName = "com.app99.driver",
            title = "HISTÓRICO",
            text = "DATA | HORA | R$/KM| VALOR\n31/08 | 10:20 | 1,68 | 8,90\nChegada prevista: 10:31\nTotal percorrido\nCustos estimados",
        )
        assertEquals(
            TipoEventoCorrida.IGNORADO,
            RideEventClassifier.classificar(notification, ofertaParseavel = false),
        )
    }

    @Test
    fun historico_vazio_do_overlay_nao_e_aceite() {
        val notification = NotificationData(
            packageName = "com.ubercab.driver",
            title = "",
            text = "DATA | HORA | Nenhuma corrida aceita",
        )
        assertEquals(
            TipoEventoCorrida.IGNORADO,
            RideEventClassifier.classificar(notification, ofertaParseavel = false),
        )
    }

    @Test
    fun tela_uber_aceitei_por_engano_e_aceite() {
        val notification = NotificationData(
            packageName = "com.ubercab.driver",
            title = "Avenida Brasil",
            text = "4,88\nUberX\nAceitei por engano\nlocal de partida\nQuer cancelar a viagem?",
        )
        assertEquals(
            TipoEventoCorrida.ACEITE_DETECTADO,
            RideEventClassifier.classificar(notification, ofertaParseavel = false),
        )
    }

    @Test
    fun tela_uber_pos_aceite_sem_frase_longa_grava() {
        val notification = NotificationData(
            packageName = "com.ubercab.driver",
            title = "Avenida Brasil",
            text = "Maria\n4,88\nUberX",
        )
        assertEquals(
            TipoEventoCorrida.ACEITE_DETECTADO,
            RideEventClassifier.classificar(notification, ofertaParseavel = false),
        )
    }
}
