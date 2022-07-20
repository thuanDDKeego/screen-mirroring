package com.abc.sreenmirroring.ui.tutorial.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.base.BaseAdapter
import com.abc.sreenmirroring.base.BaseViewHolder
import com.abc.sreenmirroring.data.model.FAQItem
import com.abc.sreenmirroring.databinding.LayoutItemFaqBinding

class FAQItemAdapter : BaseAdapter<FAQItem, LayoutItemFaqBinding>() {

    override fun setBinding(parent: ViewGroup) = LayoutItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = mItemList[position]
        val titleFAQ = item.title
        val descriptionFAQ = item.description
        (binding as LayoutItemFaqBinding).txtTitleFAQ.text = titleFAQ
        binding.expandTextView.text = descriptionFAQ
      }

}