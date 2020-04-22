package com.braeco.qq

enum class QQShareScene(val value: Int) {
    Session(0),
    QZone(1);

    companion object {
        fun fromInt(raw: Int) = values().firstOrNull { it.value == raw }
    }
}