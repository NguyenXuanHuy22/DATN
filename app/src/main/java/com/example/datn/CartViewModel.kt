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

    // ✅ Tính tổng tiền dựa trên các item được chọn (dùng uniqueId)
    val totalPrice: StateFlow<Int> = combine(_cartItems, _selectedItems) { items, selected ->
        items.filter { selected.contains(it.uniqueId()) }
            .sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val shippingFee: StateFlow<Int> =
        flowOf(0).stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // ✅ Tổng cộng
    val grandTotal: StateFlow<Int> =
        combine(totalPrice, shippingFee) { total, ship -> total + ship }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun loadCart(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.cartService.getCartByUserId(userId)
                if (resp.isSuccessful) {
                    val cartBody = resp.body()
                    if (cartBody != null) {
                        currentCartId = cartBody._id
                        _cartItems.value = cartBody.items.map { it.toCartItem() }
                        _errorMessage.value = null
                        _selectedItems.value = emptySet()
                    } else {
                        _cartItems.value = emptyList()
                        _errorMessage.value = "Không có dữ liệu giỏ hàng"
                    }
                } else {
                    _cartItems.value = emptyList()
                    _errorMessage.value = "Lỗi server: ${resp.code()} - ${resp.message()}"
                }
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

    fun toggleItemSelection(uniqueId: String) {
        _selectedItems.value = _selectedItems.value.toMutableSet().apply {
            if (contains(uniqueId)) remove(uniqueId) else add(uniqueId)
        }
    }

    fun updateItemQuantity(id: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                val updatedList = _cartItems.value.map {
                    if (it.itemId == id || it.uniqueId() == id)
                        it.copy(quantity = newQuantity)
                    else it
                }
                _cartItems.value = updatedList
                updateCartOnServer(updatedList)
            } catch (e: Exception) {
                _errorMessage.value = "Không thể cập nhật số lượng: ${e.message}"
            }
        }
    }

    fun deleteItem(id: String) {
        val cartId = currentCartId ?: return
        val itemToDelete = _cartItems.value.find { it.itemId == id || it.uniqueId() == id }
        val validItemId = itemToDelete?.itemId ?: run {
            _errorMessage.value = "Không thể xoá sản phẩm: itemId null"
            return
        }

        val oldList = _cartItems.value
        _cartItems.value = oldList.filter { it.itemId != validItemId }

        viewModelScope.launch {
            try {
                RetrofitClient.cartService.deleteItemFromCart(cartId, validItemId)
                _selectedItems.value = _selectedItems.value - (itemToDelete.uniqueId())
            } catch (e: Exception) {
                _cartItems.value = oldList
                _errorMessage.value = "Không thể xoá sản phẩm: ${e.message}"
            }
        }
    }

    fun addToCart(item: CartItem) {
        viewModelScope.launch {
            try {
                val userId = item.userId ?: return@launch

                // Gọi API lấy giỏ hàng
                val resp = try {
                    RetrofitClient.cartService.getCartByUserId(userId)
                } catch (e: HttpException) {
                    if (e.code() == 404) {
                        // Nếu không có giỏ hàng -> tạo mới
                        val newCart = CartCreateRequest(
                            userId = userId,
                            items = listOf(item.toDto(isUpdate = false)) // dùng mapper gọn hơn
                        )
                        val created = RetrofitClient.cartService.createCart(newCart)
                        currentCartId = created._id
                        currentUserId = userId
                        _cartItems.value = created.items.map { it.toCartItem() }
                        return@launch
                    } else throw e
                }

                if (resp.isSuccessful) {
                    val cartBody = resp.body()
                    if (cartBody != null) {
                        currentCartId = cartBody._id
                        currentUserId = userId

                        // Thêm sản phẩm vào cart có sẵn
                        val response = RetrofitClient.cartService.addItemToCart(
                            cartId = cartBody._id,
                            newItem = item.toDto(isUpdate = false)
                        )
                        _cartItems.value = response.items.map { it.toCartItem() }
                    } else {
                        _errorMessage.value = "Không lấy được giỏ hàng"
                    }
                } else {
                    _errorMessage.value = "Lỗi server: ${resp.code()} - ${resp.message()}"
                }
            } catch (e: Exception) {
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
