import org.slf4j.LoggerFactory
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.sql.DataSource

enum class DataSourceKey {
    MASTER,
    REPLICA
}

class RoutingDataSource(private val replicaDataSource: DataSource?) : AbstractRoutingDataSource() {

    companion object {
        private val log = LoggerFactory.getLogger(RoutingDataSource::class.java)
    }

    override fun determineCurrentLookupKey(): Any? {
        // 트랜잭션이 읽기 전용인지 확인
        val isTransactionReadOnly: Boolean = TransactionSynchronizationManager.isCurrentTransactionReadOnly()

        if (isTransactionReadOnly) {
            // replicaDataSource가 설정되어 있다면, 간단한 쿼리로 연결 가능 여부 확인
            if (replicaDataSource != null) {
                try {
                    replicaDataSource.connection.use { conn ->
                        conn.createStatement().executeQuery("SELECT 1")
                    }
                    log.info("Replica is available. Routing to Replica server.")
                    return DataSourceKey.REPLICA
                } catch (ex: Exception) {
                    log.warn("Replica is not available ({}). Fallback to Master server.", ex.message)
                    return DataSourceKey.MASTER
                }
            } else {
                log.info("Replica DataSource not configured. Routing to Master server.")
                return DataSourceKey.MASTER
            }
        }

        log.info("Routing to Master server.")
        return DataSourceKey.MASTER
    }
}
