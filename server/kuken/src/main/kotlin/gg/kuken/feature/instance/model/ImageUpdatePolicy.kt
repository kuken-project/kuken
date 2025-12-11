package gg.kuken.feature.instance.model

enum class ImageUpdatePolicy(
    val id: String,
) {
    Always("always"),
    Never("never"),
    ;

    companion object {
        fun getById(id: String): ImageUpdatePolicy = entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: Always
    }
}
