package gg.kuken.feature.unit

import gg.kuken.core.KukenException

open class UnitException : KukenException()

class UnitConflictException : UnitException()

class UnitNotFoundException : UnitException()
