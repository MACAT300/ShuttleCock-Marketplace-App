package com.example.shuttlecock_frontend.`interface`

import com.example.shuttlecock_frontend.models.Brand
import com.example.shuttlecock_frontend.models.Product
import com.example.shuttlecock_frontend.models.User
import com.example.shuttlecock_frontend.models.auth.LoginRequest
import com.example.shuttlecock_frontend.models.auth.LoginResponse
import com.example.shuttlecock_frontend.models.auth.SignUpRequest
import com.example.shuttlecock_frontend.models.cart.Cart
import com.example.shuttlecock_frontend.models.cart.CartItem
import com.example.shuttlecock_frontend.models.image.CachedImageResponse
import com.example.shuttlecock_frontend.models.order.Order
import com.example.shuttlecock_frontend.models.order.OrderItem
import com.example.shuttlecock_frontend.models.payment.PaymentIntentResponse
import com.example.shuttlecock_frontend.models.favorite.Favorite
import com.example.shuttlecock_frontend.models.history.BrowsingHistoryItem
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ===== Auth =====
    @POST("signup")
    suspend fun signup(@Body request: SignUpRequest): Response<User>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("login/google")
    suspend fun loginWithGoogle(@Body body: Map<String, String>): Response<LoginResponse>

    @POST("password-reset/request")
    suspend fun requestPasswordReset(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("password-reset/confirm")
    suspend fun confirmPasswordReset(@Body body: Map<String, String>): Response<Map<String, String>>

    // ===== Products =====
    @GET("products")
    suspend fun getProducts(
        @Query("name") name: String? = null,
        @Query("brandId") brandId: Int? = null
    ): Response<List<Product>>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<Product>

    @POST("products")
    suspend fun addProduct(@Body product: Product): Response<Product>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: Product): Response<Product>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Void>

    @GET("products/{id}/preview-image/cached")
    suspend fun getCachedProductImage(@Path("id") id: Int): Response<CachedImageResponse>

    // ===== Brands =====
    @GET("brands")
    suspend fun getBrands(): Response<List<Brand>>

    @GET("brands/{id}")
    suspend fun getBrandById(@Path("id") id: Int): Response<Brand>

    @POST("brands")
    suspend fun addBrand(@Body brand: Brand): Response<Brand>

    @GET("brands/{id}/image/cached")
    suspend fun getCachedBrandImage(@Path("id") id: Int): Response<CachedImageResponse>

    // ===== Cart =====
    // Gets (or lazily creates) the cart belonging to a user. Body: {"userId": 1}
    @POST("carts")
    suspend fun getOrCreateCart(@Body body: Map<String, Int>): Response<Cart>

    @GET("carts/{cartId}/items")
    suspend fun getCartItems(@Path("cartId") cartId: Int): Response<List<CartItem>>

    @POST("cartItems")
    suspend fun addCartItem(@Body item: CartItem): Response<CartItem>

    @PUT("cartItems/{id}")
    suspend fun updateCartItem(@Path("id") id: Int, @Body item: CartItem): Response<CartItem>

    @DELETE("cartItems/{id}")
    suspend fun deleteCartItem(@Path("id") id: Int): Response<Void>

    // ===== Orders =====
    // Body: {"userId": 1} -> converts the user's cart into an Order
    @POST("orders/checkout")
    suspend fun checkout(@Body body: HashMap<String, Any>): Response<Order>

    @GET("orders")
    suspend fun getOrders(@Query("userId") userId: Int? = null): Response<List<Order>>

    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: Int): Response<Order>

    @GET("orders/{id}/items")
    suspend fun getOrderItems(@Path("id") id: Int): Response<List<OrderItem>>

    // ===== Payment (Stripe Checkout) =====
    // Body: {"orderId": 1} -> creates a Stripe Checkout Session, returns hosted payment URL
    @POST("payments/checkout")
    suspend fun createPaymentCheckout(@Body body: Map<String, Int>): Response<PaymentIntentResponse>

    @GET("payments/orders/{orderId}")
    suspend fun getPaymentsByOrder(@Path("orderId") orderId: Int): Response<List<com.example.shuttlecock_frontend.models.Payment>>

    // ===== User profile =====
    @PUT("users/{id}/name")
    suspend fun updateUserName(@Path("id") id: Int, @Body body: Map<String, String>): Response<User>

    @PUT("users/{id}/password")
    suspend fun changePassword(@Path("id") id: Int, @Body body: Map<String, String>): Response<Map<String, String>>

    @Multipart
    @POST("users/{id}/avatar")
    suspend fun uploadAvatar(@Path("id") id: Int, @Part file: MultipartBody.Part): Response<User>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<User>

    // ===== Favorites =====
    @GET("favorites")
    suspend fun getFavorites(@Query("userId") userId: Int): Response<List<Favorite>>

    @POST("favorites")
    suspend fun addFavorite(@Body body: Map<String, Int>): Response<Favorite>

    @DELETE("favorites")
    suspend fun removeFavorite(
        @Query("userId") userId: Int,
        @Query("productId") productId: Int
    ): Response<Void>

    // ===== Order History =====
    // (getOrders 和 getOrderItems 之前已经有了，直接复用)

    // ===== Browsing History =====
    @POST("browsing-history")
    suspend fun recordBrowsingHistory(@Body body: Map<String, Int>): Response<Unit>

    @GET("browsing-history")
    suspend fun getBrowsingHistory(@Query("userId") userId: Int): Response<List<BrowsingHistoryItem>>
}
