package ru.kuzmin.batchupdate

import ru.kuzmin.BatchUpdateTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import ru.kuzmin.batchupdate.strategy.BatchUpdateStrategy
import ru.kuzmin.batchupdate.strategy.BatchUpdateStrategyType
import ru.kuzmin.batchupdate.strategy.ExposedBatchUpdateStrategy
import ru.kuzmin.batchupdate.strategy.OneTransactionBatchUpdateStrategy
import ru.kuzmin.batchupdate.strategy.SeparateTransactionBatchUpdateStrategy
import ru.kuzmin.batchupdate.strategy.TempTableBatchUpdateStrategy

class BatchUpdateService(
    private val database: Database,
) {

    private val strategies: Map<BatchUpdateStrategyType, BatchUpdateStrategy> = listOf(
        OneTransactionBatchUpdateStrategy(database),
        SeparateTransactionBatchUpdateStrategy(database),
        ExposedBatchUpdateStrategy(database),
        TempTableBatchUpdateStrategy(database),
    ).associateBy { it.getType() }

    fun insert(records: List<Pair<Long, String>>) {
        transaction(database) {
            BatchUpdateTable.batchInsert(records) {
                this[BatchUpdateTable.id] = it.first
                this[BatchUpdateTable.data] = it.second
            }
        }
    }

    fun update(
        strategy: BatchUpdateStrategyType,
        records: List<Pair<Long, String>>,
    ) {
        strategies[strategy]!!.update(records)
    }
}
