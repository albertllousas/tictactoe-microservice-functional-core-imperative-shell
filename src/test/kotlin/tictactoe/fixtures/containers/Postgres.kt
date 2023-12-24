package bed.mgmt.fixtures.containers

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

class Postgres(network: Network? = null) {

    val container: KtPostgreSQLContainer = KtPostgreSQLContainer()
        .withNetwork(network?: Network.newNetwork())
        .withNetworkAliases("localhost")
        .withUsername("tictactoe")
        .withPassword("tictactoe")
        .withDatabaseName("tictactoe")
        .also {
            it.start()
        }

    val datasource: DataSource = HikariDataSource().apply {
        driverClassName = org.postgresql.Driver::class.qualifiedName
        jdbcUrl = container.jdbcUrl
        username = container.username
        password = container.password
    }.also { Flyway(FluentConfiguration().dataSource(it.jdbcUrl, it.username, it.password)).migrate() }
}

class KtPostgreSQLContainer : PostgreSQLContainer<KtPostgreSQLContainer>("postgres:latest")
