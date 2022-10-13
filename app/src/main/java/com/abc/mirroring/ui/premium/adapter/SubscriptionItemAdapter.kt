package com.abc.mirroring.ui.premium.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abc.mirroring.data.model.DeviceItem
import com.abc.mirroring.data.model.SubscriptionItem
import com.abc.mirroring.databinding.LayoutItemDeviceBinding
import com.abc.mirroring.databinding.LayoutItemSubscriptionBinding

class SubscriptionItemAdapter(
    private val context: Context,
    private val listDeviceItem: ArrayList<SubscriptionItem>
) :
    RecyclerView.Adapter<SubscriptionItemAdapter.BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemBinding =
            LayoutItemSubscriptionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return BaseViewHolder(itemBinding)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        with(holder) {
            with(listDeviceItem[position]) {
                itemBinding.apply {
                    txtLabel.text = listDeviceItem[position].label
                    txtDescription.text = listDeviceItem[position].description
                }
            }
        }
    }

    override fun getItemCount(): Int = listDeviceItem.size

    inner class BaseViewHolder(val itemBinding: LayoutItemSubscriptionBinding) :
        RecyclerView.ViewHolder(itemBinding.root)
}