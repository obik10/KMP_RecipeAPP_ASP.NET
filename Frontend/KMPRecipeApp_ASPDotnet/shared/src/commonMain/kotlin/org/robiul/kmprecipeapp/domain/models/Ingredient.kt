package org.robiul.kmprecipeapp.domain.models

import io.ktor.util.date.getTimeMillis
import kotlin.random.Random

data class Ingredient(
    val id: String = "${getTimeMillis()}-${Random.nextInt(0, 9999)}",
    val name: String = "",
    val measure: String = ""
)
