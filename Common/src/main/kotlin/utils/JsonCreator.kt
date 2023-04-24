package utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class JsonCreator {
    inline fun <reified T> objectToString(clazz: T): String {
        return Json.encodeToString(serializer(), clazz)
    }

    inline fun <reified T> stringToObject(json: String): T {
        return Json.decodeFromString(serializer(), json)
    }
}