package gg.kuken.features.account.http.exception

import gg.kuken.http.HttpError
import gg.kuken.http.exception.ResourceNotFoundException

class AccountNotFoundException : ResourceNotFoundException(HttpError.UnknownAccount)