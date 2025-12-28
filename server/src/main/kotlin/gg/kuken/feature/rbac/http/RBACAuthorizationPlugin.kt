package gg.kuken.feature.rbac.http

import gg.kuken.feature.rbac.model.Permission
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.util.AttributeKey
import kotlin.uuid.Uuid

val GetUserIdKey = AttributeKey<(ApplicationCall) -> Uuid?>("GetUserId")

class RBACAuthorizationConfig {
    var getUserId: (ApplicationCall) -> Uuid? = { null }
}

val RBACAuthorizationPlugin =
    createApplicationPlugin(
        name = "RBACAuthorization",
        createConfiguration = ::RBACAuthorizationConfig,
    ) {
        val getUserId = pluginConfig.getUserId
        application.attributes.put(GetUserIdKey, getUserId)
    }
