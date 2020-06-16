

package com.gary.encryption.ui.editentries

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gary.encryption.R
import com.gary.encryption.util.PICK_IMAGE_REQUEST_CODE
import com.gary.encryption.util.hideSoftKeyboard
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_edit_entry.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class EditEntryFragment : Fragment(), Toolbar.OnMenuItemClickListener {

companion object {
  private const val IMG_WIDTH = 640
  private const val IMG_HEIGHT = 480
}
  private val viewModel: EditEntryViewModel by viewModels()

  private val existingEntryName get() = navArgs<EditEntryFragmentArgs>().value.entryStardate

  private var mSelectedImageFileUri: Uri? = null



  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_edit_entry, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    var chosenImage: Bitmap? = null
    encryptButton.setOnClickListener {
      saveEntry()
      NavHostFragment.findNavController(this).navigateUp()
      navigateUp()
    }
    decryptButton.setOnClickListener {
      bodyEditText.visibility = View.GONE
      imageView2.visibility = View.VISIBLE
      imageView2.setImageBitmap(chosenImage)
    }
    getFile.setOnClickListener {
      showImageChooser()
    }
    toolbar.setNavigationOnClickListener {
      saveEntry()
      navigateUp()
    }
    toolbar.inflateMenu(R.menu.toolbar_edit_menu)
    toolbar.setOnMenuItemClickListener(this@EditEntryFragment)

    if (existingEntryName.isNotBlank()) {
      stardateEditText.setText(existingEntryName)
      bodyEditText.setText(viewModel.decryptEntry(existingEntryName))
      chosenImage = convertStringToBitmap(viewModel.decryptEntry(existingEntryName))
      bodyEditText.visibility = View.VISIBLE
    }

    requireActivity().onBackPressedDispatcher.addCallback(this, true) {
      saveEntry()
      findNavController().popBackStack()
    }

    viewModel.snackbar.observe(viewLifecycleOwner, Observer { text ->
      text?.let {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
        viewModel.onSnackbarShown()
      }
    })
  }

  override fun onMenuItemClick(item: MenuItem?): Boolean {
    return when (item?.itemId) {
      R.id.menu_edit_done -> {
        saveEntry()
        NavHostFragment.findNavController(this).navigateUp()
        navigateUp()
        true
      }
      R.id.menu_edit_delete -> {
        viewModel.deleteEntry(existingEntryName)
        navigateUp()
        true
      }
      else -> false
    }
  }

  private fun saveEntry() {
    viewModel.encryptEntry(
      stardateEditText.text.toString(),
      bodyEditText.text.toString(),
      existingEntryName
    )
  }
  private fun navigateUp() {
    NavHostFragment.findNavController(this).navigateUp()
    hideSoftKeyboard(activity as? Activity)
  }
  private fun showImageChooser() {
    var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
  }
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
      mSelectedImageFileUri = data.data
      var bitmap = MediaStore.Images.Media.getBitmap(context?.applicationContext?.contentResolver, data.data)
      // get the base 64 string
      var imgString: String = Base64.encodeToString(
        getBytesFromBitmap(bitmap),
        Base64.NO_WRAP
      )
      bodyEditText.text = imgString.toString()
      try {
        Glide.with(this@EditEntryFragment)
          .load(mSelectedImageFileUri)
          .apply(
            RequestOptions()
              .placeholder(R.drawable.fingerprint_dialog_fp_to_error)
          )
          .into(imageView2)
      } catch (e: IOException) {
        Toast.makeText(context, "ERROR CODE: PERMYPROBLEM", Toast.LENGTH_SHORT).show()
      }
    }
  }
  private fun getBytesFromBitmap(bitmap: Bitmap): ByteArray? {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
    return stream.toByteArray()
  }

  private fun convertString64ToImage(base64String: String): Bitmap {
    val decodedString = Base64.decode(base64String, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
  }

  fun convertStringToBitmap(base64String: String): Bitmap {
    return convertString64ToImage(resizeBase64Image(base64String))
  }

  private fun resizeBase64Image(base64image: String): String {
    val encodeByte: ByteArray = Base64.decode(base64image.toByteArray(), Base64.DEFAULT)
    val options = BitmapFactory.Options()
    var image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size, options)
    if (image.height != 400 && image.width != 400) {
      return base64image
    }
    image = Bitmap.createScaledBitmap(image, IMG_WIDTH, IMG_HEIGHT, false)
    val baos = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val b = baos.toByteArray()
    System.gc()
    return Base64.encodeToString(b, Base64.NO_WRAP)
  }
}
