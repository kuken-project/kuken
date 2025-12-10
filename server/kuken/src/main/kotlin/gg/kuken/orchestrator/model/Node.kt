package gg.kuken.orchestrator.model

data class Node(
    val identifier: String,
    val state: NodeState,
    val role: NodeRole,
    val hostAddress: String,
)
