package com.example.sbooks.models
data class SearchFilter(
    val query: String = "",
    val categoryId: Int? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val authorFilter: String = "",
    val publisherFilter: String = "",
    val sortBy: SortOption = SortOption.NAME_ASC,
    val showLowStockOnly: Boolean = false,
    val showOutOfStockOnly: Boolean = false
) {
    enum class SortOption(val value: String, val displayName: String) {
        NAME_ASC("name_asc", "Tên A-Z"),
        NAME_DESC("name_desc", "Tên Z-A"),
        PRICE_ASC("price_asc", "Giá thấp đến cao"),
        PRICE_DESC("price_desc", "Giá cao đến thấp"),
        STOCK_ASC("stock_asc", "Tồn kho thấp đến cao"),
        DATE_DESC("date_desc", "Mới nhất");

        companion object {
            fun fromValue(value: String): SortOption {
                return values().find { it.value == value } ?: NAME_ASC
            }
        }
    }

    fun hasActiveFilters(): Boolean {
        return query.isNotEmpty() || categoryId != null || minPrice != null ||
                maxPrice != null || authorFilter.isNotEmpty() || publisherFilter.isNotEmpty() ||
                showLowStockOnly || showOutOfStockOnly
    }
}