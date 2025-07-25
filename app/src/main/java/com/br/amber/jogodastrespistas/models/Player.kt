package com.br.amber.jogodastrespistas.models

data class Player(
    var id: String = "",
    var points: Int = 0,
    var isOnline: Boolean = false
){
    constructor() : this("", 0, false)
}