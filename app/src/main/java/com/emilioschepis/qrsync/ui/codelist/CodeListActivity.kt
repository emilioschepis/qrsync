package com.emilioschepis.qrsync.ui.codelist

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.model.QSCode
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.ui.detail.DetailActivity
import com.emilioschepis.qrsync.ui.preferences.PreferencesActivity
import com.emilioschepis.qrsync.ui.scan.ScanActivity
import org.koin.android.architecture.ext.viewModel

class CodeListActivity : AppCompatActivity() {

    private val viewModel by viewModel<CodeListViewModel>()
    private val codeListAdapter = CodeListAdapter(this::onListItemClicked)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_list)

        findViewById<FloatingActionButton>(R.id.code_list_scan_fab)
                .run {
                    setOnClickListener {
                        val intent = Intent(this@CodeListActivity, ScanActivity::class.java)
                        startActivity(intent)
                    }
                }

        findViewById<RecyclerView>(R.id.code_list_main_rev)
                .run {
                    // Initialize layoutManager and decorations
                    val layoutManager = LinearLayoutManager(this@CodeListActivity)
                    val decoration = DividerItemDecoration(this@CodeListActivity, layoutManager.orientation)
                    val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {

                        // The scroll is only necessary if new items are added as
                        // newly added items are usually positioned on top.
                        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                            layoutManager.scrollToPosition(positionStart)
                            super.onItemRangeChanged(positionStart, itemCount)
                        }
                    }
                    codeListAdapter.registerAdapterDataObserver(adapterDataObserver)

                    // The cells' height does not change
                    setHasFixedSize(true)
                    addItemDecoration(decoration)
                    setLayoutManager(layoutManager)
                    adapter = codeListAdapter
                }

        viewModel.loading.observe(this, Observer {
            it?.let {
                val progressBar = findViewById<ProgressBar>(R.id.code_list_loading_prb)
                if (it) {
                    codeListAdapter.submitList(emptyList())
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        })

        viewModel.collection.observe(this, Observer {
            it?.fold(this::onCollectionRetrievalError, this::onCollectionRetrievalSuccess)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, PreferencesActivity::class.java))
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun onCollectionRetrievalError(error: QSError) {
        codeListAdapter.submitList(emptyList())
        snackbarMessage(getString(error.resId, error.params.getOrNull(0))).show()
    }

    private fun onCollectionRetrievalSuccess(collection: List<QSCode>?) {
        codeListAdapter.submitList(collection)
    }

    private fun onListItemClicked(code: QSCode) {
        startActivity(Intent(this, DetailActivity::class.java)
                .apply { putExtra("code_id", code.id) })
    }

    private fun snackbarMessage(message: String,
                                duration: Int = Snackbar.LENGTH_INDEFINITE,
                                callback: (() -> Unit)? = null): Snackbar {
        val root = findViewById<CoordinatorLayout>(R.id.code_list_root_cdl)
        val snackbar = Snackbar.make(root, message, duration)

        if (duration == Snackbar.LENGTH_INDEFINITE) {
            snackbar.setAction(android.R.string.ok) { callback?.invoke() }
        }

        return snackbar
    }
}
