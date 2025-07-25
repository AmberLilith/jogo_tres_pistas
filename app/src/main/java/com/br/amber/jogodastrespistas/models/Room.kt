package com.br.amber.jogodastrespistas.models

data class Room(
    val owner: Player = Player(), // Sempre inicializado
    val guest: Player = Player(id = "guest", isOnline = false),
    var ownerTurn: Boolean = true,
    var status: String = RoomStatusesEnum.WAITING.status,
    var cluesShown: Int = 0,
    var round: Int = 0,
    val drawnWords: List<Word> = emptyList() // Lista imutável
) {
    // Construtor para Firebase
    constructor() : this(owner = Player())

    // Factory method para criação lógica
    companion object {
        fun create(ownerId: String, words: List<Word>): Room {
            return Room(
                owner = Player(id = ownerId, isOnline = true),
                drawnWords = words
            )
        }
    }
}