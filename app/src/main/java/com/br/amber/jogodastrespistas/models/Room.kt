package com.br.amber.jogodastrespistas.models

data class Room(
    val owner: Player = Player(),
    val guest: Player = Player(id = "guest", isOnline = false),
    var ownerTurn: Boolean = true,
    var status: String = RoomStatusesEnum.WAITING.status,
    var cluesShown: Int = 0,
    var round: Int = 0,
    val drawnWords: List<Word> = emptyList()
) {
    constructor() : this(owner = Player())

}