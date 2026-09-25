package com.example.shuttlecock_frontend.activity

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
import com.example.shuttlecock_frontend.adapter.BrowsingHistoryAdapter
import com.example.shuttlecock_frontend.auth.RetrofitClient
import com.example.shuttlecock_frontend.data.UserSession
import kotlinx.coroutines.launch

class BrowsingHistoryActivity : AppCompatActivity() {

    private lateinit var adapter: BrowsingHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browsing_history)

        findViewById<FrameLayout>(R.id.backButton).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerHistory)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = BrowsingHistoryAdapter(emptyList()) { entry ->
            startActivity(ProductDetailActivity.newIntent(this, entry.productId))
        }
        recycler.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getBrowsingHistory(UserSession.userId)
                val history = response.body().orEmpty()
                adapter.updateList(history)

                val emptyState = findViewById<LinearLayout>(R.id.emptyState)
                val recycler = findViewById<RecyclerView>(R.id.recyclerHistory)
                if (history.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(this@BrowsingHistoryActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}