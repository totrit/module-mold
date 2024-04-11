package com.totrit.modulemold.exampleapp.somekotlinlib

import java.util.UUID

class IdGenerator {
    fun generate(): String = UUID.randomUUID().toString()
}