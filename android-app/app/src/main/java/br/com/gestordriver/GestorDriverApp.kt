package br.com.gestordriver

import android.app.Application
import androidx.room.Room
import br.com.gestordriver.data.GestorDatabase
import br.com.gestordriver.data.HistoricoRepository
import br.com.gestordriver.data.ConfiguracaoStore
import br.com.gestordriver.data.PreferencesConfiguracaoStore
import br.com.gestordriver.data.RoomHistoricoRepository

class GestorDriverApp : Application() {
    lateinit var historicoRepository: HistoricoRepository
        private set

    lateinit var configuracaoStore: ConfiguracaoStore
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            this,
            GestorDatabase::class.java,
            "gestor-driver.db",
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
        historicoRepository = RoomHistoricoRepository(database.historicoDao())
        configuracaoStore = PreferencesConfiguracaoStore(this)
    }
}
