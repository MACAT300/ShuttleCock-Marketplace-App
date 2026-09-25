package com.example.shuttlecock_frontend.activity

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.adapter.OrderItemDetailAdapter
import com.example.shuttlecock_frontend.auth.RetrofitClient
import kotlinx.coroutines.launch

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var adapter: OrderItemDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        val orderId = intent.getIntExtra("orderId", -1)
        if (orderId == -1) {
            finish()
            return
        }

        findViewById<TextView>(R.id.tvOrderTitle).text = "ORDER #$orderId"
        findViewById<FrameLayout>(R.id.backButton).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerOrderItems)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = OrderItemDetailAdapter(emptyList())
        recycler.adapter = adapter

        loadOrderDetail(orderId)
    }

    private fun loadOrderDetail(orderId: Int) {
        lifecycleScope.launch {
            try {
                val orderResponse = RetrofitClient.apiService.getOrderById(orderId)
                val order = orderResponse.body()
                if (order != null) {
                    findViewById<TextView>(R.id.tvOrderDetailTotal).text = "RM %.2f".format(order.totalAmount)
                }

                val itemsResponse = RetrofitClient.apiService.getOrderItems(orderId)
                adapter.updateList(itemsResponse.body().orEmpty())
            } catch (e: Exception) {
                Toast.makeText(this@OrderDetailActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}