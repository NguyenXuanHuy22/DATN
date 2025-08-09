package com.example.datn

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException

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
        if (it >= 500_000) 0 else 30_000
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val grandTotal: StateFlow<Int> = combine(totalPrice, shippingFee) { total, ship ->
        total + ship
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun loadCart(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            try {
                val cart = RetrofitClient.cartService.getCartByUserId(userId)
                currentCartId = cart._id
                _cartItems.value = cart.items?.map { it.toCartItem() } ?: emptyList()
                _errorMessage.value = null
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    _cartItems.value = emptyList()
                    currentCartId = null
                    _errorMessage.value = null
                } else {
                    _cartItems.value = emptyList()
                    _errorMessage.value = "Lỗi server: ${e.code()}"
                }
            } catch (e: UnknownHostException) {
                _cartItems.value = emptyList()
                _errorMessage.value = "Không thể kết nối đến server"
            } catch (e: ConnectException) {
                _cartItems.value = emptyList()
                _errorMessage.value = "Kết nối server thất bại"
            } catch (e: Exception) {
                _cartItems.value = emptyList()
                _errorMessage.value = "Lỗi không xác định: ${e.message}"
            }
        }
    }


    fun toggleItemSelection(itemId: String) {
        _selectedItems.value = _selectedItems.value.toMutableSet().apply {
            if (contains(itemId)) remove(itemId) else add(itemId)
        }
    }

    fun updateItemQuantity(itemId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                val updatedList = _cartItems.value.map {
                    if (it.itemId == itemId) it.copy(quantity = newQuantity) else it
                }
                _cartItems.value = updatedList
                updateCartOnServer(updatedList)
            } catch (e: Exception) {
                _errorMessage.value = "Không thể cập nhật số lượng: ${e.message}"
            }
        }
    }

    fun deleteItem(itemId: String?) {
        val cartId = currentCartId ?: run {
            _errorMessage.value = "Không thể xoá sản phẩm: cartId null"
            return
        }
        val validItemId = itemId ?: run {
            _errorMessage.value = "Không thể xoá sản phẩm: itemId null"
            return
        }

        // Lưu danh sách cũ để rollback nếu xoá fail
        val oldList = _cartItems.value

        // Cập nhật UI ngay lập tức (xoá item khỏi danh sách)
        _cartItems.value = oldList.filter { it.itemId != validItemId }

        viewModelScope.launch {
            try {
                RetrofitClient.cartService.deleteItemFromCart(cartId, validItemId)
                Log.d("DeleteItem", "✅ Xóa thành công itemId: $validItemId")
            } catch (e: Exception) {
                // Rollback nếu server xoá thất bại
                _cartItems.value = oldList
                _errorMessage.value = "Không thể xoá sản phẩm: ${e.message}"
                Log.e("DeleteItem", "❌ Lỗi khi xoá sản phẩm: ${e.message}", e)
            }
        }
    }


    fun addToCart(item: CartItem) {
        viewModelScope.launch {
            try {
                val userId = item.userId
                Log.d("CART", "User ID: $userId")

                val cart = try {
                    RetrofitClient.cartService.getCartByUserId(userId)
                } catch (e: HttpException) {
                    if (e.code() == 404) {
                        val newCart = CartCreateRequest(
                            userId = userId,
                            items = listOf(item.toDtoForCreate())
                        )
                        val created = RetrofitClient.cartService.createCart(newCart)
                        currentCartId = created._id
                        currentUserId = userId
                        _cartItems.value = created.items.map { it.toCartItem() }
                        return@launch
                    } else throw e
                }

                currentCartId = cart._id
                currentUserId = userId

                val response = RetrofitClient.cartService.addItemToCart(
                    cartId = cart._id,
                    newItem = item.toDtoForCreate()
                )

                _cartItems.value = response.items.map { it.toCartItem() }
            } catch (e: Exception) {
                Log.e("CART", "Lỗi khi thêm vào giỏ hàng", e)
                _errorMessage.value = "Lỗi khi thêm vào giỏ hàng: ${e.message}"
            }
        }
    }

    private fun updateCartOnServer(updatedItems: List<CartItem>) {
        val cartId = currentCartId ?: return
        val userId = currentUserId ?: return

        val updatedCart = CartResponse(
            _id = cartId,
            userId = userId,
            items = updatedItems.map { it.toDtoForUpdate() }
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
