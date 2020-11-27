package com.philblandford.mp3converter.ui.report

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

fun reportError(midiFile:ByteArray, name:String, e:Exception) {
  val storage = Firebase.storage
  val storageRef = storage.reference
  val nameRef = storageRef.child(name)
  val uploadTask = nameRef.putBytes(midiFile)
  uploadTask.addOnFailureListener{
    Log.e("Error", "Failed uploading file $name", it)
  }
  uploadTask.addOnSuccessListener {
    Log.d("Error", "Upload successful")
  }

  FirebaseCrashlytics.getInstance().recordException(Exception("Failed converting $name", e))

}