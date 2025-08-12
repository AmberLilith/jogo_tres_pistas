package com.br.amber.jogodastrespistas

fun String.normalize(): String {
    return this
        .lowercase()
        .normalizeAccents()
        .replace("[^a-z0-9 ]".toRegex(), "")
        .replace("\\s+".toRegex(), " ")
        .trim()

}

fun String.normalizeAccents(): String {
    val comAcentos = "áàâãäéèêëíìîïóòôõöúùûüçñ"
    val semAcentos = "aaaaaeeeeiiiiooooouuuucn"

    return this.map { char ->
        val index = comAcentos.indexOf(char)
        if (index >= 0) semAcentos[index] else char
    }.joinToString("")
}