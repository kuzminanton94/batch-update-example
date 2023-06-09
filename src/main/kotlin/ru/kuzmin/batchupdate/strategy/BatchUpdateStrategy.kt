package ru.kuzmin.batchupdate.strategy

interface BatchUpdateStrategy {

    fun update(records: List<Pair<Long, String>>)

    fun getType(): BatchUpdateStrategyType
}
