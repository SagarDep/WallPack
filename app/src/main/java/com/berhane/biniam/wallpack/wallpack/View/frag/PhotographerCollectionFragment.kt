/*
 * DayTime:10/10/18 10:40 AM :
 * Year:2018 :
 * Author:bini :
 */

package com.berhane.biniam.wallpack.wallpack.View.frag

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.berhane.biniam.wallpack.wallpack.R
import com.berhane.biniam.wallpack.wallpack.model.View.WallPackViewModel
import com.berhane.biniam.wallpack.wallpack.model.data.PhotoCollection
import com.berhane.biniam.wallpack.wallpack.model.data.Photos
import com.berhane.biniam.wallpack.wallpack.utils.EndlessRecyclerViewScrollListener
import com.berhane.biniam.wallpack.wallpack.utils.PhotoConstants
import com.berhane.biniam.wallpack.wallpack.utils.adapter.CollectionAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.photographer_fragment.*


class PhotographerCollectionFragment : Fragment() {

    private var pageNumber: Int = 1
    private var currentPage: Int = 0
    private var totalPage: Int = 0
    private lateinit var viewModel: WallPackViewModel
    private lateinit var mRecyclerView: RecyclerView
    private var collectionAdapter: CollectionAdapter? = null
    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    private var progress_layout = photographer_progress
    private val TAG: String = "PhotographerFrag"


    companion object {
        fun newInstance(photographer: Photos) = PhotographerCollectionFragment().apply {
            val args = Bundle()
            args.putParcelable("photographerCollection", photographer)
            val fragment = PhotographerCollectionFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "OnCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.photographer_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(WallPackViewModel::class.java)
        progress_layout = rootView.findViewById(R.id.photographer_progress)
        mRecyclerView = rootView.findViewById(R.id.photographer_photos_recycler)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated" + "Photographer Activity Already Started!!!!")
        initPhotographerPhotos()
    }

    private fun initPhotographerPhotos() {
        //Add which views you don't want to hide. In this case don't hide the toolbar
        mRecyclerView.itemAnimator = DefaultItemAnimator()
        var linearLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        mRecyclerView.layoutManager = linearLayoutManager
        // Ids to show while the loading view is showing
        excludeViewWhileLoading()
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                progress_layout.showContent()
                currentPage = page
                totalPage = totalItemsCount
                excludeViewWhileLoading()
                ++pageNumber
                loadPhotographerPhotos(true)
            }
        }
        // Adds the scroll listener to RecyclerView
        mRecyclerView.addOnScrollListener(scrollListener as EndlessRecyclerViewScrollListener)
    }

    override fun onResume() {
        super.onResume()
        loadPhotographerPhotos(false)
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        Glide.with(this@PhotographerCollectionFragment).resumeRequests()
                    }
                    else -> {
                        Glide.with(this@PhotographerCollectionFragment).pauseRequests()
                    }
                }
            }
        })
    }

    /**
     * Will Load all the Collections ,Likes and Photos of the Given Photographer
     */
    private fun loadPhotographerPhotos(loadMore: Boolean) {
        val args = arguments
        var photographerCol: Photos? = args!!.getParcelable("photographerCollection")
        viewModel.getPhotographerCollection(photographerCol!!.user, pageNumber, PhotoConstants.PERPAGE)!!.observe(this@PhotographerCollectionFragment,
                Observer<List<PhotoCollection>> { t: List<PhotoCollection>? ->
                    if (collectionAdapter == null) {
                        collectionAdapter = CollectionAdapter((t as MutableList<PhotoCollection>?)!!, activity as Activity)
                        mRecyclerView.adapter = collectionAdapter
                    } else {
                        if (loadMore) {
                            collectionAdapter!!.addCollectionPhotos(t!!)
                            mRecyclerView.recycledViewPool.clear()
                        } else {
                            collectionAdapter!!.setImageInfo(t!!)
                            mRecyclerView.recycledViewPool.clear()
                        }
                    }
                })
    }

    /**
     * excluding some views while showing the  loading progress bar
     */
    private fun excludeViewWhileLoading() {
        var viewId = arrayListOf<Int>()
        viewId.add(R.id.photographer_photos_recycler)
        viewId.add(R.id.photographer_viewpager)
        progress_layout.showLoading(viewId)
    }

    /**
     *
     */
    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach()
        collectionAdapter = null
        scrollListener!!.resetState()
    }

    /**
     *
     */
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        if (mRecyclerView != null) {
            scrollListener!!.resetState()
        }
    }

}