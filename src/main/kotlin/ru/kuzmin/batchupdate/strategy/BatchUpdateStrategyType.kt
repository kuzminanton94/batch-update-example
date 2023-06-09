package ru.kuzmin.batchupdate.strategy

enum class BatchUpdateStrategyType {
    SEPARATE_TRANSACTION,

    ONE_TRANSACTION,

    EXPOSED,

    TEMP_TABLE,
}
