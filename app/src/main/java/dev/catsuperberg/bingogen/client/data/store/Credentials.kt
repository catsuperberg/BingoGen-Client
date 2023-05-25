package dev.catsuperberg.bingogen.client.data.store

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import dev.catsuperberg.bingogen.client.Credentials
import java.io.InputStream
import java.io.OutputStream

object CredentialsSerializer : Serializer<Credentials> {
    override val defaultValue: Credentials = Credentials.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Credentials {
        try {
            return Credentials.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: Credentials,
        output: OutputStream
    ) = t.writeTo(output)
}

val Context.credentialsDataStore: DataStore<Credentials> by dataStore(
    fileName = "Credentials.pb",
    serializer = CredentialsSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { Credentials.getDefaultInstance() }
    )
)
