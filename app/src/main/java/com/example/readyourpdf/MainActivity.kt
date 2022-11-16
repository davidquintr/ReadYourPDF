package com.example.readyourpdf

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.transaction

const val DOCUMENT_FRAGMENT_TAG = "com.example.android.actionopendocument.tags.DOCUMENT_FRAGMENT"

class MainActivity : AppCompatActivity() {

    private lateinit var noDocumentView: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noDocumentView = findViewById(R.id.no_document_view)
        findViewById<Button>(R.id.open_file).setOnClickListener {
            openDocumentPicker()
        }

        getSharedPreferences(TAG, Context.MODE_PRIVATE).let { sharedPreferences ->
            if (sharedPreferences.contains(LAST_OPENED_URI_KEY)) {
                val documentUri =
                    sharedPreferences.getString(LAST_OPENED_URI_KEY, null)?.toUri() ?: return@let
                openDocument(documentUri)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                AlertDialog.Builder(this)
                    .setMessage(R.string.intro_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                return true
            }
            R.id.action_open -> {
                openDocumentPicker()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { documentUri ->

                contentResolver.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                openDocument(documentUri)
            }
        }
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
    }

    private fun openDocument(documentUri: Uri) {
        getSharedPreferences(TAG, MODE_PRIVATE).edit {
            putString(LAST_OPENED_URI_KEY, documentUri.toString())
        }

        val fragment = ReaderFragment.newInstance(documentUri)
        supportFragmentManager.transaction {
            add(R.id.container, fragment, DOCUMENT_FRAGMENT_TAG)
        }

        // Document is open, so get rid of the call to action view.
        noDocumentView.visibility = View.GONE
    }
}

private const val OPEN_DOCUMENT_REQUEST_CODE = 0x33
private const val TAG = "MainActivity"
private const val LAST_OPENED_URI_KEY =
    "com.example.android.actionopendocument.pref.LAST_OPENED_URI_KEY"

