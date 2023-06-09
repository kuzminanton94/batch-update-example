package ru.kuzmin

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object BatchUpdateTable : IdTable<Long>("batch_update_table") {
    override val id: Column<EntityID<Long>> = long("id").entityId()
    val data: Column<String> = text("data")
}
