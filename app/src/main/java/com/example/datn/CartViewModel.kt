package com.example.datn

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _selectedItems = MutableStateFlow<Set<String>>(emptySet())
    val selectedItems: StateFlow<Set<String>> = _selectedItems

    private var currentCartId: String? = null
    private var currentUserId: String? = null

    // Tính tổng tiền các sản phẩm đã chọn
    val totalPrice: StateFlow<Int> = combine(_cartItems, _selectedItems) { items, selected ->
        items.filter { selected.contains(it.productId) }
            .sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Tính phí ship (30k nếu có ít nhất 1 sản phẩm)
    val shippingFee: StateFlow<Int> = totalPrice.map {
        if (it > 0) 30000 else 0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Tính tổng tiền + phí ship
    val grandTotal: StateFlow<Int> = combine(totalPrice, shippingFee) { total, ship ->
        total + ship
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun loadCart(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            try {
                val cartList = RetrofitClient.cartService.getCartByUserId(userId)
                if (cartList.isNotEmpty()) {
                    val cart = cartList.first()
                    currentCartId = cart.id
                    _cartItems.value = cart.items.map { it.toCartItem() }
                    _errorMessage.value = null
                } else {
                    val newCartId = UUID.randomUUID().toString()
                    val newCart = CartResponse(newCartId, userId, emptyList())
                    RetrofitClient.cartService.createCart(newCart)
                    currentCartId = newCartId
                    _cartItems.value = emptyList()
                    _errorMessage.value = null
                }
            } catch (e: Exception) {
                _cartItems.value = emptyList()
                _errorMessage.value = when (e) {
                    is HttpException -> "Lỗi server: ${e.code()}"
                    is UnknownHostException -> "Không thể kết nối đến server"
                    is ConnectException -> "Kết nối server thất bại"
                    else -> "Lỗi không xác định: ${e.message}"
                }
            }
        }
    }

    fun toggleItemSelection(productId: String) {
        _selectedItems.value = _selectedItems.value.toMutableSet().apply {
            if (contains(productId)) remove(productId) else add(productId)
        }
    }

    fun updateItemQuantity(userId: String, productId: String, newQuantity: Int) {
        if (newQuantity <= 0) return
        val updatedList = _cartItems.value.map {
            if (it.productId == productId) it.copy(quantity = newQuantity) else it
        }
        _cartItems.value = updatedList
        updateCartOnServer(updatedList)
    }

    fun deleteItem(userId: String, productId: String) {
        val updatedList = _cartItems.value.filterNot { it.productId == productId }
        _cartItems.value = updatedList
        _selectedItems.value = _selectedItems.value - productId
        updateCartOnServer(updatedList)
    }

    private fun updateCartOnServer(updatedItems: List<CartItem>) {
        val userId = currentUserId ?: return
        val cartId = currentCartId ?: return

        val updatedCart = CartResponse(
            id = cartId,
            userId = userId,
            items = updatedItems.map {
                CartItemDto(
                    productId = it.productId,
                    image = it.image,
                    name = it.name,
                    price = it.price,
                    size = it.size,
                    color = it.color,
                    quantity = it.quantity
                )
            }
        )

        viewModelScope.launch {
            try {
                RetrofitClient.cartService.updateCart(cartId, updatedCart)
            } catch (e: Exception) {
                Log.e("CartViewModel", "Lỗi cập nhật giỏ hàng: ${e.message}")
            }
        }
    }
}
