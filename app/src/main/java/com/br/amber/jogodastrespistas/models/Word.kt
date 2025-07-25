package com.br.amber.jogodastrespistas.models

data class Word(
    val name: String = "",
    val clues: List<String> = emptyList<String>()
) {
    constructor() : this("", emptyList<String>())
}