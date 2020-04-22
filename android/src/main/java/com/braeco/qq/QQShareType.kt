package com.braeco.qq

enum class QQShareType(val value: Int) {
    WXShareTypeText(0),
    WXShareTypeImage(1),
    WXShareTypeMusic(2),
    WXShareTypeVideo(3),
    WXShareTypeWeb(4);

    companion object {
        fun fromInt(raw: Int) = values().firstOrNull { it.value == raw }
    }
}