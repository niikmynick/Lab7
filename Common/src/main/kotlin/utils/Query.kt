package utils

import kotlinx.serialization.Serializable

@Serializable
data class Query (val queryType: QueryType, val information: String, val args: MutableMap<String, String>, var token: String = "")
