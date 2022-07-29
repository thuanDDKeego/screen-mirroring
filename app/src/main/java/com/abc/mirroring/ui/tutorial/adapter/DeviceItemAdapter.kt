package com.abc.mirroring.ui.tutorial.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abc.mirroring.data.model.DeviceItem
import com.abc.mirroring.databinding.LayoutItemDeviceBinding

class DeviceItemAdapter(
    private val context: Context,
    private val listDeviceItem: ArrayList<DeviceItem>
) :
    RecyclerView.Adapter<DeviceItemAdapter.BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemBinding =
            LayoutItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(itemBinding)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        with(holder) {
            with(listDeviceItem[position]) {
                itemBinding.txtCategoryTitle.text = this.categoryTitle
                itemBinding.txtCategoryContent.text = this.categoryContent
                itemBinding.imgDevice.setImageDrawable(context.getDrawable(this.imgDevice))
            }
        }
    }

    override fun getItemCount(): Int = listDeviceItem.size

    inner class BaseViewHolder(val itemBinding: LayoutItemDeviceBinding) :
        RecyclerView.ViewHolder(itemBinding.root)
}