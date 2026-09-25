package com.example.shuttlecock_frontend.fragment

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlecock_frontend.activity.PaymentWebViewActivity
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.adapter.BrandGroup
import com.example.shuttlecock_frontend.adapter.CartBrandGroupAdapter
import com.example.shuttlecock_frontend.auth.RetrofitClient
import com.example.shuttlecock_frontend.data.UserSession
import com.example.shuttlecock_frontend.models.cart.CartItem
import kotlinx.coroutines.launch

class CartFragment : Fragment(R.layout.fragment_cart) {

    private lateinit var totalText: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var bottomBar: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var confirmButton: TextView

    private lateinit var adapter: CartBrandGroupAdapter
    private val cartItems = mutableListOf<CartItem>()
    private val selectedIds = mutableSetOf<Int>()

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            PaymentWebViewActivity.RESULT_PAYMENT_SUCCESS -> {
                Toast.makeText(requireContext(), "Payment completed! Your order is being confirmed.", Toast.LENGTH_LONG).show()
                loadCart()
            }
            PaymentWebViewActivity.RESULT_PAYMENT_CANCELLED -> {
                Toast.makeText(requireContext(), "Payment cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        totalText = view.findViewById(R.id.totalPrice)
        recycler = view.findViewById(R.id.orderRecyclerView)
        bottomBar = view.findViewById(R.id.bottomBar)
        emptyState = view.findViewById(R.id.emptyState)
        confirmButton = view.findViewById(R.id.confirmButton)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = CartBrandGroupAdapter(
            groups = emptyList(),
            selectedIds = selectedIds,
            onSelectionChanged = {
                adapter.notifyDataSetChanged()
                updateTotal()
            },
            onQuantityChange = { item, newQuantity -> changeQuantity(item, newQuantity) },
            onDelete = { item -> deleteItem(item) }
        )
        recycler.adapter = adapter

        confirmButton.setOnClickListener { startCheckout() }
    }

    override fun onResume() {
        super.onResume()
        loadCart()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadCart()
        }
    }

    private fun loadCart() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val cartResponse = RetrofitClient.apiService.getOrCreateCart(
                    mapOf("userId" to UserSession.userId)
                )
                val cart = cartResponse.body()
                if (!cartResponse.isSuccessful || cart == null) {
                    Toast.makeText(requireContext(), "Failed to load cart", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                UserSession.cartId = cart.id

                val itemsResponse = RetrofitClient.apiService.getCartItems(cart.id)
                val items = itemsResponse.body().orEmpty()

                cartItems.clear()
                cartItems.addAll(items)

                items.forEach { selectedIds.add(it.id) }
                selectedIds.retainAll(items.map { it.id }.toSet())

                rebuildGroupsAndRender()
                updateEmptyState()
                updateTotal()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rebuildGroupsAndRender() {
        val groups = cartItems
            .groupBy { it.product?.brand }
            .map { (brand, items) -> BrandGroup(brand, items) }
        adapter.updateGroups(groups)
    }

    private fun changeQuantity(item: CartItem, newQuantity: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateCartItem(item.id, item.copy(quantity = newQuantity))
                val updated = response.body()
                if (response.isSuccessful && updated != null) {
                    val index = cartItems.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        cartItems[index] = updated.copy(product = item.product)
                    }
                    rebuildGroupsAndRender()
                    updateTotal()
                } else {
                    Toast.makeText(requireContext(), "Failed to update quantity", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteItem(item: CartItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteCartItem(item.id)
                if (response.isSuccessful) {
                    cartItems.removeAll { it.id == item.id }
                    selectedIds.remove(item.id)
                    rebuildGroupsAndRender()
                    updateEmptyState()
                    updateTotal()
                } else {
                    Toast.makeText(requireContext(), "Failed to remove item", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCheckout() {
        if (selectedIds.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one item", Toast.LENGTH_SHORT).show()
            return
        }

        confirmButton.isEnabled = false
        confirmButton.alpha = 0.5f
        val originalText = confirmButton.text
        confirmButton.text = "Processing..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val orderResponse = RetrofitClient.apiService.checkout(
                    hashMapOf(
                        "userId" to UserSession.userId,
                        "cartItemIds" to selectedIds.toList()
                    )
                )
                val order = orderResponse.body()
                if (!orderResponse.isSuccessful || order == null) {
                    val msg = orderResponse.errorBody()?.string() ?: "Checkout failed"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val paymentResponse = RetrofitClient.apiService.createPaymentCheckout(mapOf("orderId" to order.id))
                val payment = paymentResponse.body()
                if (!paymentResponse.isSuccessful || payment == null) {
                    val msg = paymentResponse.errorBody()?.string() ?: "Could not start payment"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                paymentLauncher.launch(PaymentWebViewActivity.start(requireContext(), payment.url))
                } catch (e: Exception) {
                android.util.Log.e("CHECKOUT_ERROR", "Full error", e)
                Toast.makeText(requireContext(), "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                confirmButton.isEnabled = true
                confirmButton.alpha = 1f
                confirmButton.text = originalText
            }
        }
    }

    private fun updateTotal() {
        val total = cartItems
            .filter { selectedIds.contains(it.id) }
            .sumOf { (it.product?.price ?: 0.0) * it.quantity }
        totalText.text = "Total: RM %.2f".format(total)
    }

    private fun updateEmptyState() {
        val isEmpty = cartItems.isEmpty()
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
        bottomBar.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}