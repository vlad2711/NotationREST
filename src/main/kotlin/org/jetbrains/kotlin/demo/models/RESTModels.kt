package org.jetbrains.kotlin.demo.models

class RESTModels {
    data class LogInModel(val email: String, val password: String)
    data class NotationsModel(val logInModel: LogInModel, val notations: String)
    data class SignUpModel(val name: String, val logInModel: LogInModel, val islandId: Int)
    data class SignUpModelResponse(val signUpModel: SignUpModel, val result: String)
    data class NotationModelResponse(val logInModel: LogInModel, val result: String)
    data class LogInModelResponse(val logInModel: LogInModel, val result: String)
    data class NotationResponseItem(var id: Int, var email: String, var notation: String)
    data class NotationResponse(val response: List<NotationResponseItem>)
}     