package com.example.datn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressManagerSheet(
    userId: String,
    addressViewModel: AddressViewModel,
    provinces: List<Province>,
    onDismiss: () -> Unit,
    onAddressChosen: (Address) -> Unit,
    userName: String = "",
    userPhone: String = ""
) {
    val addresses by addressViewModel.addresses
    var showAddForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Address?>(null) }
    var deleteConfirm by remember { mutableStateOf<Address?>(null) }
    var selectedId by remember(addresses) { mutableStateOf(addresses.firstOrNull { it.isDefault }?._id ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Chọn địa chỉ giao hàng", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                AssistChip(onClick = { showAddForm = !showAddForm }, label = { Text("+ Thêm mới") })
            }

            Spacer(Modifier.height(12.dp))

            if (showAddForm) {
                AddressAddForm(userId = userId, provinces = provinces, userName = userName, userPhone = userPhone, onAdded = { addr ->
                    // call backend to persist then refresh and return selection
                    addressViewModel.addAddress(addr) {
                        addressViewModel.loadAddresses(userId)
                        onAddressChosen(addr)
                        selectedId = addr._id
                    }
                })
                Spacer(Modifier.height(16.dp))
            }

            editing?.let { toEdit ->
                AddressEditForm(
                    original = toEdit,
                    provinces = provinces,
                    onCancel = { editing = null },
                    onSave = { updated ->
                        addressViewModel.updateAddress(toEdit._id, updated) {
                            editing = null
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
            }

            LazyColumn {
                items(addresses) { addr ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = addr._id == selectedId,
                                    onClick = {
                                        // Cập nhật state ngay lập tức để UI đổi tick
                                        selectedId = addr._id

                                        // Gọi API để set mặc định
                                        addressViewModel.setDefaultAddress(addr) {
                                            // Sau khi server cập nhật xong, refresh danh sách
                                            addressViewModel.loadAddresses(userId)
                                            onAddressChosen(addr.copy(isDefault = true))
                                        }
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedId = addr._id
                                            addressViewModel.setDefaultAddress(addr) {
                                                addressViewModel.loadAddresses(userId)
                                                onAddressChosen(addr.copy(isDefault = true))
                                            }
                                        }

                                ) {
                                    Text(addr.name, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        addr.address,
                                        color = Color.Gray,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {

                                Spacer(Modifier.width(8.dp))
                                TextButton(onClick = { editing = addr }) { Text("Sửa") }
                                Spacer(Modifier.width(4.dp))
                                TextButton(onClick = { deleteConfirm = addr }) { Text("Xóa") }
                            }
                        }
                    }
                }
            }


            if (deleteConfirm != null) {
                val target = deleteConfirm!!
                AlertDialog(
                    onDismissRequest = { deleteConfirm = null },
                    confirmButton = {
                        TextButton(onClick = {
                            addressViewModel.deleteAddress(target._id, target.userId) {
                                deleteConfirm = null
                                if (selectedId == target._id) selectedId = addresses.firstOrNull()?. _id ?: ""
                            }
                        }) { Text("Xóa") }
                    },
                    dismissButton = { TextButton(onClick = { deleteConfirm = null }) { Text("Hủy") } },
                    title = { Text("Xóa địa chỉ") },
                    text = { Text("Bạn có chắc muốn xóa địa chỉ này?") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressAddForm(
    userId: String,
    provinces: List<Province>,
    userName: String = "",
    userPhone: String = "",
    onAdded: (Address) -> Unit
) {
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf("") }

    var selectedProvince by remember { mutableStateOf<Province?>(null) }
    var selectedDistrict by remember { mutableStateOf<District?>(null) }
    var selectedWard by remember { mutableStateOf<Ward?>(null) }
    var districts by remember { mutableStateOf<List<District>>(emptyList()) }
    var wards by remember { mutableStateOf<List<Ward>>(emptyList()) }

    Column(Modifier.background(Color(0xFFF7F7F7)).padding(12.dp)) {
        Text("Thêm địa chỉ giao hàng", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))

        var pExp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = pExp, onExpandedChange = { pExp = !pExp }) {
            OutlinedTextField(value = selectedProvince?.name ?: "Chọn tỉnh/thành", onValueChange = {}, readOnly = true, label = { Text("Tỉnh/Thành phố") }, modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = pExp, onDismissRequest = { pExp = false }) {
                provinces.forEach { prov ->
                    DropdownMenuItem(text = { Text(prov.name) }, onClick = {
                        selectedProvince = prov
                        selectedDistrict = null
                        selectedWard = null
                        districts = emptyList()
                        wards = emptyList()
                        scope.launch {
                            try {
                                val res = RetrofitClient.provincesApi.getProvince(prov.code, 2)
                                if (res.isSuccessful) {
                                    districts = res.body()?.districts ?: emptyList()
                                }
                            } catch (_: Exception) { }
                        }
                        pExp = false
                    })
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        var dExp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = dExp, onExpandedChange = { if (districts.isNotEmpty()) dExp = !dExp }) {
            OutlinedTextField(value = selectedDistrict?.name ?: "Chọn quận/huyện", onValueChange = {}, readOnly = true, label = { Text("Quận/Huyện") }, enabled = districts.isNotEmpty(), modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = dExp, onDismissRequest = { dExp = false }) {
                districts.forEach { d ->
                    DropdownMenuItem(text = { Text(d.name) }, onClick = {
                        selectedDistrict = d
                        selectedWard = null
                        wards = emptyList()
                        scope.launch {
                            try {
                                val res = RetrofitClient.provincesApi.getDistrict(d.code, 2)
                                if (res.isSuccessful) {
                                    wards = res.body()?.wards ?: emptyList()
                                }
                            } catch (_: Exception) { }
                        }
                        dExp = false
                    })
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        var wExp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = wExp, onExpandedChange = { if (wards.isNotEmpty()) wExp = !wExp }) {
            OutlinedTextField(value = selectedWard?.name ?: "Chọn phường/xã", onValueChange = {}, readOnly = true, label = { Text("Phường/Xã") }, enabled = wards.isNotEmpty(), modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = wExp, onDismissRequest = { wExp = false }) {
                wards.forEach { w ->
                    DropdownMenuItem(text = { Text(w.name) }, onClick = {
                        selectedWard = w
                        wExp = false
                    })
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text("Địa chỉ chi tiết") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            val fullAddress = buildString {
                selectedWard?.let { append(it.name).append(", ") }
                selectedDistrict?.let { append(it.name).append(", ") }
                selectedProvince?.let { append(it.name) }
                if (detail.isNotBlank()) { if (isNotEmpty()) append(", "); append(detail) }
            }
            val addr = Address(
                _id = "",
                userId = userId,
                name = userName, // Use name from profile
                address = fullAddress,
                phone = userPhone, // Use phone from profile
                isDefault = false
            )
            onAdded(addr)
        }, modifier = Modifier.fillMaxWidth()) { Text("Lưu địa chỉ") }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressEditForm(
    original: Address,
    provinces: List<Province>,
    onCancel: () -> Unit,
    onSave: (Address) -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(original.name) }
    var phone by remember { mutableStateOf(original.phone) }
    var detail by remember { mutableStateOf("") }

    var selectedProvince by remember { mutableStateOf<Province?>(null) }
    var selectedDistrict by remember { mutableStateOf<District?>(null) }
    var selectedWard by remember { mutableStateOf<Ward?>(null) }
    var districts by remember { mutableStateOf<List<District>>(emptyList()) }
    var wards by remember { mutableStateOf<List<Ward>>(emptyList()) }

    Column(Modifier.background(Color(0xFFF7F7F7)).padding(12.dp)) {
        Text("Sửa địa chỉ", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên người nhận") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Số điện thoại") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))

        var pExp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = pExp, onExpandedChange = { pExp = !pExp }) {
            OutlinedTextField(value = selectedProvince?.name ?: "Chọn tỉnh/thành", onValueChange = {}, readOnly = true, label = { Text("Tỉnh/Thành phố") }, modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = pExp, onDismissRequest = { pExp = false }) {
                provinces.forEach { prov ->
                    DropdownMenuItem(text = { Text(prov.name) }, onClick = {
                        selectedProvince = prov
                        selectedDistrict = null
                        selectedWard = null
                        districts = emptyList()
                        wards = emptyList()
                        scope.launch {
                            try {
                                val res = RetrofitClient.provincesApi.getProvince(prov.code, 2)
                                if (res.isSuccessful) districts = res.body()?.districts ?: emptyList()
                            } catch (_: Exception) { }
                        }
                        pExp = false
                    })
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        var dExp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = dExp, onExpandedChange = { if (districts.isNotEmpty()) dExp = !dExp }) {
            OutlinedTextField(value = selectedDistrict?.name ?: "Chọn quận/huyện", onValueChange = {}, readOnly = true, label = { Text("Quận/Huyện") }, enabled = districts.isNotEmpty(), modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = dExp, onDismissRequest = { dExp = false }) {
                districts.forEach { d ->
                    DropdownMenuItem(text = { Text(d.name) }, onClick = {
                        selectedDistrict = d
                        selectedWard = null
                        wards = emptyList()
                        scope.launch {
                            try {
                                val res = RetrofitClient.provincesApi.getDistrict(d.code, 2)
                                if (res.isSuccessful) wards = res.body()?.wards ?: emptyList()
                            } catch (_: Exception) { }
                        }
                        dExp = false
                    })
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        var wExp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = wExp, onExpandedChange = { if (wards.isNotEmpty()) wExp = !wExp }) {
            OutlinedTextField(value = selectedWard?.name ?: "Chọn phường/xã", onValueChange = {}, readOnly = true, label = { Text("Phường/Xã") }, enabled = wards.isNotEmpty(), modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = wExp, onDismissRequest = { wExp = false }) {
                wards.forEach { w ->
                    DropdownMenuItem(text = { Text(w.name) }, onClick = {
                        selectedWard = w
                        wExp = false
                    })
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text("Địa chỉ chi tiết") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel) { Text("Hủy") }
            Button(
                onClick = {
                    val full = buildString {
                        selectedWard?.let { append(it.name).append(", ") }
                        selectedDistrict?.let { append(it.name).append(", ") }
                        selectedProvince?.let { append(it.name) }
                        if (detail.isNotBlank()) { if (isNotEmpty()) append(", "); append(detail) }
                    }
                    onSave(
                        original.copy(
                            name = name,
                            phone = phone,
                            address = if (full.isNotBlank()) full else original.address
                        )
                    )
                }
            ) { Text("Lưu") }
        }

    }
}


