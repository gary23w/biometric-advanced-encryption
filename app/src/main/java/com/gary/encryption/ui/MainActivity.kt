package com.gary.encryption.ui


import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gary.encryption.R
import com.gary.encryption.util.READ_STORAGE_PERMISSION_CODE


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_CODE)
        }


    }

}
