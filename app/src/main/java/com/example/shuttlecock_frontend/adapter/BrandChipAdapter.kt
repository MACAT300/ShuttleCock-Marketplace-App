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

        val params = holder.logo.layoutParams
        if (isAll) {
            params.width = dpToPx(holder.itemView.context, 30)
            params.height = dpToPx(holder.itemView.context, 30)
            holder.logo.layoutParams = params
            holder.logo.load(R.drawable.ic_all_brands)
        } else {
            params.width = dpToPx(holder.itemView.context, 30)
            params.height = dpToPx(holder.itemView.context, 30)
            holder.logo.layoutParams = params
            val logoUrl = brand?.image?.url
            if (!logoUrl.isNullOrBlank()) {
                holder.logo.load(logoUrl) {
                    placeholder(R.drawable.bg_circle_plain)
                    error(R.drawable.bg_circle_plain)
                }
            } else {
                holder.logo.load(R.drawable.bg_circle_plain)
            }
        }

        // 选中的那张卡片背景变绿，文字/图标要跟着变成深色才看得清
        holder.root.isSelected = isSelected
        val textColor = if (isSelected) R.color.bg_dark else R.color.white
        holder.name.setTextColor(holder.itemView.context.getColor(textColor))

        // "All"的图标是纯色图案，要跟着背景切换深浅，不然深色背景下会看不清
        if (isAll) {
            holder.logo.setColorFilter(holder.itemView.context.getColor(textColor))
        } else {
            holder.logo.clearColorFilter() // 品牌logo本身是彩色的，不需要染色
        }

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

    private fun dpToPx(context: android.content.Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}