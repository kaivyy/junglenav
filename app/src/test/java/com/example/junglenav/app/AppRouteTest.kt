package com.example.junglenav.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRouteTest {
    @Test
    fun mapRouteIsTheDefaultStartDestination() {
        assertEquals(AppRoute.Map.route, AppRoute.startDestination)
    }
}
