package org.jetbrains.kotlin.demo

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.kotlin.demo.models.RESTModels
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.LoggerFactory
import java.sql.Connection


class DatabaseController {

    private fun connect(){
        Database.connect(url = "jdbc:mysql://localhost/notations", driver =  "com.mysql.jdbc.Driver", user = "root", password = "0987654")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction {
            logger.addLogger(StdOutSqlLogger)
            create(SignUpTable)
            create(NotationTable)
        }
    }

    class SignUp(id: EntityID<Int>): IntEntity(id){
        companion object: IntEntityClass<SignUp>(SignUpTable)

        var email by SignUpTable.email
        var name by SignUpTable.name
        var password by SignUpTable.password
    }

    class Notation(id: EntityID<Int>): IntEntity(id){
        companion object: IntEntityClass<Notation>(NotationTable)

        var notationId by NotationTable.idOfUserNotation
        var email by NotationTable.email
        var notation by NotationTable.notation
    }

    object SignUpTable: IntIdTable("SignUpTable") {
        val email: Column<String> = varchar("email", 60)
        val name: Column<String> = varchar("name", 25)
        val password: Column<String> = varchar("password", 60)
    }

    object NotationTable: IntIdTable("notation"){
        val idOfUserNotation: Column<Int> = integer("notation_id")
        val email: Column<String> = varchar("email", 60)
        val notation: Column<String> = varchar("notation", 250)
    }


    fun addUser(signUpModel: RESTModels.SignUpModel): RESTModels.SignUpModelResponse {
        connect()

        if(transaction {SignUp.find{SignUpTable.email eq signUpModel.logInModel.email}.empty()}) {

            transaction {
                SignUp.new {
                    email = signUpModel.logInModel.email
                    name = signUpModel.name
                    password = signUpModel.logInModel.password
                }
            }
            return RESTModels.SignUpModelResponse(signUpModel, "OK")
        }
        return RESTModels.SignUpModelResponse(signUpModel, "ERROR, this account already exist")
    }

    fun addNotation(notationsModel: RESTModels.NotationsModel): RESTModels.NotationModelResponse {
        connect()
        if(transaction {SignUp.find{SignUpTable.email eq notationsModel.logInModel.email}.first().password == notationsModel.logInModel.password}){
            transaction {
                val list = NotationTable.select { NotationTable.email eq notationsModel.logInModel.email }.toMutableList()
                Notation.new {
                    notationId = list.size
                    email = notationsModel.logInModel.email
                    notation = notationsModel.notations
                }
            }
        }
        return RESTModels.NotationModelResponse(notationsModel.logInModel, "OK")
    }

    fun getNotations(logInModel: RESTModels.LogInModel, from: Int, to: Int): RESTModels.NotationResponse {
        connect()
        var response = RESTModels.NotationResponse(List(init = { RESTModels.NotationResponseItem(0, "", "") }, size = 0))

        if(login(logInModel).result == "OK") {
            val list = transaction { NotationTable.select { NotationTable.email eq logInModel.email and (NotationTable.idOfUserNotation.between(from, to)) }.toMutableList() }
            response = RESTModels.NotationResponse(List(init = { RESTModels.NotationResponseItem(0, "", "") }, size = list.size))
            transaction {
                for (i in list.indices) {
                    response.response[i].email = list[i].data[2].toString()
                    response.response[i].id = list[i].data[1] as Int
                    response.response[i].notation = list[i].data[3].toString()
                }
            }
        }
        return response
    }

    fun login(logInModel: RESTModels.LogInModel): RESTModels.LogInModelResponse {
        connect()
        val table = transaction {SignUp.find { SignUpTable.email eq logInModel.email}}
        if(transaction {table.count() > 0}) {
            if (transaction { table.first().password == logInModel.password }) {
                return RESTModels.LogInModelResponse(logInModel, "OK")
            }
        }
        return RESTModels.LogInModelResponse(logInModel, "ERROR")
    }
}

