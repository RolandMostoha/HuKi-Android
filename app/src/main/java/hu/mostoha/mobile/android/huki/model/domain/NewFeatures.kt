package hu.mostoha.mobile.android.huki.model.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class NewFeatures(
    val version: String,
    val releaseDate: LocalDate,
    val releaseNotes: String
) : Parcelable
