package com.example.shuttlecock_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.models.Product

class FavouriteAdapter(
    private val products: MutableList<Product>,
    private val onCardClick: (Product) -> Unit,
    private val onRemoveClick: (Product, position: Int) -> Unit
) : RecyclerView.Adapter<FavouriteAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgProduct)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val brand: TextView = view.findViewById(R.id.tvBrand)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val heart: ImageView = view.findViewById(R.id.btnHeart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favourite_row, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val product = products[position]

        holder.title.text = product.name
        holder.brand.text = product.brand?.name ?: ""
        holder.price.text = "RM %.2f".format(product.price)

        val imageUrl = product.image?.url
        if (!imageUrl.isNullOrBlank()) {
            holder.img.load(imageUrl) {
                placeholder(R.drawable.image_background)
                error(R.drawable.image_background)
            }
        } else {
            holder.img.setImageResource(R.drawable.image_background)
        }

        holder.itemView.setOnClickListener { onCardClick(product) }
        holder.heart.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) onRemoveClick(products[pos], pos)
        }
    }

    override fun getItemCount(): Int = products.size

    fun replaceAll(newList: List<Product>) {
        products.clear()
        products.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        if (position in products.indices) {
            products.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}