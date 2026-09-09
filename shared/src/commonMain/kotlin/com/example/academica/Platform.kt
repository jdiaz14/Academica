package com.example.academica

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform