package gg.kuken.orchestrator

import gg.kuken.orchestrator.model.Node
import gg.kuken.orchestrator.model.NodeRole
import gg.kuken.orchestrator.model.NodeState
import io.lettuce.core.RedisClient
import org.apache.commons.lang3.mutable.Mutable

class Orchestrator(
    val redisClient: RedisClient,
) {
    private val _connectedNodes = mutableMapOf<String, Node>()
    val connectedNodes get() = _connectedNodes.toList()

    fun onNodeConnected(node: Node) {
    }

    fun onNodeDisconnected(node: Node) {
    }
}
