package gg.kuken.features.auth.http.exception

import gg.kuken.http.HttpError
import gg.kuken.http.exception.ResourceException
import io.ktor.http.HttpStatusCode

class InvalidAccessTokenException : ResourceException(HttpError.InvalidAccessToken, HttpStatusCode.Unauthorized)