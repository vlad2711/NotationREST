package org.jetbrains.kotlin.demo

import org.jetbrains.kotlin.demo.models.RESTModels
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest


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


    @GetMapping("/get")
    fun getNotations(@RequestParam(value = "email") email: String,
                     @RequestParam(value = "password") password: String) = DatabaseController().getNotations(RESTModels.LogInModel(email,password))

    @DeleteMapping("/delete")
    fun deleteNotations(@RequestParam(value = "login")login: RESTModels.LogInModel){

    }


}