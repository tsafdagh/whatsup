package com.mirai.whatsup.entities

data class User(val name: String,
                val bio:String,
                val profilePicturePath: String?
                ){
    constructor(): this("","", null)
}