package com.exa.android.loctrace.Helper

import android.content.Context
import android.util.Patterns
import android.widget.Toast

object AppConstants {

     var userId : String ? = null

    fun verifyEmail(email:String):Pair<Boolean, String>{
        var result=Pair(true, "")
        if(email.isBlank()){
            result=Pair(false, "Email must be provided.")
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.contains('@')){
            result=Pair(false, "Enter valid Email")
        }
        return result
    }
    fun verifyPassword(password:String):Pair<Boolean, String>{
        var result=Pair(true, "")
        if(password.isBlank()){
            result=Pair(false, "Password must be provided.")
        }else if(password.length<6){
            result=Pair(false, "Password must be of 6 character")
        }
        return result
    }
    fun verifyConfirmPassword(password:String, confirmPassword:String):Pair<Boolean, String>{
        var result=Pair(true, "")
        if(password.isBlank() || confirmPassword.isBlank())result=Pair(false, "Password must be provided.")
        else if(password!=confirmPassword)result=Pair(false, "ConfirmPassword must match with Password")
        return result
    }
    fun showToast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}