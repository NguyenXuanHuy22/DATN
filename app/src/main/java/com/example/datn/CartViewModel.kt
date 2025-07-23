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

    val totalPrice: StateFlow<Int> = combine(_cartItems, _selectedItems) { items, selected ->
        items.filter { selected.contains(it.itemId) }
            .sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val shippingFee: StateFlow<Int> = totalPrice.map {
        if (it > 0) 30000 else 0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

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
                    val newCart = CartCreateRequest(userId, emptyList())
                    val createdCart = RetrofitClient.cartService.createCart(newCart)
                    currentCartId = createdCart.id
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

    fun toggleItemSelection(itemId: String) {
        _selectedItems.value = _selectedItems.value.toMutableSet().apply {
            if (contains(itemId)) remove(itemId) else add(itemId)
        }
    }

    fun updateItemQuantity(userId: String, itemId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                val updatedList = _cartItems.value.map { item ->
                    if (item.itemId == itemId) item.copy(quantity = newQuantity) else item
                }
                _cartItems.value = updatedList
                updateCartOnServer(updatedList)
            } catch (e: Exception) {
                _errorMessage.value = "Không thể cập nhật số lượng: ${e.message}"
            }
        }
    }

    fun deleteItem(itemId: String) {
        val updatedList = _cartItems.value.filterNot { it.itemId == itemId }
        _cartItems.value = updatedList
        _selectedItems.value = _selectedItems.value - itemId
        updateCartOnServer(updatedList)
    }

    fun addToCart(cartItem: CartItem) {
        val userId = currentUserId
        val cartId = currentCartId

        if (userId == null || cartId == null) {
            _errorMessage.value = "Không thể thêm vào giỏ hàng: thiếu userId hoặc cartId"
            return
        }

        viewModelScope.launch {
            try {
                val existing = _cartItems.value.find { it.itemId == cartItem.itemId }

                val updatedList = if (existing != null) {
                    _cartItems.value.map {
                        if (it.itemId == cartItem.itemId)
                            it.copy(quantity = it.quantity + cartItem.quantity)
                        else it
                    }
                } else {
                    _cartItems.value + cartItem
                }

                _cartItems.value = updatedList
                updateCartOnServer(updatedList)

            } catch (e: Exception) {
                _errorMessage.value = "Lỗi thêm vào giỏ hàng: ${e.message}"
            }
        }
    }

    private fun updateCartOnServer(updatedItems: List<CartItem>) {
        val userId = currentUserId ?: return
        val cartId = currentCartId ?: return

        val updatedCart = CartResponse(
            id = cartId,
            userId = userId,
            items = updatedItems.map {
                CartItemDto(
                    itemId = it.itemId,
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


