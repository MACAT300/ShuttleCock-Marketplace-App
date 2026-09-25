package com.example.shuttlecock_frontend.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.adapter.OrderHistoryAdapter
import com.example.shuttlecock_frontend.auth.RetrofitClient
import com.example.shuttlecock_frontend.data.UserSession
import kotlinx.coroutines.launch

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var adapter: OrderHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        findViewById<FrameLayout>(R.id.backButton).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerOrders)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = OrderHistoryAdapter(emptyList()) { order ->
            startActivity(
                Intent(this, OrderDetailActivity::class.java)
                    .putExtra("orderId", order.id)
            )
        }
        recycler.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getOrders(UserSession.userId)
                val orders = response.body().orEmpty().sortedByDescending { it.id }
                adapter.updateList(orders)

                val emptyState = findViewById<LinearLayout>(R.id.emptyState)
                val recycler = findViewById<RecyclerView>(R.id.recyclerOrders)
                if (orders.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(this@OrderHistoryActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}