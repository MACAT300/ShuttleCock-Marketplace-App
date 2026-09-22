package com.example.shuttlecock_frontend.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlecock_frontend.activity.ProductDetailActivity
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.adapter.BrandChipAdapter
import com.example.shuttlecock_frontend.adapter.ProductAdapter
import com.example.shuttlecock_frontend.auth.RetrofitClient
import com.example.shuttlecock_frontend.data.UserSession
import com.example.shuttlecock_frontend.models.Brand
import com.example.shuttlecock_frontend.models.Product
import kotlinx.coroutines.launch

class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private lateinit var productAdapter: ProductAdapter
    private lateinit var brandAdapter: BrandChipAdapter

    private var selectedBrandId: Int? = null
    private var searchQuery: String = ""
    private var favoritedIds: MutableSet<Int> = mutableSetOf()

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductAdapter(
            products = emptyList(),
            onCardClick = { product -> openDetail(product) },
            onHeartClick = { product, position -> toggleFavorite(product, position) }
        )
        val productRecycler = view.findViewById<RecyclerView>(R.id.productRecycler)
        productRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        productRecycler.adapter = productAdapter

        brandAdapter = BrandChipAdapter(emptyList()) { brand ->
            selectedBrandId = brand?.id
            fetchProducts()
        }
        val brandRecycler = view.findViewById<RecyclerView>(R.id.brandFilterRecycler)
        brandRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        brandRecycler.adapter = brandAdapter

        view.findViewById<EditText>(R.id.searchEditText).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable { fetchProducts() }
                searchHandler.postDelayed(searchRunnable!!, 300)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fetchBrands()
        fetchProducts()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadFavorites()
        }
    }

    private fun fetchBrands() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getBrands()
                val brands: List<Brand> = response.body().orEmpty()
                brandAdapter.updateBrands(brands)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load brands: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getProducts(
                    name = searchQuery.trim().ifBlank { null },
                    brandId = selectedBrandId
                )
                val products: List<Product> = response.body().orEmpty()
                productAdapter.updateList(products, favoritedIds)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load products: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getFavorites(UserSession.userId)
                favoritedIds = response.body().orEmpty().map { it.productId }.toMutableSet()
                productAdapter.updateFavoritedIds(favoritedIds)
            } catch (e: Exception) {
                // Non-critical: heart state just won't be pre-filled.
            }
        }
    }

    private fun toggleFavorite(product: Product, position: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (favoritedIds.contains(product.id)) {
                    val response = RetrofitClient.apiService.removeFavorite(UserSession.userId, product.id)
                    if (!response.isSuccessful) {
                        Toast.makeText(requireContext(), "Failed (${response.code()}): ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    favoritedIds.remove(product.id)
                } else {
                    val response = RetrofitClient.apiService.addFavorite(
                        mapOf("userId" to UserSession.userId, "productId" to product.id)
                    )
                    if (!response.isSuccessful) {
                        Toast.makeText(requireContext(), "Failed (${response.code()}): ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    favoritedIds.add(product.id)
                }
                productAdapter.updateFavoritedIds(favoritedIds)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openDetail(product: Product) {
        startActivity(ProductDetailActivity.newIntent(requireContext(), product.id))
    }
}