package org.jetbrains.kotlin.demo

import org.jetbrains.kotlin.demo.models.RESTModels
import org.springframework.web.bind.annotation.*

@RestController
class RESTController {
    @GetMapping("/add")
    fun addNotations(@RequestParam(value = "email") email: String,
                     @RequestParam(value = "password") password: String,
                     @RequestParam(value = "notation") notation: String) = DatabaseController().addNotation(RESTModels.NotationsModel(RESTModels.LogInModel(email, password), notation))


    @GetMapping("/signup")
    fun signup(@RequestParam(value = "email") email: String,
               @RequestParam(value = "password") password: String,
               @RequestParam(value = "name") name: String) = DatabaseController().addUser(RESTModels.SignUpModel(name, RESTModels.LogInModel(email, password), 0))

    @GetMapping("/login")
    fun login(@RequestParam(value = "email") email: String,
              @RequestParam(value = "password") password: String) = DatabaseController().login(RESTModels.LogInModel(email, password))

    @GetMapping("/get")
    fun getNotations(@RequestParam(value = "email") email: String,
                     @RequestParam(value = "password") password: String,
                     @RequestParam(value = "from") from: Int,
                     @RequestParam(value = "to") to: Int) = DatabaseController().getNotations(RESTModels.LogInModel(email,password), from, to)
}