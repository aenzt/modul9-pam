package com.example.modul9

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.modul9.databinding.ActivityInsertNoteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class InsertNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsertNoteBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("NOTES")
        binding = ActivityInsertNoteBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this@InsertNoteActivity)

        binding.btnSubmit.setOnClickListener {
            submitData()
        }
    }

    private fun submitData(){
        if (!validateForm()){
            return
        }
        val title = binding.etTitle.text.toString()
        val desc = binding.etDescription.text.toString()
        val time = System.currentTimeMillis()/1000
        val id = getRandomString(12)

        val newNote = Note(id, title, desc, time.toString())

        loadingDialog.startDialog()
        databaseReference.child(auth.uid!!).child(id).setValue(newNote).addOnSuccessListener (this){
            loadingDialog.dismissDialog()
            startActivity(Intent(this@InsertNoteActivity, NoteActivity::class.java))
        }.addOnFailureListener(this) {
            Toast.makeText(this@InsertNoteActivity, "Failed to add data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateForm(): Boolean {
        var result = true
        if (binding.etTitle.text.isEmpty()){
            binding.etTitle.error = "Required"
            result = false
        }else {
            binding.etTitle.error = null
        }
        if (binding.etDescription.text.isEmpty()){
            binding.etDescription.error = "Required"
            result = false
        } else {
            binding.etDescription.error = null
        }
        return result
    }

    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}