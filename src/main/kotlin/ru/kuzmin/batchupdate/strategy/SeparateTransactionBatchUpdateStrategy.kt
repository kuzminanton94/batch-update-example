package ru.kuzmin.batchupdate.strategy

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.kuzmin.BatchUpdateTable

class SeparateTransactionBatchUpdateStrategy(
    private val database: Database,
) : BatchUpdateStrategy {
    override fun update(records: List<Pair<Long, String>>) {
        records.forEach {
            transaction(database) {
                BatchUpdateTable.update({ BatchUpdateTable.id eq it.first }) { statement ->
                    statement[data] = it.second
                }
            }
        }
    }

    override fun getType() = BatchUpdateStrategyType.SEPARATE_TRANSACTION
}
