package ru.kuzmin.batchupdate.strategy

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.transactions.transaction
import ru.kuzmin.BatchUpdateTable

class ExposedBatchUpdateStrategy(
    private val database: Database,
) : BatchUpdateStrategy {
    override fun update(
        records: List<Pair<Long, String>>,
        shouldReturnGeneratedValues: Boolean,
    ) {
        transaction(database) {
            val statement = BatchUpdateStatement(BatchUpdateTable)
            records.forEach {
                statement.addBatch(EntityID(it.first, BatchUpdateTable))
                statement[BatchUpdateTable.data] = it.second
            }
            statement.execute(this)
        }
    }

    override fun getType() = BatchUpdateStrategyType.EXPOSED
}
