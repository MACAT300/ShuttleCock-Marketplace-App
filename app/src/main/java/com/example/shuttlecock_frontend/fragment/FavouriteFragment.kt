package com.example.shuttlecock_frontend.fragment

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlecock_frontend.activity.ProductDetailActivity
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.adapter.FavouriteAdapter
import com.example.shuttlecock_frontend.auth.RetrofitClient
import com.example.shuttlecock_frontend.data.UserSession
import com.example.shuttlecock_frontend.models.Product
import com.example.shuttlecock_frontend.models.favorite.Favorite
import kotlinx.coroutines.launch

class FavouriteFragment : Fragment(R.layout.fragment_favourite) {

    private lateinit var adapter: FavouriteAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var emptyState: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.favouriteRecycler)
        emptyState = view.findViewById(R.id.emptyState)

        adapter = FavouriteAdapter(
            products = mutableListOf(),
            onCardClick = { product -> openDetail(product) },
            onRemoveClick = { product, position -> removeFavorite(product, position) }
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
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

    private fun loadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getFavorites(UserSession.userId)
                val favorites: List<Favorite> = response.body().orEmpty()
                val products: List<Product> = favorites.mapNotNull { it.product }

                adapter.replaceAll(products)
                updateEmptyState(products.isEmpty())
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load favourites: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeFavorite(product: Product, position: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.removeFavorite(UserSession.userId, product.id)
                if (!response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to remove (${response.code()}): ${response.errorBody()?.string()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                adapter.removeAt(position)
                updateEmptyState(adapter.itemCount == 0)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to remove: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun openDetail(product: Product) {
        startActivity(ProductDetailActivity.newIntent(requireContext(), product.id))
    }
}