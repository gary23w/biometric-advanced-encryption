
package com.gary.encryption.util

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import java.net.URLDecoder
import java.net.URLEncoder


  const val PICK_IMAGE_REQUEST_CODE = 2
  const val READ_STORAGE_PERMISSION_CODE = 1
  var FILE_CHECK = false


fun String.urlDecode():String = URLDecoder.decode(this, "UTF-8")

fun String.urlEncode():String = URLEncoder.encode(this, "UTF-8")

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
  return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun Fragment.hideSoftKeyboard(activity: Activity?) {
  if (activity?.currentFocus == null) {
    return
  }
  val inputMethodManager: InputMethodManager =
    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
}