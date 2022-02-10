package com.example.mymessagingapp.Main.system.Fragment.utilities

import com.example.mymessagingapp.Main.utils.CONSTANT
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DeleteDataUser {
    companion object{

        fun deleteUserFromGroup(userId : String, groupId : String){
            val db = Firebase.firestore
            db.collection(CONSTANT.KEY_USER).document(userId)
                .get().addOnSuccessListener {  value ->
                    if(value != null){
                        var listGroup = value.data?.get(CONSTANT.KEY_USER_LIST_GROUP_ID) as MutableList<String>
                        listGroup.remove(groupId)
                        val hashMapOf = hashMapOf(
                            CONSTANT.KEY_USER_LIST_GROUP_ID to listGroup
                        )
                        db.collection(CONSTANT.KEY_USER).document(userId).update(hashMapOf as Map<String, Any>)
                    }
            }
            db.collection(CONSTANT.KEY_GROUP).document(groupId)
                .get().addOnSuccessListener { value ->
                    if(value != null){
                        var listMember = value.data?.get(CONSTANT.KEY_GROUP_LIST_MEMBER) as MutableList<String>
                        listMember.remove(userId)
                        val hashMapOf = hashMapOf(
                            CONSTANT.KEY_USER_LIST_GROUP_ID to listMember
                        )
                        db.collection(CONSTANT.KEY_GROUP).document(groupId).update(hashMapOf as Map<String, Any>)
                    }
                }
        }
        fun deleteAllGroupOfUser(userId : String){
            val db = Firebase.firestore
            db.collection(CONSTANT.KEY_USER).document(userId)
                .get().addOnSuccessListener { value ->
                    if(value != null){
                        val listGroup = value.data?.get(CONSTANT.KEY_USER_LIST_GROUP_ID) as MutableList<String>
                        for (i in listGroup){
                            db.collection(CONSTANT.KEY_GROUP).document(i).get()
                                .addOnSuccessListener { value1 ->
                                    val listMember = value.data?.get(CONSTANT.KEY_GROUP_LIST_MEMBER) as MutableList<String>
                                    listGroup.remove(userId)
                                    val hashMapOf = hashMapOf(
                                        CONSTANT.KEY_USER_LIST_GROUP_ID to listMember
                                    )
                                    db.collection(CONSTANT.KEY_GROUP).document(i)
                                        .update(hashMapOf as Map<String, Any>)
                                }
                        }
                    }
                }
            val hashMapOfUser = hashMapOf(
                CONSTANT.KEY_USER_LIST_GROUP_ID to emptyList<String>()
            )
            db.collection(CONSTANT.KEY_USER).document(userId).update(hashMapOfUser as Map<String, Any>)
        }
    }
}