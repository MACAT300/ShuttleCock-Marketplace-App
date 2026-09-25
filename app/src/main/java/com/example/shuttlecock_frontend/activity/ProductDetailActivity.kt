package com.example.shuttlecock_frontend.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.auth.RetrofitClient
import com.example.shuttlecock_frontend.data.UserSession
import com.example.shuttlecock_frontend.models.Product
import com.example.shuttlecock_frontend.models.cart.CartItem
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_PRODUCT_ID = "product_id"

        fun newIntent(context: Context, productId: Int): Intent {
            return Intent(context, ProductDetailActivity::class.java)
                .putExtra(EXTRA_PRODUCT_ID, productId)
        }
    }

    private var product: Product? = null
    private var isFavorited = false

    private lateinit var imgFavoriteIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (productId == -1) {
            finish()
            return
        }

        imgFavoriteIcon = findViewById(R.id.imgFavoriteIcon)

        findViewById<FrameLayout>(R.id.backButton).setOnClickListener { finish() }
        findViewById<FrameLayout>(R.id.cartButton).setOnClickListener {
            startActivity(
                Intent(this, MainHostActivity::class.java)
                    .putExtra(MainHostActivity.EXTRA_START_TAB, MainHostActivity.TAB_CART)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
        findViewById<FrameLayout>(R.id.favoriteButton).setOnClickListener { toggleFavorite() }

        findViewById<LinearLayout>(R.id.bottomBar).setOnClickListener { addToCart() }

        loadProduct(productId)
        checkFavoriteStatus(productId)
    }

    private fun loadProduct(productId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getProductById(productId)
                val p = response.body()
                if (!response.isSuccessful || p == null) {
                    Toast.makeText(this@ProductDetailActivity, "Product not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }
                product = p
                bindProduct(p)
                recordBrowsingHistory(productId) // 加这一行
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun recordBrowsingHistory(productId: Int) {
        lifecycleScope.launch {
            try {
                RetrofitClient.apiService.recordBrowsingHistory(
                    mapOf("userId" to UserSession.userId, "productId" to productId)
                )
            } catch (e: Exception) {
                // 记录浏览记录失败不影响用户正常看商品，静默处理就好
            }
        }
    }

    private fun checkFavoriteStatus(productId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getFavorites(UserSession.userId)
                val favorites = response.body().orEmpty()
                isFavorited = favorites.any { it.productId == productId }
                updateFavoriteIcon()
            } catch (e: Exception) {
                // Non-critical: heart just won't be pre-filled.
            }
        }
    }

    private fun toggleFavorite() {
        val p = product ?: return
        lifecycleScope.launch {
            try {
                if (isFavorited) {
                    val response = RetrofitClient.apiService.removeFavorite(UserSession.userId, p.id)
                    if (!response.isSuccessful) {
                        Toast.makeText(this@ProductDetailActivity, "Failed (${response.code()})", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    isFavorited = false
                } else {
                    val response = RetrofitClient.apiService.addFavorite(
                        mapOf("userId" to UserSession.userId, "productId" to p.id)
                    )
                    if (!response.isSuccessful) {
                        Toast.makeText(this@ProductDetailActivity, "Failed (${response.code()})", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    isFavorited = true
                }
                updateFavoriteIcon()
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteIcon() {
        imgFavoriteIcon.setImageResource(
            if (isFavorited) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
    }

    private fun bindProduct(p: Product) {
        findViewById<TextView>(R.id.tvBrandTop).text = p.brand?.name?.uppercase() ?: ""
        findViewById<TextView>(R.id.tvTitle).text = p.name
        findViewById<TextView>(R.id.tvDescription).text = p.description ?: ""
        findViewById<TextView>(R.id.tvPrice).text = "RM %.2f".format(p.price)
        findViewById<TextView>(R.id.tvPriceInfo).text = "RM %.2f".format(p.price)

        val tvStock = findViewById<TextView>(R.id.tvStock)
        if (p.quantity <= 10) {
            tvStock.text = "Only ${p.quantity} left"
            tvStock.setTextColor(android.graphics.Color.parseColor("#FF5252"))
        } else {
            tvStock.text = "●  In Stock"
            tvStock.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        }

        val imageUrl = p.image?.url
        val imgView = findViewById<ImageView>(R.id.imgProduct)
        if (!imageUrl.isNullOrBlank()) {
            imgView.load(imageUrl) {
                placeholder(R.drawable.image_background)
                error(R.drawable.image_background)
            }
        } else {
            imgView.setImageResource(R.drawable.image_background)
        }
    }

    private fun addToCart() {
        val p = product ?: return
        lifecycleScope.launch {
            try {
                val cartResponse = RetrofitClient.apiService.getOrCreateCart(
                    mapOf("userId" to UserSession.userId)
                )
                val cart = cartResponse.body()
                if (!cartResponse.isSuccessful || cart == null) {
                    Toast.makeText(this@ProductDetailActivity, "Failed to access cart", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                UserSession.cartId = cart.id

                val newItem = CartItem(cartId = cart.id, productId = p.id, quantity = 1)
                val addResponse = RetrofitClient.apiService.addCartItem(newItem)

                if (addResponse.isSuccessful) {
                    com.google.android.material.snackbar.Snackbar.make(
                        findViewById(android.R.id.content),
                        "${p.name} added to cart",
                        800 // 毫秒，想多短都可以自己调
                    ).show()
                } else {
                    val msg = addResponse.errorBody()?.string() ?: "Failed to add to cart"
                    Toast.makeText(this@ProductDetailActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}