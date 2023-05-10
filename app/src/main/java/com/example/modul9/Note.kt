package com.example.modul9

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note (
    var id: String? = "",
    var title: String? = "",
    var description: String? = "",
    var timestamp: String? = "0"
) : Parcelable