package com.example.shuttlecock_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.models.cart.CartItem

/**
 * Cart screen adapter. Backed by real CartItem objects fetched from the API
 * (each carries its nested `product`). All mutations (quantity change / delete)
 * are delegated back to OrderActivity via callbacks, which perform the actual
 * network calls and then tell this adapter how to update itself.
 */
class OrderAdapter(
    private val list: MutableList<CartItem>,
    private val onQuantityChange: (item: CartItem, newQuantity: Int, position: Int) -> Unit,
    private val onDelete: (item: CartItem, position: Int) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgProduct)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val qty: TextView = view.findViewById(R.id.quantity_item)
        val delete: ImageButton = view.findViewById(R.id.delete_button)
        val plus: ImageButton = view.findViewById(R.id.btnPlus)
        val minus: ImageButton = view.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_dark, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = list[position]
        val product = item.product

        holder.title.text = product?.name ?: "Product #${item.productId}"
        holder.description.text = product?.description ?: ""
        holder.price.text = "RM %.2f".format(product?.price ?: 0.0)
        holder.qty.text = item.quantity.toString()

        val imageUrl = product?.image?.url
        if (!imageUrl.isNullOrBlank()) {
            holder.img.load(imageUrl) {
                placeholder(R.drawable.image_background)
                error(R.drawable.image_background)
            }
        } else {
            holder.img.setImageResource(R.drawable.image_background)
        }

        holder.plus.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            val current = list[pos]
            onQuantityChange(current, current.quantity + 1, pos)
        }

        holder.minus.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            val current = list[pos]
            if (current.quantity > 1) {
                onQuantityChange(current, current.quantity - 1, pos)
            }
        }

        holder.delete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            onDelete(list[pos], pos)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateItemQuantity(position: Int, newItem: CartItem) {
        if (position in list.indices) {
            list[position] = newItem
            notifyItemChanged(position)
        }
    }

    fun removeAt(position: Int) {
        if (position in list.indices) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun replaceAll(newList: List<CartItem>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
