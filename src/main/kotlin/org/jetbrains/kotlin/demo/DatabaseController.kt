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

        var email by NotationTable.email
        var notation by NotationTable.notation
    }

    object SignUpTable: IntIdTable("SignUpTable") {
        val email: Column<String> = varchar("email", 60)
        val name: Column<String> = varchar("name", 25)
        val password: Column<String> = varchar("password", 60)
    }

    object NotationTable: IntIdTable("NotationTable"){
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
                Notation.new {
                    email = notationsModel.logInModel.email
                    notation = notationsModel.notations
                }
            }
        }
        return RESTModels.NotationModelResponse(notationsModel.logInModel, "OK")
    }

    fun getNotations(logInModel: RESTModels.LogInModel): MutableList<ResultRow> {
        connect()
        if(transaction {SignUp.find{SignUpTable.email eq logInModel.email}.first().password == logInModel.password}){
            return transaction{ NotationTable.select {NotationTable.email eq logInModel.email}.toMutableList()}
        }
        return emptyArray<ResultRow>().toMutableList()
    }

    fun deleteNotation(notationsModel: RESTModels.NotationsModel) = NotationTable.deleteWhere{
        (NotationTable.notation eq notationsModel.notations) and (NotationTable.email eq notationsModel.logInModel.email)
    }
}

