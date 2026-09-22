package com.example.shuttlecock_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.models.Brand

class BrandChipAdapter(
    private var brands: List<Brand>,
    private var selectedBrandId: Int? = null, // null == "All"
    private val onBrandSelected: (Brand?) -> Unit
) : RecyclerView.Adapter<BrandChipAdapter.ChipViewHolder>() {

    class ChipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: LinearLayout = view.findViewById(R.id.chipRoot)
        val logo: ImageView = view.findViewById(R.id.imgBrandLogo)
        val name: TextView = view.findViewById(R.id.tvBrandName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brand_chip_dark, parent, false)
        return ChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val isAll = position == 0
        val brand = if (isAll) null else brands[position - 1]
        val isSelected = if (isAll) selectedBrandId == null else brand?.id == selectedBrandId

        holder.name.text = if (isAll) "All" else brand?.name ?: ""

        if (isAll) {
            holder.logo.setImageResource(R.drawable.ic_all_brands)
        } else {
            val logoUrl = brand?.image?.url
            if (!logoUrl.isNullOrBlank()) {
                holder.logo.load(logoUrl) {
                    placeholder(R.drawable.bg_circle_plain)
                    error(R.drawable.bg_circle_plain)
                }
            } else {
                holder.logo.setImageDrawable(null)
            }
        }

        // 选中的那张卡片背景变绿，文字/图标要跟着变成深色才看得清
        holder.root.isSelected = isSelected
        val textColor = if (isSelected) R.color.bg_dark else R.color.white
        holder.name.setTextColor(holder.itemView.context.getColor(textColor))

        holder.root.setOnClickListener {
            selectedBrandId = brand?.id
            notifyDataSetChanged()
            onBrandSelected(brand)
        }
    }

    override fun getItemCount(): Int = brands.size + 1

    fun updateBrands(newBrands: List<Brand>) {
        brands = newBrands
        notifyDataSetChanged()
    }
}