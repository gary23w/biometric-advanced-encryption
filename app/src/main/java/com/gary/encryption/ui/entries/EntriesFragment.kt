package com.gary.encryption.ui.entries


import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gary.encryption.R
import com.gary.encryption.model.LogEntry
import com.gary.encryption.util.DirectoryLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_entries.*


class EntriesFragment : Fragment(),
  EntryAdapter.EntryAdapterListener {

  private val viewModel: EntriesViewModel by viewModels()

  private val authenticationCallBack = object: BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
      super.onAuthenticationSucceeded(result)
      editLogKey()
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
      super.onAuthenticationError(errorCode, errString)
      viewModel.showSnackbar(getString(R.string.error_unable_to_authenticate_biometrically))
    }
  }

  private lateinit var promptInfo: BiometricPrompt.PromptInfo
  private lateinit var biometricPrompt: BiometricPrompt

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    setHasOptionsMenu(true)
    return inflater.inflate(R.layout.fragment_entries, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

    viewModel.snackbar.observe(viewLifecycleOwner, Observer { text ->
      text?.let {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
        viewModel.onSnackbarShown()
      }
    })

    val adapter = EntryAdapter(this)
    recyclerView.adapter = adapter

    DirectoryLiveData(requireContext().filesDir)
      .observe(viewLifecycleOwner, Observer { newList ->
      adapter.submitList(newList)
    })

    fab.setOnClickListener {
      findNavController().navigate(R.id.action_EntriesFragment_to_EditEntryFragment)
    }

    promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle("log key title")
      .setDescription("test")
      .setDeviceCredentialAllowed(true)
      .build()

    biometricPrompt = BiometricPrompt(
      this,
      ContextCompat.getMainExecutor(context),
      authenticationCallBack
    )
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.menu_main, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_key -> {
        handleLogKeyPressed()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun handleLogKeyPressed() {
    when (BiometricManager.from(context!!).canAuthenticate()) {
      BiometricManager.BIOMETRIC_SUCCESS -> {
        biometricPrompt.authenticate(promptInfo)
      }
      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
        editLogKey()
      }
    }
  }

  private fun editLogKey() {
    if (viewModel.getLogKey() == null) showSetLogKeyDialog() else showResetLogKeyDialog()
  }

  override fun onEntryClicked(entry: LogEntry) {
    if (viewModel.getLogKey() == null) {
      editEntry(entry)
    } else {
      buildLogKeyDialog {
        if (it == viewModel.getLogKey()) {
          editEntry(entry)
        } else {
          viewModel.showSnackbar(getString(R.string.error_incorrect_log_key))
        }
      }.show()
    }
  }

  private fun editEntry(entry: LogEntry) {
    findNavController().navigate(
      EntriesFragmentDirections.actionEntriesFragmentToEditEntryFragment(
        entry.stardate
      )
    )
  }

  private fun showSetLogKeyDialog() {
    buildLogKeyDialog(title = R.string.dialog_set_log_key_title) {
      viewModel.setLogKey(viewModel.getLogKey(), it)
    }.show()
  }

  private fun showResetLogKeyDialog() {
    buildResetLogKeyDialog { current, new ->
      viewModel.setLogKey(current, new)
    }.show()
  }

  private fun buildLogKeyDialog(
    @StringRes title: Int = R.string.dialog_log_key_title,
    onPositiveClicked: (value: String) -> Unit
  ): AlertDialog {
    val view = View.inflate(requireContext(), R.layout.alert_dialog_log_key_layout, null)
    val editTextView: EditText = view.findViewById(R.id.log_key_input_edit_text)

    return MaterialAlertDialogBuilder(requireContext())
      .setTitle(title)
      .setView(view)
      .setPositiveButton(R.string.dialog_log_key_positive_button) { _, _ ->
        onPositiveClicked(editTextView.text.toString())
      }
      .setNegativeButton(R.string.dialog_log_key_negative_button) { _, _ -> }
      .create()
  }

  private fun buildResetLogKeyDialog(
    @StringRes title: Int = R.string.dialog_new_log_key_title,
    onPositiveClicked: (current: String, new: String) -> Unit
  ): AlertDialog {
    val view = View.inflate(requireContext(), R.layout.alert_dialog_reset_log_key_layout, null)
    val logKeyEditTextView: EditText = view.findViewById(R.id.log_key_input_edit_text)
    val newLogKeyEditTextView: EditText = view.findViewById(R.id.new_log_key_input_edit_text)

    return MaterialAlertDialogBuilder(requireContext())
      .setTitle(title)
      .setView(view)
      .setPositiveButton(R.string.dialog_new_log_key_positive_button) { _, _ ->
        onPositiveClicked(
          logKeyEditTextView.text.toString(),
          newLogKeyEditTextView.text.toString()
        )
      }
      .setNegativeButton(R.string.dialog_new_log_key_negative_button) { _, _ -> }
      .create()
  }
}
