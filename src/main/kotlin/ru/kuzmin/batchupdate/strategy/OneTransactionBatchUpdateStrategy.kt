package ru.kuzmin.batchupdate.strategy

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.kuzmin.BatchUpdateTable

class OneTransactionBatchUpdateStrategy(
    private val database: Database,
) : BatchUpdateStrategy {
    override fun update(records: List<Pair<Long, String>>,
                        shouldReturnGeneratedValues: Boolean) {
        transaction(database) {
            records.forEach {
                BatchUpdateTable.update({ BatchUpdateTable.id eq it.first }) { statement ->
                    statement[data] = it.second
                }
            }
        }
    }

    override fun getType() = BatchUpdateStrategyType.ONE_TRANSACTION
}
