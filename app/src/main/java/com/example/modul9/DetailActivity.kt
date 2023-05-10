package com.example.modul9

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.modul9.databinding.ActivityDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var note: Note
    private lateinit var database : DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database.getReference("NOTES")
        auth = FirebaseAuth.getInstance()

        note = intent.getParcelableExtra(NoteActivity.INTENT_ITEM)!!

        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.ENGLISH)
        val date = sdf.format(note.timestamp!!.toInt() * 1000L)

        note?.let {
            binding.tvTitleDetail.text = note.title
            binding.etDescriptionDetail.setText(note.description)
            binding.tvTimestampDetail.text = "Last updated: $date"
        }

        binding.btnEdit.setOnClickListener {
            edit()
        }
    }

    private fun edit(){
        var desc = binding.etDescriptionDetail.text
        note.description = desc.toString()
        note.timestamp = (System.currentTimeMillis() / 1000).toString()
        database.child(auth.currentUser!!.uid).child(note.id!!).setValue(note).addOnCompleteListener(this) {
            finish()
        }
    }
}