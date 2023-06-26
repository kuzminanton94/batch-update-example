package ru.kuzmin.batchupdate.strategy

interface BatchUpdateStrategy {

    fun update(
        records: List<Pair<Long, String>>,
        shouldReturnGeneratedValues: Boolean,
    )

    fun getType(): BatchUpdateStrategyType
}
