package com.abc.mirroring.ui.tutorial.adapter

import AdType
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.data.model.FAQItem
import com.abc.mirroring.databinding.LayoutAdContainerBinding
import com.abc.mirroring.databinding.LayoutItemFaqBinding
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking

class FAQItemAdapter(
    private val context: AppCompatActivity,
    private val listFAQItem: ArrayList<FAQItem>,
    private val hasAds: Boolean = true
) :
    RecyclerView.Adapter<FAQItemAdapter.BaseViewHolder>() {

    lateinit var admobHelper: AdmobHelper
    override fun getItemViewType(position: Int): Int {
        return if (position == 1 && hasAds) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemBinding = if (viewType == 0) {
            LayoutItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        } else {
            LayoutAdContainerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        }
        return BaseViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        with(holder) {
            if (position != 1 || !hasAds) {
                with(listFAQItem[position]) {
                    var expanded = false
                    itemBinding as LayoutItemFaqBinding
                    itemBinding.root.setOnClickListener {
                        if (!expanded) {
                            val params = Bundle().also {
                                it.putString(
                                    "FAQ_QUESTION",
                                    listFAQItem[position].title
                                )
                            }
                            FirebaseTracking.log(FirebaseLogEvent.FAQ_Click_Question, params)
                            itemBinding.imgExpandCollapse.setImageResource(R.drawable.ic_collapse)
                            itemBinding.expandTextView.visibility = View.VISIBLE
                        } else {
                            itemBinding.imgExpandCollapse.setImageResource(R.drawable.ic_expand)
                            itemBinding.expandTextView.visibility = View.GONE
                        }
                        expanded = !expanded
                    }
                    itemBinding.txtTitleFAQ.text = title
                    itemBinding.expandTextView.text = description
                }
            } else {
                itemBinding as LayoutAdContainerBinding
                admobHelper.showNativeAdmob(
                    context,
                    AdType.FAQ_NATIVE,
                    itemBinding.nativeAdView,
                    true
                )
            }
        }
    }

    override fun getItemCount(): Int = listFAQItem.size

    inner class BaseViewHolder(val itemBinding: ViewBinding) :
        RecyclerView.ViewHolder(itemBinding.root)
}