package br.com.gestordriver.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.core.Classificacao
import br.com.gestordriver.core.Corrida
import br.com.gestordriver.model.ClassificacaoVisual
import br.com.gestordriver.model.HistoricoItemPresentation
import java.time.LocalDateTime

@Entity(tableName = "historico_corridas_aceitas")
data class HistoricoCorridaEntity(
    @PrimaryKey val chave: String,
    val dataHora: String,
    val plataforma: String,
    val valorPorKm: Double,
    val valorTotal: Double,
    val kmTotal: Double,
    val tempoEstimado: Int?,
    val notaPassageiro: Double?,
    val classificacao: String,
    val corClassificacao: String,
    val kmAtePassageiro: Double,
    val kmViagem: Double,
    val combustivelEstimado: Double?,
    val custoCombustivel: Double?,
    val dataHoraRegistro: String?,
    val enderecoEmbarque: String?,
    val enderecoDestino: String?,
)

@Dao
interface HistoricoDao {
    @Query("SELECT * FROM historico_corridas_aceitas")
    fun listar(): List<HistoricoCorridaEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun inserir(entity: HistoricoCorridaEntity)

    @Query("DELETE FROM historico_corridas_aceitas WHERE chave IN (:chaves)")
    fun remover(chaves: List<String>)

    @Query("DELETE FROM historico_corridas_aceitas")
    fun limpar()
}

@androidx.room.Database(
    entities = [HistoricoCorridaEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class GestorDatabase : RoomDatabase() {
    abstract fun historicoDao(): HistoricoDao
}

class RoomHistoricoRepository(
    private val dao: HistoricoDao,
) : HistoricoRepository {
    override fun listar(): List<HistoricoItemPresentation> =
        dao.listar().map { it.paraPresentation() }

    override fun salvar(item: HistoricoItemPresentation) {
        dao.inserir(item.paraEntity())
    }

    override fun remover(chaves: Collection<String>) {
        if (chaves.isEmpty()) {
            return
        }
        dao.remover(chaves.distinct())
    }

    override fun limpar() {
        dao.limpar()
    }
}

fun HistoricoItemPresentation.paraEntity(): HistoricoCorridaEntity =
    HistoricoCorridaEntity(
        chave = chaveHistorico(),
        dataHora = dataHora,
        plataforma = plataforma,
        valorPorKm = valorPorKm,
        valorTotal = valorTotal,
        kmTotal = kmTotal,
        tempoEstimado = tempoEstimado,
        notaPassageiro = notaPassageiro,
        classificacao = classificacao.name,
        corClassificacao = corClassificacao,
        kmAtePassageiro = kmAtePassageiro,
        kmViagem = kmViagem,
        combustivelEstimado = combustivelEstimado,
        custoCombustivel = custoCombustivel,
        dataHoraRegistro = dataHoraRegistro?.toString(),
        enderecoEmbarque = enderecoEmbarque,
        enderecoDestino = enderecoDestino,
    )

fun HistoricoCorridaEntity.paraPresentation(): HistoricoItemPresentation =
    HistoricoItemPresentation(
        dataHora = dataHora,
        plataforma = plataforma,
        valorPorKm = valorPorKm,
        valorTotal = valorTotal,
        kmTotal = kmTotal,
        tempoEstimado = tempoEstimado,
        notaPassageiro = notaPassageiro,
        classificacao = ClassificacaoVisual.valueOf(classificacao),
        corClassificacao = corClassificacao,
        kmAtePassageiro = kmAtePassageiro,
        kmViagem = kmViagem,
        combustivelEstimado = combustivelEstimado,
        custoCombustivel = custoCombustivel,
        dataHoraRegistro = dataHoraRegistro?.let { LocalDateTime.parse(it) },
        enderecoEmbarque = enderecoEmbarque,
        enderecoDestino = enderecoDestino,
    )
