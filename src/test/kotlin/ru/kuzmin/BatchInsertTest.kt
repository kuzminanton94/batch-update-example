package ru.kuzmin

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import ru.kuzmin.batchupdate.BatchUpdateService
import ru.kuzmin.utils.Timer
import java.text.DecimalFormat
import java.util.Random
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class BatchInsertTest {

    @ParameterizedTest
    @CsvSource(
        textBlock = """
        false, true
        false, false
        true, true
        true, false
        """,
    )
    fun performance_test(
        reWriteBatchedInserts: Boolean,
        shouldReturnGeneratedValues: Boolean,
    ) {
        val insertRecordsCount = 300_000

        val measurements = mutableListOf<Double>()
        for (i in 1..5) {
            createPostgres().use {
                it.start()

                val database = createDatabase(it, reWriteBatchedInserts)
                val batchUpdateService = BatchUpdateService(database)

                createMainTable(database)

                val records = createTestData(insertRecordsCount)

                val insertDuration = Timer.withTimer { batchUpdateService.insert(records, shouldReturnGeneratedValues) }

                measurements.add(calculateRps(insertRecordsCount, insertDuration))
            }
        }

        println(
            """
                |records=$insertRecordsCount
                |reWriteBatchedInserts=$reWriteBatchedInserts
                |shouldReturnGeneratedValues=$shouldReturnGeneratedValues
                |insert=${formatRps(measurements.average())} rps
            """.trimMargin(),
        )
    }

    private fun createPostgres(): PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageName.parse("postgres"))
            .withUsername("flyway")
            .withPassword("flyway")
            .withDatabaseName("flyway_demo")
            .waitingFor(Wait.forListeningPort())

    private fun createDatabase(
        container: PostgreSQLContainer<*>,
        reWriteBatchedInserts: Boolean,
    ) = Database.connect(
        HikariDataSource().also {
            it.jdbcUrl = container.jdbcUrl
            it.username = container.username
            it.password = container.password
            it.dataSourceProperties.setProperty("prepareThreshold", "0")
            it.dataSourceProperties.setProperty("preparedStatementCacheQueries", "0")
            it.dataSourceProperties.setProperty("reWriteBatchedInserts", reWriteBatchedInserts.toString())
        },
    )

    private fun createMainTable(database: Database) {
        transaction(database) {
            exec(
                """
                create table if not exists batch_update_table
                (
                    id   bigint primary key,
                    data text      not null
                );
                """.trimIndent(),
            )
        }
    }

    private fun createTestData(size: Int): List<Pair<Long, String>> {
        val random = Random()
        val recordsIds = mutableSetOf<Long>()
        while (recordsIds.size < size) {
            recordsIds.add(random.nextLong())
        }
        val recordsIdsList = recordsIds.toList()

        val testData = mutableListOf<Pair<Long, String>>()
        for (i in 0 until size) {
            testData.add(recordsIdsList[i] to UUID.randomUUID().toString())
        }
        return testData
    }

    private fun calculateRps(
        count: Int,
        duration: Duration,
    ) = count.toDouble() / (duration.inWholeMilliseconds.toDouble() / 1.seconds.inWholeMilliseconds)

    private fun formatRps(
        value: Double,
    ) = DecimalFormat("#.00").format(value)
}
