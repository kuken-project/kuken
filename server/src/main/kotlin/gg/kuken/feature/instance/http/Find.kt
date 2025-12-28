package gg.kuken.feature.instance.http

import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.rbac.Permissions
import gg.kuken.feature.rbac.http.requirePermission
import gg.kuken.http.util.validateOrThrow
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.util.getOrFail
import jakarta.validation.Validator
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

fun Route.getInstanceDetails() {
    requirePermission(Permissions.ReadInstance) { call ->
        call.parameters.getOrFail("instanceId").let(Uuid::parse)
    }

    val instanceService by inject<InstanceService>()
    val validator by inject<Validator>()

    get<InstanceRoutes.ById> { parameters ->
        validator.validateOrThrow(parameters)

        val instance = instanceService.getInstance(parameters.instanceId)
        call.respond(instance)
    }
}
