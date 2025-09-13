package com.example.autostradaauctions.data.serialization

import com.example.autostradaauctions.data.model.UserRole
import com.google.gson.*
import java.lang.reflect.Type

class UserRoleAdapter : JsonDeserializer<UserRole>, JsonSerializer<UserRole> {
    
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): UserRole? {
        return when (val roleString = json?.asString?.uppercase()) {
            "ADMIN" -> UserRole.ADMIN
            "SELLER" -> UserRole.SELLER
            "BUYER" -> UserRole.BUYER
            else -> UserRole.BUYER // Default to BUYER for unrecognized roles
        }
    }
    
    override fun serialize(
        src: UserRole?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src?.name ?: "BUYER")
    }
}