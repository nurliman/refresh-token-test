package model

import kotlinx.serialization.Serializable
import util.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class PatientListItem(
    val id: String,
    val name: String,
    val address: String,
    @Serializable(LocalDateSerializer::class) val dateOfBirth: LocalDate,
)
