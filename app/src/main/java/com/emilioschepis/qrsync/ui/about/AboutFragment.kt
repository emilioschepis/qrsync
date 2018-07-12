package com.emilioschepis.qrsync.ui.about

import android.arch.lifecycle.Observer
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.model.QSHandle
import org.koin.android.viewmodel.ext.android.viewModel

class AboutFragment : Fragment() {

    companion object {
        fun newInstance() = AboutFragment()
    }

    private val viewModel by viewModel<AboutViewModel>()
    private val handleListAdapter = AboutHandleAdapter(this::onHandleSelected)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textTev = view.findViewById<TextView>(R.id.about_general_info_tev)
        val handlesRev = view.findViewById<RecyclerView>(R.id.about_handle_rev)

        handlesRev.run {
            // Initialize layoutManager and decorations
            val layoutManager = LinearLayoutManager(activity)

            // We don't need to calculate how many actions fit on screen,
            // as they will always be a set amount.
            layoutManager.isItemPrefetchEnabled = true
            layoutManager.initialPrefetchItemCount = 2

            val decoration = DividerItemDecoration(activity, layoutManager.orientation)
            val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {

                // The scroll is only necessary if new items are added as
                // newly added items are usually positioned on top.
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    layoutManager.scrollToPosition(positionStart)
                    super.onItemRangeChanged(positionStart, itemCount)
                }
            }
            handleListAdapter.registerAdapterDataObserver(adapterDataObserver)

            // The cells' height does not change
            setHasFixedSize(true)
            addItemDecoration(decoration)
            setLayoutManager(layoutManager)
            adapter = handleListAdapter
        }

        viewModel.generalInfo.observe(this, Observer {
            textTev.text = it
        })

        viewModel.handles.observe(this, Observer {
            Log.d("TAG", "Observed handles: $it")
            handleListAdapter.submitList(it)
        })
    }

    private fun onHandleSelected(handle: QSHandle) {
        val uri = when (handle) {
            QSHandle.Twitter -> viewModel.twitterHandle
            QSHandle.Instagram -> viewModel.instagramHandle
            QSHandle.GitHub -> viewModel.githubHandle
            QSHandle.LinkedIn -> viewModel.linkedInHandle
            QSHandle.PlayStore -> viewModel.playStoreHandle
        }

        try {
            startActivity(Intent(Intent.ACTION_VIEW).apply { data = uri })
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this.activity, R.string.error_activity_not_found, Toast.LENGTH_SHORT).show()
        }
    }
}
