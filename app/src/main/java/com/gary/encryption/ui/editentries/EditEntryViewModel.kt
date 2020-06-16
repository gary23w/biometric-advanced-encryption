package com.gary.encryption.ui.editentries

import android.app.Application
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.gary.encryption.R
import com.gary.encryption.util.urlEncode
import java.io.File
import java.lang.Exception

class EditEntryViewModel(application: Application) : AndroidViewModel(application) {

  val snackbar: LiveData<String?>
    get() = _snackbar
  private val _snackbar = MutableLiveData<String?>()
  private val context = getApplication<Application>().applicationContext
  private var useFile = false


  fun onSnackbarShown() {
    _snackbar.value = null
  }


  fun encryptEntry(stardate: String, body:String, existingName: String) {
    if(stardate.isBlank()) return
    try {
        deleteEntry(existingName)
        val encryptedFile = getEncryptedEntry(stardate)
        encryptedFile.openFileOutput().use { output ->
        output.write(body.toByteArray())
      }
    } catch (e: Exception) {
      e.printStackTrace()
      _snackbar.value = context.getString(R.string.error_unable_to_save_entry)
    }
  }


  fun decryptEntry(stardate: String): String {
   val encryptedFile = getEncryptedEntry(stardate)
    try {
        encryptedFile.openFileInput().use { input ->
          return String(input.readBytes(), Charsets.UTF_8)
        }

    } catch (e: Exception) {
      e.printStackTrace()
      _snackbar.value = context.getString(R.string.error_unable_to_decrypt)
      return ""
    }
  }


  fun deleteEntry(stardate: String) {
    if (stardate.isBlank()) return
    val file = File(context.filesDir, stardate.urlEncode())
    if (file.exists()) file.delete()
  }
  private fun getEncryptedEntry(name: String): EncryptedFile {
    val advancedSpec = KeyGenParameterSpec.Builder(
      "master_key",
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).apply {
      setBlockModes((KeyProperties.BLOCK_MODE_GCM))
      setEncryptionPaddings((KeyProperties.ENCRYPTION_PADDING_NONE))
      setKeySize(256)
      setUserAuthenticationRequired(true)
      setUserAuthenticationValidityDurationSeconds(15)
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        setUnlockedDeviceRequired(true)
        setIsStrongBoxBacked(true)
      }
    }.build()

    val keyGenParamterSpec = MasterKeys.AES256_GCM_SPEC
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParamterSpec)
    val fileEnryptionScheme = EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB

    return EncryptedFile.Builder(
      File(context.filesDir, name.urlEncode()),
      context,
      masterKeyAlias,
      fileEnryptionScheme
    ).build()
  }
}