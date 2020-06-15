
package com.gary.encryption.util

import android.os.FileObserver
import androidx.lifecycle.LiveData
import com.gary.encryption.model.LogEntry
import java.io.File

class DirectoryLiveData(
    private val observationDir: File
) : LiveData<List<LogEntry>>() {

    @Suppress("deprecation")
    private val observer = object : FileObserver(observationDir.path) {
        override fun onEvent(event: Int, path: String?) {
            dispatchFilesChanged()
        }
    }

    private fun dispatchFilesChanged() {
        postValue(observationDir.listFiles()?.map { LogEntry(it.name.urlDecode(), it.path) }?.sortedBy { entry ->
            entry.stardate
        }?.reversed() ?: emptyList())
    }

    override fun onActive() {
        super.onActive()
        dispatchFilesChanged()
        observer.startWatching()
    }

    override fun onInactive() {
        super.onInactive()
        observer.stopWatching()
    }
}