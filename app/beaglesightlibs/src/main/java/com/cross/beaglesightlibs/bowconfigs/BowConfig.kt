package com.cross.beaglesightlibs.bowconfigs

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Objects
import java.util.UUID

@JsonClass(generateAdapter = true)
data class BowConfig(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var description: String = "",
    var positionArray: MutableList<PositionPair> = ArrayList(),
) {
    val positionCalculator: PositionCalculator
        get() {
            val positionCalculator: PositionCalculator = LineOfBestFitCalculator()
            positionCalculator.positions = positionArray
            return positionCalculator
        }

    override fun toString(): String {
        return String.format("ID: %s, Name: %s, Desc: %s", id, name, description)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BowConfig) return false
        return id == other.id && name == other.name && description == other.description && positionArray == other.positionArray
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, description, positionArray)
    }

    fun export(outputStream: FileOutputStream) {
        val moshi: Moshi = Moshi.Builder().build()
        val jsonAdapter: JsonAdapter<BowConfig> = moshi.adapter(BowConfig::class.java)
        outputStream.write(jsonAdapter.toJson(this).toByteArray())
        outputStream.close()
    }

    companion object {
        fun load(inputStream: InputStream): BowConfig? {
            val moshi: Moshi = Moshi.Builder().build()
            val jsonAdapter: JsonAdapter<BowConfig> = moshi.adapter(BowConfig::class.java)

            try {
                val jsonData = String(inputStream.readBytes())

                return jsonAdapter.fromJson(jsonData)!!
            } catch (_: Exception) {
            }
            return null
        }
    }
}