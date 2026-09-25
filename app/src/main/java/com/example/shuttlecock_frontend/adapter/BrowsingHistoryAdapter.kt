package com.example.shuttlecock_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.models.history.BrowsingHistoryItem

class BrowsingHistoryAdapter(
    private var items: List<BrowsingHistoryItem>,
    private val onClick: (BrowsingHistoryItem) -> Unit
) : RecyclerView.Adapter<BrowsingHistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgProduct)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val brand: TextView = view.findViewById(R.id.tvBrand)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val heart: ImageView = view.findViewById(R.id.btnHeart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favourite_row, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val entry = items[position]
        val product = entry.product

        holder.title.text = product?.name ?: "Product #${entry.productId}"
        holder.brand.text = product?.brand?.name ?: ""
        holder.price.text = "RM %.2f".format(product?.price ?: 0.0)
        holder.heart.visibility = View.GONE // 浏览历史不需要爱心按钮

        val imageUrl = product?.image?.url
        if (!imageUrl.isNullOrBlank()) {
            holder.img.load(imageUrl) {
                placeholder(R.drawable.image_background)
                error(R.drawable.image_background)
            }
        } else {
            holder.img.setImageResource(R.drawable.image_background)
        }

        holder.itemView.setOnClickListener { onClick(entry) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<BrowsingHistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}