package ru.alexey.flowapp.core.model

import java.util.UUID

/** Generates an id for a new domain entity */
fun newId(): String = UUID.randomUUID().toString()