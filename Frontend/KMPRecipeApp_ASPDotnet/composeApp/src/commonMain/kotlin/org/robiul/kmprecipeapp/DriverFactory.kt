package org.robiul.kmprecipeapp
import app.cash.sqldelight.db.SqlDriver


expect class DriverFactory {
    fun createDriver(): SqlDriver
}
