package ru.kuzmin.batchupdate.strategy

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.kuzmin.BatchUpdateTable

class TempTableBatchUpdateStrategy(
    private val database: Database,
) : BatchUpdateStrategy {
    override fun update(
        records: List<Pair<Long, String>>,
        shouldReturnGeneratedValues: Boolean,
    ) {
        transaction(database) {
            execInBatch(
                listOf(
                    """
                    DROP TABLE IF EXISTS ${TempBatchUpdateTable.tableName};
                    """.trimIndent(),
                    """
                    CREATE TEMPORARY TABLE ${TempBatchUpdateTable.tableName} (
                        id   bigint    primary key,
                        data text      not null
                    );
                    """.trimIndent(),
                ),
            )

            TempBatchUpdateTable.batchInsert(
                data = records,
                shouldReturnGeneratedValues = shouldReturnGeneratedValues,
            ) {
                this[TempBatchUpdateTable.id] = it.first
                this[TempBatchUpdateTable.data] = it.second
            }

            BatchUpdateTable
                .innerJoin(
                    otherTable = TempBatchUpdateTable,
                    onColumn = { id },
                    otherColumn = { id },
                )
                .update {
                    it[BatchUpdateTable.data] = TempBatchUpdateTable.data
                }
        }
    }

    override fun getType() = BatchUpdateStrategyType.TEMP_TABLE

    private object TempBatchUpdateTable : IdTable<Long>("temp_batch_update_table") {
        override val id: Column<EntityID<Long>> = long("id").entityId()
        val data: Column<String> = text("data")
    }
}
