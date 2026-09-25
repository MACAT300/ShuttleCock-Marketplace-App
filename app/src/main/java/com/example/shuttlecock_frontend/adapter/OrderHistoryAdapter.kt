package com.example.shuttlecock_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.models.order.Order

class OrderHistoryAdapter(
    private var orders: List<Order>,
    private val onClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderId: TextView = view.findViewById(R.id.tvOrderId)
        val status: TextView = view.findViewById(R.id.tvOrderStatus)
        val date: TextView = view.findViewById(R.id.tvOrderDate)
        val total: TextView = view.findViewById(R.id.tvOrderTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.orderId.text = "Order #${order.id}"
        holder.status.text = order.status
        holder.date.text = ""
        holder.total.text = "RM %.2f".format(order.totalAmount)

        holder.itemView.setOnClickListener { onClick(order) }
    }

    override fun getItemCount(): Int = orders.size

    fun updateList(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}