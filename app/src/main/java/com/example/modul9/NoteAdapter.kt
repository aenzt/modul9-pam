package com.example.modul9

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.modul9.databinding.ItemListBinding
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NoteAdapter: RecyclerView.Adapter<NoteAdapter.ViewHolder> () {
    lateinit var onItemClick: ((Note) -> Unit)
    lateinit var onDeleteClick: ((Note) -> Unit)

    var noteList = arrayListOf<Note>()

    fun setNote(notes: ArrayList<Note>){
        noteList.clear()
        noteList.addAll(notes)
        notifyDataSetChanged()
    }

    class ViewHolder (var binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteAdapter.ViewHolder {
        return ViewHolder(ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = noteList[position]
        holder.binding.tvTitle.text = note.title
        holder.binding.tvDesc.text = note.description
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.ENGLISH)
        val date = sdf.format(note.timestamp!!.toInt() * 1000L)

        holder.binding.tvTimestamp.text = "Last updated: $date"

        holder.itemView.setOnClickListener{
            onItemClick.invoke(note)
        }

        holder.binding.btnDelete.setOnClickListener {
            onDeleteClick.invoke(note)
        }
    }
}
