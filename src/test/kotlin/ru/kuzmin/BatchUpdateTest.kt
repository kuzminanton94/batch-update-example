package ru.kuzmin

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import ru.kuzmin.batchupdate.BatchUpdateService
import ru.kuzmin.batchupdate.strategy.BatchUpdateStrategyType
import ru.kuzmin.utils.Timer.withTimer
import java.util.Random
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.Duration

class BatchUpdateTest {

    @Test
    fun unit_test() {
        val totalRecordsCount = 10
        val updateRecordsCount = 5

        for (updateStrategy in BatchUpdateStrategyType.values()) {
            startPostgres().use {
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

    @Test
    fun performance_test() {
        val totalRecordsCount = 200000
        val updateRecordsCount = 10000

        val stats = mutableMapOf<BatchUpdateStrategyType, Pair<Duration, Duration>>()
        for (updateStrategy in BatchUpdateStrategyType.values()) {
            startPostgres().use {
                val database = createDatabase(it)
                val batchUpdateService = BatchUpdateService(database)

                createMainTable(database)

                val records = createTestData(totalRecordsCount)
                val recordsToUpdate = createTestDataToUpdate(updateRecordsCount, records)

                val insertDuration = withTimer { batchUpdateService.insert(records) }
                val updateDuration = withTimer { batchUpdateService.update(updateStrategy, recordsToUpdate) }
                stats[updateStrategy] = insertDuration to updateDuration
            }
        }

        println("totalSize=$totalRecordsCount, updateSize=$updateRecordsCount")
        for (entry in stats.entries) {
            println("strategy=${entry.key}, insert=${entry.value.first}, update=${entry.value.second}")
        }
    }

    private fun startPostgres(): PostgreSQLContainer<*> {
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:alpine"))
            .withUsername("flyway")
            .withPassword("flyway")
            .withDatabaseName("flyway_demo")
        postgres.start()

        val database = Database.connect(url = postgres.jdbcUrl, user = postgres.username, password = postgres.password)

        var started = false
        while (!started) {
            try {
                transaction(database) { exec("select 1;") }
                started = true
            } catch (_: Exception) {
                Thread.sleep(1000)
            }
        }

        return postgres
    }

    private fun createDatabase(container: PostgreSQLContainer<*>) = Database.connect(
        HikariDataSource().also {
            it.jdbcUrl = container.jdbcUrl
            it.username = container.username
            it.password = container.password
            it.dataSourceProperties.setProperty("prepareThreshold", "0")
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
}
