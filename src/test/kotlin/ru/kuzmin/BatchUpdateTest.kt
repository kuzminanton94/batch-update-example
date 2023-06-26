package ru.kuzmin

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import ru.kuzmin.batchupdate.BatchUpdateService
import ru.kuzmin.batchupdate.strategy.BatchUpdateStrategyType
import ru.kuzmin.utils.Timer.withTimer
import java.text.DecimalFormat
import java.util.Random
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class BatchUpdateTest {

    @Test
    fun unit_test() {
        val totalRecordsCount = 10
        val updateRecordsCount = 5

        for (updateStrategy in BatchUpdateStrategyType.values()) {
            createPostgres().use {
                it.start()

                val database = createDatabase(it)
                val batchUpdateService = BatchUpdateService(database)

                createMainTable(database)

                val records = createTestData(totalRecordsCount)
                val recordsToUpdate = createTestDataToUpdate(updateRecordsCount, records)

                batchUpdateService.insert(records)
                batchUpdateService.update(updateStrategy, recordsToUpdate)

                val updatedRecordsIds = recordsToUpdate.map { record -> record.first }.toSet()
                val notUpdatedRecords = records.filter { record -> !updatedRecordsIds.contains(record.first) }.toList()
                for (record in notUpdatedRecords) {
                    val recordData = transaction(database) {
                        BatchUpdateTable
                            .select { BatchUpdateTable.id eq record.first }
                            .first()[BatchUpdateTable.data]
                    }
                    assertEquals(recordData, record.second)
                }

                for (record in recordsToUpdate) {
                    val recordData = transaction(database) {
                        BatchUpdateTable
                            .select { BatchUpdateTable.id eq record.first }
                            .first()[BatchUpdateTable.data]
                    }
                    assertEquals(recordData, record.second)
                }
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        textBlock = """
        200000, 50000
        """,
    )
    fun performance_test(insertRecordsCount: Int, updateRecordsCount: Int) {
        for (i in 1..2) {
            val rpsStats = mutableMapOf<BatchUpdateStrategyType, Pair<Double, Double>>()
            for (updateStrategy in listOf(BatchUpdateStrategyType.TEMP_TABLE)) {
                createPostgres().use {
                    it.start()

                    val database = createDatabase(it)
                    val batchUpdateService = BatchUpdateService(database)

                    createMainTable(database)

                    val records = createTestData(insertRecordsCount)
                    val recordsToUpdate = createTestDataToUpdate(updateRecordsCount, records)

                    val insertDuration = withTimer { batchUpdateService.insert(records) }
                    val updateDuration = withTimer { batchUpdateService.update(updateStrategy, recordsToUpdate) }

                    val insertRps = calculateRps(insertRecordsCount, insertDuration)
                    val updateRps = calculateRps(updateRecordsCount, updateDuration)
                    rpsStats[updateStrategy] = insertRps to updateRps
                }
            }

            println(
                """
                |total records=$insertRecordsCount
                |updated records=$updateRecordsCount
                """.trimMargin(),
            )
            for (entry in rpsStats.entries) {
                println(
                    """
                    |${entry.key}:
                    |  insert=${formatRps(entry.value.first)} rps
                    |  update=${formatRps(entry.value.second)} rps
                    """.trimMargin(),
                )
            }
        }
    }

    private fun createPostgres(): PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageName.parse("postgres"))
            .withUsername("flyway")
            .withPassword("flyway")
            .withDatabaseName("flyway_demo")
            .waitingFor(Wait.forListeningPort())

    private fun createDatabase(container: PostgreSQLContainer<*>) = Database.connect(
        HikariDataSource().also {
            it.jdbcUrl = container.jdbcUrl
            it.username = container.username
            it.password = container.password
            it.dataSourceProperties.setProperty("prepareThreshold", "0")
            it.dataSourceProperties.setProperty("preparedStatementCacheQueries", "0")
            it.dataSourceProperties.setProperty("reWriteBatchedInserts", "true")
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
                
                create index batch_update_table_data_idx on batch_update_table(data);
                create index batch_update_table_id_data_idx on batch_update_table(id, data);
                create index batch_update_table_data_id_idx on batch_update_table(data, id);
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

    private fun createTestDataToUpdate(size: Int, testData: List<Pair<Long, String>>): List<Pair<Long, String>> {
        val recordsToUpdate = mutableListOf<Pair<Long, String>>()
        for (i in 0 until size) {
            recordsToUpdate.add(testData[i].first to UUID.randomUUID().toString())
        }
        return recordsToUpdate
    }

    private fun calculateRps(
        count: Int,
        duration: Duration,
    ) = count.toDouble() / (duration.inWholeMilliseconds.toDouble() / 1.seconds.inWholeMilliseconds)

    private fun formatRps(
        value: Double,
    ) = DecimalFormat("#.00").format(value)
}
