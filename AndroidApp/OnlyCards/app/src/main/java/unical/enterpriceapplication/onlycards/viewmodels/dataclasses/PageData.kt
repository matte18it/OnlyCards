package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

data class PageData<T>(
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val content: List<T>,
    val error :Boolean = false,
)