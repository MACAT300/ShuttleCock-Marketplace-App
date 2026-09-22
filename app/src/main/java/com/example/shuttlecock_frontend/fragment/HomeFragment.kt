package com.example.shuttlecock_frontend.fragment

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlecock_frontend.activity.MainHostActivity
import com.example.shuttlecock_frontend.activity.ProductDetailActivity
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.adapter.HomeProductAdapter
import com.example.shuttlecock_frontend.auth.RetrofitClient
import com.example.shuttlecock_frontend.data.UserSession
import com.example.shuttlecock_frontend.models.Product
import com.example.shuttlecock_frontend.models.cart.CartItem
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: HomeProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvUsername).text = UserSession.userName ?: "Player"
        view.findViewById<TextView>(R.id.tvWelcomeName).text =
            (UserSession.userName ?: "PLAYER").uppercase()

        // No onHeartClick passed here -> HomeProductAdapter hides the heart icon on this page.
        adapter = HomeProductAdapter(
            products = emptyList(),
            onCardClick = { product -> openDetail(product) }
        )

        val recycler = view.findViewById<RecyclerView>(R.id.featuredRecycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter

        view.findViewById<FrameLayout>(R.id.cartButton).setOnClickListener {
            (activity as? MainHostActivity)?.switchTab(R.id.nav_cart)
        }
    }

    override fun onResume() {
        super.onResume()
        loadFeaturedProducts()
        loadCartBadge()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadFeaturedProducts()
            loadCartBadge()
        }
    }

    private fun loadFeaturedProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getProducts()
                val products: List<Product> = response.body().orEmpty().take(4)
                adapter.updateList(products)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load products: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCartBadge() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val cartResponse = RetrofitClient.apiService.getOrCreateCart(
                    mapOf("userId" to UserSession.userId)
                )
                val cart = cartResponse.body() ?: return@launch
                val itemsResponse = RetrofitClient.apiService.getCartItems(cart.id)
                val items: List<CartItem> = itemsResponse.body().orEmpty()
                val badge = view?.findViewById<TextView>(R.id.cartBadge) ?: return@launch
                val count = items.sumOf { it.quantity }
                if (count > 0) {
                    badge.text = count.toString()
                    badge.visibility = View.VISIBLE
                } else {
                    badge.visibility = View.GONE
                }
            } catch (e: Exception) {
                // Non-critical: badge just won't show.
            }
        }
    }

    private fun openDetail(product: Product) {
        startActivity(ProductDetailActivity.newIntent(requireContext(), product.id))
    }
}