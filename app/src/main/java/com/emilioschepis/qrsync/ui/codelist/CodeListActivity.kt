package com.emilioschepis.qrsync.ui.codelist

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.extension.snackbarError
import com.emilioschepis.qrsync.extension.snackbarMessage
import com.emilioschepis.qrsync.model.QSCode
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.ui.detail.DetailActivity
import com.emilioschepis.qrsync.ui.preferences.PreferencesActivity
import com.emilioschepis.qrsync.ui.scan.GalleryActivity
import com.emilioschepis.qrsync.ui.scan.ScanActivity
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt


class CodeListActivity : AppCompatActivity() {

    private val viewModel by viewModel<CodeListViewModel>()
    private val codeListAdapter = CodeListAdapter(this::onListItemClicked)

    private val root by lazy { findViewById<CoordinatorLayout>(R.id.code_list_root_cdl) }
    private val codesRev by lazy { findViewById<RecyclerView>(R.id.code_list_main_rev) }
    private val scanMenuFab by lazy { findViewById<FloatingActionMenu>(R.id.code_list_scan_fab) }
    private val scanFromCameraFab by lazy { findViewById<FloatingActionButton>(R.id.code_list_scan_camera_fab) }
    private val scanFromGalleryFab by lazy { findViewById<FloatingActionButton>(R.id.code_list_scan_gallery_fab) }
    private val progressBar by lazy { findViewById<ProgressBar>(R.id.code_list_loading_prb) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_list)

        scanFromCameraFab.run {
            setOnClickListener {
                val intent = Intent(this@CodeListActivity, ScanActivity::class.java)
                startActivity(intent)
                scanMenuFab.close(true)
            }
        }

        scanFromGalleryFab.run {
            setOnClickListener {
                val intent = Intent(this@CodeListActivity, GalleryActivity::class.java)
                startActivity(intent)
                scanMenuFab.close(true)
            }
        }

        codesRev.run {
            // Initialize layoutManager and decorations
            val layoutManager = LinearLayoutManager(this@CodeListActivity)

            // Find how many items fit on the screen at once
            val displayMetrics = context.resources.displayMetrics
            val dpHeight = displayMetrics.heightPixels / displayMetrics.density
            // Approximately the height of a code item
            val itemHeight = 8 + 51 + 8 + 51 + 8

            layoutManager.isItemPrefetchEnabled = true
            layoutManager.initialPrefetchItemCount = (dpHeight / itemHeight).roundToInt()

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
        snackbarError(root, error)
    }

    private fun onCollectionRetrievalSuccess(collection: List<QSCode>?) {
        codeListAdapter.submitList(collection)
    }

    private fun onListItemClicked(id: String) {
        scanMenuFab.close(true)
        startActivity(Intent(this, DetailActivity::class.java)
                .apply { putExtra("code_id", id) })
    }
}
