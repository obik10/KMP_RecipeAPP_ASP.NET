package org.robiul.kmprecipeapp.domain.models

data class PaginatedResult<T>(
    val items: List<T>,
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int,
    val totalPages: Int? = null
) {
    val hasNextPage: Boolean
        get() = totalPages?.let { pageNumber < it } ?: (items.size == pageSize)
}
