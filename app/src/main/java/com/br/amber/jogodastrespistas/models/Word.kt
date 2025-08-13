package com.br.amber.jogodastrespistas.models

data class Word(
    val name: String = "",
    val used: Boolean = false,
    val clues: List<String> = emptyList()
) {
    constructor() : this("", false, emptyList<String>())
}