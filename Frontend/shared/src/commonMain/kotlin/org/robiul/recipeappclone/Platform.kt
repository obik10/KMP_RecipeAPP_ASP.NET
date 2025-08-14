package org.robiul.recipeappclone

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform