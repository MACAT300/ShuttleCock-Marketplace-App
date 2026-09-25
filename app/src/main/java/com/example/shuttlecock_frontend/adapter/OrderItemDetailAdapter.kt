package com.example.shuttlecock_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.models.order.OrderItem

class OrderItemDetailAdapter(
    private var items: List<OrderItem>
) : RecyclerView.Adapter<OrderItemDetailAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgProduct)
        val name: TextView = view.findViewById(R.id.tvProductName)
        val qtyPrice: TextView = view.findViewById(R.id.tvQuantityPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_detail_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.product?.name ?: "Product #${item.productId}"
        holder.qtyPrice.text = "Qty: ${item.quantity} x RM %.2f".format(item.price)

        val imageUrl = item.product?.image?.url
        if (!imageUrl.isNullOrBlank()) {
            holder.img.load(imageUrl) {
                placeholder(R.drawable.image_background)
                error(R.drawable.image_background)
            }
        } else {
            holder.img.setImageResource(R.drawable.image_background)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<OrderItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}