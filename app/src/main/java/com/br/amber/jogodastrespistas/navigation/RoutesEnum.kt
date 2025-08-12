package com.br.amber.jogodastrespistas.navigation

enum class RoutesEnum(val route: String) {
    LOGIN("login"),
    HOME("home"),
    ROOM("room/{roomId}");

    companion object {
        fun roomWithId(roomId: String): String = "room/$roomId"
    }
}
