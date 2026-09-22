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

class HomeProductAdapter(
    private var products: List<Product>,
    private var favoritedIds: Set<Int> = emptySet(),
    private val onCardClick: (Product) -> Unit,
    private val onHeartClick: ((Product, position: Int) -> Unit)? = null
) : RecyclerView.Adapter<HomeProductAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgProduct)
        val heart: ImageView = view.findViewById(R.id.btnHeart)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val brand: TextView = view.findViewById(R.id.tvBrand)
        val price: TextView = view.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_home, parent, false)
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

        if (onHeartClick == null) {
            // Home page: no favorite feature here, hide the heart entirely.
            holder.heart.visibility = View.GONE
        } else {
            holder.heart.visibility = View.VISIBLE
            val isFavorited = favoritedIds.contains(product.id)
            holder.heart.setImageResource(
                if (isFavorited) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
            holder.heart.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) onHeartClick.invoke(products[pos], pos)
            }
        }

        holder.itemView.setOnClickListener { onCardClick(product) }
    }

    override fun getItemCount(): Int = products.size

    fun updateList(newList: List<Product>, newFavoritedIds: Set<Int> = favoritedIds) {
        products = newList
        favoritedIds = newFavoritedIds
        notifyDataSetChanged()
    }

    fun updateFavoritedIds(newFavoritedIds: Set<Int>) {
        favoritedIds = newFavoritedIds
        notifyDataSetChanged()
    }
}