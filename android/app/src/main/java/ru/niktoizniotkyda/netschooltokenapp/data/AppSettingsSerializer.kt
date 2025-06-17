package ru.niktoizniotkyda.netschooltokenapp.data

import java.io.InputStream
import java.io.OutputStream
import androidx.datastore.core.Serializer

object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings = AppSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppSettings {
        return AppSettings.parseFrom(input)
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        t.writeTo(output)
    }
}