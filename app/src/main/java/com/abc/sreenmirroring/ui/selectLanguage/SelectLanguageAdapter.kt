package com.abc.sreenmirroring.ui.selectLanguage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.databinding.ItemSelectLanguageBinding
import java.util.*

class SelectLanguageAdapter(
    private val languages: ArrayList<SelectLanguageViewState>,
    private val dLocale: Locale?
) :
    RecyclerView.Adapter<SelectLanguageAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: ItemSelectLanguageBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSelectLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    var itemClicked: ((SelectLanguageViewState) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(languages[position]) {
                binding.txtLanguage.text = this.language
                binding.llItem.setOnClickListener {
                    itemClicked?.invoke(this)
                }
                if (this.locale == dLocale.toString()) {
                    binding.btnCheckBox.setImageResource(R.drawable.ic_selected)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return languages.size
    }
}