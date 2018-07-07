package com.emilioschepis.qrsync.ui.detail

import android.arch.lifecycle.Observer
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.extension.*
import com.emilioschepis.qrsync.model.QSCode
import com.emilioschepis.qrsync.model.QSCodeAction
import com.emilioschepis.qrsync.model.QSError
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.DateFormat

class DetailActivity : AppCompatActivity() {
    private val viewModel by viewModel<DetailViewModel> { parametersOf(intent.getStringExtra("code_id")) }
    private val actionListAdapter = DetailActionAdapter(this::onActionSelected)

    private val root by lazy { findViewById<CoordinatorLayout>(R.id.detail_root_cdl) }
    private val actionsRev by lazy { findViewById<RecyclerView>(R.id.detail_actions_rev) }
    private val contentTev by lazy { findViewById<TextView>(R.id.detail_content_tev) }
    private val progressBar by lazy { findViewById<ProgressBar>(R.id.detail_loading_prb) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        actionsRev.run {
            // Initialize layoutManager and decorations
            val layoutManager = LinearLayoutManager(this@DetailActivity)
            val decoration = DividerItemDecoration(this@DetailActivity, layoutManager.orientation)
            val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {

                // The scroll is only necessary if new items are added as
                // newly added items are usually positioned on top.
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    layoutManager.scrollToPosition(positionStart)
                    super.onItemRangeChanged(positionStart, itemCount)
                }
            }
            actionListAdapter.registerAdapterDataObserver(adapterDataObserver)

            // The cells' height does not change
            setHasFixedSize(true)
            addItemDecoration(decoration)
            setLayoutManager(layoutManager)
            adapter = actionListAdapter
        }

        viewModel.loading.observe(this, Observer {
            it?.let {
                if (it) {
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        })

        viewModel.code.observe(this, Observer {
            it?.fold(this::onCodeRetrievalError, this::onCodeRetrievalSuccess)
        })

        viewModel.actions.observe(this, Observer { onActionsChanged(it) })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onActionSelected(action: QSCodeAction) {
        when (action) {
            is QSCodeAction.Delete -> {
                val title = getString(R.string.dialog_title_confirmation)
                val message = getString(R.string.question_delete_code)

                confirmationDialog(title, message) {
                    // When we delete a code we don't want to observe its changes
                    viewModel.code.removeObservers(this)
                    viewModel.deleteCode().observe(this, Observer {
                        it?.fold(this::onCodeDeletionSuccess,
                                this::onCodeDeletionError)
                    })
                }.show()
            }
            is QSCodeAction.EditTitle -> {
                val title = R.string.dialog_title_title_edit
                val start = viewModel.currentCode.title

                editableDialog(title, start) {
                    viewModel.editTitle(it).observe(this, Observer {
                        it?.fold(this::onCodeUpdateSuccess, this::onCodeUpdateError)
                    })
                }.show()
            }
            is QSCodeAction.CopyContent -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("QSCode.Content", viewModel.currentCode.content)
                clipboard.primaryClip = clip
                snackbarMessage(root, getString(R.string.info_copied_content))
            }
            is QSCodeAction.ReadInfo -> {
                dialog(message = viewModel.currentCode.infoText)
                        .apply {
                            neutralPressed(R.string.action_copy_id) {
                                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("QSCode.Id", viewModel.currentCode.id)
                                clipboard.primaryClip = clip
                                snackbarMessage(root, getString(R.string.info_copied_id))
                            }
                        }.show()
            }
            else -> {
                try {
                    startActivity(action.intent?.invoke(viewModel.currentCode))
                } catch (ex: ActivityNotFoundException) {
                    snackbarMessage(root, getString(R.string.error_activity_not_found))
                }
            }
        }
    }

    private fun onCodeDeletionError(error: QSError) {
        snackbarError(root, error)
    }

    private fun onCodeDeletionSuccess() {
        finish()
    }

    private fun onCodeUpdateError(error: QSError) {
        snackbarError(root, error)
    }

    private fun onCodeUpdateSuccess() {

    }

    private fun onCodeRetrievalError(error: QSError) {
        // Checking for active observers makes sure that no error
        // is showed when dealing with a deleted code
        if (viewModel.code.hasActiveObservers()) {
            snackbarError(root, error) { finish() }
        }
    }

    private fun onCodeRetrievalSuccess(code: QSCode) {
        viewModel.currentCode = code

        supportActionBar?.title =
                if (code.title.isBlank()) getString(R.string.placeholder_no_title) else code.title

        contentTev.text = code.content
    }

    private fun onActionsChanged(actions: List<QSCodeAction>?) {
        actionListAdapter.submitList(actions)
    }

    private val QSCode.infoText: String
        get() {
            val formattedTime = DateFormat.getDateTimeInstance().format(this.timestamp.toDate())
            val created = "${getString(R.string.header_created)}: $formattedTime"
            val type = "${getString(R.string.header_type)}: ${this.type}"
            val id = "${getString(R.string.header_id)}: ${this.id}"

            return "$created\n$type\n$id"
        }
}
