package com.br.amber.jogodastrespistas.models

data class Player(
    var id: String = "",
    var nickName: String = "",
    var points: Int = 0,
    var online: Boolean = false,//Não pode ser "isOnline" porque o firebase nao aceita
){
    constructor() : this("", "", 0, false)
}