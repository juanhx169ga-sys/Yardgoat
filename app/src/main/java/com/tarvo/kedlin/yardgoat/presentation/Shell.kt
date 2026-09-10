package com.tarvo.kedlin.yardgoat.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class Route {
    data object Splash : Route()
    data object Tutorial : Route()
    data object Menu : Route()
    data object Jobs : Route()
    data class Drive(val job: Int) : Route()
    data object Awards : Route()
    data object Records : Route()
    data object Setup : Route()
}

class Shell {
    private val stack = mutableStateListOf<Route>(Route.Splash)

    var current by mutableStateOf<Route>(Route.Splash)
        private set

    fun go(route: Route) {
        stack.add(route)
        current = route
    }

    fun swap(route: Route) {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
        stack.add(route)
        current = route
    }

    fun back(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        current = stack[stack.lastIndex]
        return true
    }

    fun home() {
        stack.clear()
        stack.add(Route.Menu)
        current = Route.Menu
    }
}
