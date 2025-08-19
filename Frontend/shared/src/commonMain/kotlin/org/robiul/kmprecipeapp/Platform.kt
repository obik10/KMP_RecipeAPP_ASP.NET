package org.robiul.kmprecipeapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform