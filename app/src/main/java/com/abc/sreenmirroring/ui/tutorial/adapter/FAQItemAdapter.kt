package com.abc.sreenmirroring.ui.tutorial.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abc.sreenmirroring.data.model.FAQItem
import com.abc.sreenmirroring.databinding.LayoutItemFaqBinding

class FAQItemAdapter(private val listFAQItem: ArrayList<FAQItem>) :
    RecyclerView.Adapter<FAQItemAdapter.BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemBinding =
            LayoutItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        with(holder) {
            with(listFAQItem[position]) {
                itemBinding.txtTitleFAQ.text = title
                itemBinding.expandTextView.text = description
            }
        }
    }

    override fun getItemCount(): Int = listFAQItem.size

    inner class BaseViewHolder(val itemBinding: LayoutItemFaqBinding) :
        RecyclerView.ViewHolder(itemBinding.root)
}