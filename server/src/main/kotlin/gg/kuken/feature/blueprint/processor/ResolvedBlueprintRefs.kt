package gg.kuken.feature.blueprint.processor

enum class ResolvedBlueprintRefs(
    val key: String,
) {
    INSTANCE_ID("instance.id"),
    INSTANCE_NAME("instance.name"),
    INSTANCE_MEMORY("instance.memory"),
    NETWORK_HOST("network.host"),
    NETWORK_PORT("network.port"),
}
