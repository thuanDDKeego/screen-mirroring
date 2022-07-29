package com.abc.mirroring.base

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, B : ViewBinding> : RecyclerView.Adapter<BaseViewHolder>() {
    protected val mItemList = ArrayList<T>()
    protected lateinit var binding: B
    val itemList get() = mItemList

    protected var mCurrentItem: T? = null
    val currentItem get() = mCurrentItem

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        Log.d("BaseAdapter", "run onCreateViewHolder")
        binding = setBinding(parent)
        return BaseViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    open fun setItemList(arrayList: ArrayList<T>) {
        mItemList.clear()
        mItemList.addAll(arrayList)
        Log.d("BaseAdapter", "setItemList $arrayList")
        notifyDataSetChanged()
    }

    fun addItem(item: T) {
        mItemList.add(item)
        notifyDataSetChanged()
    }

    fun clear() {
        mItemList.clear()
        notifyDataSetChanged()
    }

    abstract fun setBinding(viewGroup: ViewGroup): B

}