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

class ProductAdapter(
    private var products: List<Product>,
    private var favoritedIds: Set<Int> = emptySet(),
    private val onCardClick: (Product) -> Unit,
    private val onHeartClick: (Product, position: Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgProduct)
        val brandLabel: TextView = view.findViewById(R.id.tvBrandLabel)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val heart: ImageView = view.findViewById(R.id.btnHeart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_grid, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.title.text = product.name
        holder.brandLabel.text = product.brand?.name?.uppercase() ?: ""
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

        val isFavorited = favoritedIds.contains(product.id)
        holder.heart.setImageResource(
            if (isFavorited) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

        holder.itemView.setOnClickListener { onCardClick(product) }
        holder.heart.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) onHeartClick(products[pos], pos)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateList(newList: List<Product>, newFavoritedIds: Set<Int>) {
        products = newList
        favoritedIds = newFavoritedIds
        notifyDataSetChanged()
    }

    fun updateFavoritedIds(newFavoritedIds: Set<Int>) {
        favoritedIds = newFavoritedIds
        notifyDataSetChanged()
    }
}