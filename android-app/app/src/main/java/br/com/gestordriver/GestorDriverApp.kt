package br.com.gestordriver

import android.app.Application
import androidx.room.Room
import br.com.gestordriver.data.GestorDatabase
import br.com.gestordriver.data.HistoricoRepository
import br.com.gestordriver.data.ConfiguracaoStore
import br.com.gestordriver.data.OnboardingStore
import br.com.gestordriver.data.PreferencesConfiguracaoStore
import br.com.gestordriver.data.PreferencesOnboardingStore
import br.com.gestordriver.data.RoomHistoricoRepository
import br.com.gestordriver.notification.NotificationDiagnosticLog

class GestorDriverApp : Application() {
    lateinit var historicoRepository: HistoricoRepository
        private set

    lateinit var configuracaoStore: ConfiguracaoStore
        private set

    lateinit var diagnosticLog: NotificationDiagnosticLog
        private set

    lateinit var onboardingStore: OnboardingStore
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            this,
            GestorDatabase::class.java,
            "gestor-driver.db",
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .allowMainThreadQueries()
            .build()
        historicoRepository = RoomHistoricoRepository(database.historicoDao())
        configuracaoStore = PreferencesConfiguracaoStore(this)
        onboardingStore = PreferencesOnboardingStore(this)
        diagnosticLog = NotificationDiagnosticLog(this)
        val anterior = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, erro ->
            diagnosticLog.registrarCrash(erro)
            anterior?.uncaughtException(thread, erro)
        }
    }
}
