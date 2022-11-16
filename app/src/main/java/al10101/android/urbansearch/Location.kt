package al10101.android.urbansearch

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Location(
    @PrimaryKey val uuid: UUID = UUID.randomUUID(),
    val title: String,
    val date: Date = Date(),
    val latitude: Double,
    val longitude: Double
)
