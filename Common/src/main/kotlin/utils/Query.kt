package utils

import kotlinx.serialization.Serializable

@Serializable
data class Query (val queryType: QueryType, override var message: String, val args: MutableMap<String, String>, override var token: String = "") : Sending
