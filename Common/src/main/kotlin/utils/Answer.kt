package utils

import kotlinx.serialization.Serializable

@Serializable
class Answer (val answerType: AnswerType, override var message: String, override var token: String = "", var receiver: String) : Sending