package com.example.modul9

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.modul9.databinding.ActivityNoteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.format.DateTimeFormatter

class NoteActivity : AppCompatActivity() {

    companion object {
        const val INTENT_ITEM = "intent_item"
    }

    private lateinit var binding: ActivityNoteBinding
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var database : DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingDialog: LoadingDialog
    private var listNote = ArrayList<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = Firebase.database.getReference("NOTES")
        auth = FirebaseAuth.getInstance()

        loadingDialog = LoadingDialog(this@NoteActivity)

        binding.title.text = auth.currentUser?.email + "'s Note List"

        readData()
        setNoteRecycle()
        onAction()
    }

    private fun onAction() {
        binding.tvOption.setOnClickListener {
            val intent = Intent(this@NoteActivity, InsertNoteActivity::class.java)
            startActivity(intent)
        }
        noteAdapter.onItemClick = {
            note: Note -> startActivity(Intent(this@NoteActivity, DetailActivity::class.java).putExtra(INTENT_ITEM, note))
        }

        noteAdapter.onDeleteClick = { note: Note ->
            delete(note)
        }

        binding.tvLogout.setOnClickListener {
            logOut()
        }

    }

    private fun setNoteRecycle(){
        noteAdapter = NoteAdapter()
        binding.rv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = noteAdapter
        }
        noteAdapter.setNote(listNote)
    }

    private fun readData(){
        var query : Query = database.child(auth.currentUser!!.uid).orderByChild("timestamp")
        val noteListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listNote.clear()
                for (h in snapshot.children){
                    listNote.add(h.getValue<Note>()!!)
                }
                noteAdapter.setNote(listNote)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", error.toException())
            }
        }
        query.addValueEventListener(noteListener)
    }

    private fun delete(note: Note){
        loadingDialog.startDialog()
        database.child(auth.currentUser!!.uid).child(note.id!!).removeValue().addOnCompleteListener {
            loadingDialog.dismissDialog()
        }
    }

    private fun logOut(){
        auth.signOut()
        val intent = Intent(this@NoteActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}