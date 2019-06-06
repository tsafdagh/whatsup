package com.mirai.whatsup.entities

data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
}