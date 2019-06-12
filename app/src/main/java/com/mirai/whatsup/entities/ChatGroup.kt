package com.mirai.whatsup.entities

import java.util.*

class ChatGroup(
    val adminId: String,
    val groupeName: String,
    val groupeDescription:String,
    val createdAt: Date,
    val groupIcon: String,
    val members: MutableList<String>?
) {
    constructor(): this("","","", Date(0),"",null)
}