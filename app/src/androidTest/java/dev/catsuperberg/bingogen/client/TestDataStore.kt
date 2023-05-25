package dev.catsuperberg.bingogen.client

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import dev.catsuperberg.bingogen.client.data.store.CredentialsSerializer

val Context.testDataStore: DataStore<Credentials> by dataStore(
    fileName = "test.pb",
    serializer = CredentialsSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { Credentials.getDefaultInstance() }
    )
)
