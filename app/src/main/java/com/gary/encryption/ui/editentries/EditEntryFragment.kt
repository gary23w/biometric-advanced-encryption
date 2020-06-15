

package com.gary.encryption.ui.editentries

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gary.encryption.R
import com.gary.encryption.util.hideSoftKeyboard
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_edit_entry.*

class EditEntryFragment : Fragment(), Toolbar.OnMenuItemClickListener {

  private val viewModel: EditEntryViewModel by viewModels()

  private val existingEntryName get() = navArgs<EditEntryFragmentArgs>().value.entryStardate

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_edit_entry, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    toolbar.setNavigationOnClickListener {
      saveEntry()
      navigateUp()
    }
    toolbar.inflateMenu(R.menu.toolbar_edit_menu)
    toolbar.setOnMenuItemClickListener(this@EditEntryFragment)

    if (existingEntryName.isNotBlank()) {
      stardateEditText.setText(existingEntryName)
      bodyEditText.setText(viewModel.decryptEntry(existingEntryName))
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
}
