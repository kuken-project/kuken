package gg.kuken.feature.rbac.exception

import gg.kuken.http.HttpError
import gg.kuken.http.exception.ResourceException
import io.ktor.http.HttpStatusCode

class InsufficientPermissionsException : ResourceException(HttpError.InsufficientPermissions, HttpStatusCode.Forbidden)
