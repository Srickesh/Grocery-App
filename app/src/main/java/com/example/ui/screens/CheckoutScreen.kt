package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Address
import com.example.model.DeliverySlot
import com.example.ui.theme.ESewaGreen
import com.example.ui.theme.FonepayRed
import com.example.ui.theme.KhaltiPurple
import com.example.ui.theme.PrimaryContainerGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerOrange
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TertiaryContainerBlue

@Composable
fun CheckoutScreen(
    addresses: List<Address>,
    selectedAddressId: Long?,
    deliverySlots: List<DeliverySlot>,
    selectedDateSlot: String,
    selectedTimeSlot: String,
    isExpressAddon: Boolean,
    selectedPaymentMethod: String,
    subtotalNrs: Int,
    deliveryFeeNrs: Int,
    vatNrs: Int,
    discountNrs: Int,
    grandTotalNrs: Int,
    onSelectAddress: (Long) -> Unit,
    onAddNewAddress: (String, String, String, String) -> Unit,
    onSelectDateSlot: (String) -> Unit,
    onSelectTimeSlot: (String) -> Unit,
    onToggleExpressAddon: () -> Unit,
    onSelectPaymentMethod: (String) -> Unit,
    onPlaceOrder: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    val effectiveDelivery = deliveryFeeNrs + (if (isExpressAddon) 150 else 0)
    val effectiveGrandTotal = subtotalNrs - discountNrs + effectiveDelivery + vatNrs

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) {
                                currentStep--
                            } else {
                                onBackClick()
                            }
                        },
                        modifier = Modifier.testTag("checkout_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = when (currentStep) {
                            1 -> "Step 1: Delivery Address"
                            2 -> "Step 2: Delivery Slot"
                            else -> "Step 3: Payment Method"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = PrimaryGreen
                    )
                }

                // Step Progress Indicator
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..3) {
                        val isFinished = i <= currentStep
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isFinished) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }

        // Step Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            when (currentStep) {
                1 -> {
                    // Step 1: Address Selection
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Delivery Address",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            TextButton(
                                onClick = { showAddAddressDialog = true },
                                modifier = Modifier.testTag("add_new_address_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add New", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(addresses) { addr ->
                        val isSelected = (selectedAddressId == addr.id) || (selectedAddressId == null && addr.isDefault)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PrimaryContainerGreen.copy(alpha = 0.08f) else SurfaceContainerLowest
                            ),
                            border = CardDefaults.outlinedCardBorder(
                                enabled = true
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("address_card_${addr.id}")
                                .clickable { onSelectAddress(addr.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectAddress(addr.id) },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = if (addr.label == "HOME") PrimaryGreen.copy(alpha = 0.15f) else TertiaryContainerBlue.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = addr.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (addr.label == "HOME") PrimaryGreen else TertiaryContainerBlue,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${addr.recipientName} • ${addr.phone}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )

                                    Text(
                                        text = "${addr.street}, ${addr.area}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Step 2: Delivery Slot
                    item {
                        Text(
                            text = "Choose Delivery Date",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Date Tabs
                        val dates = listOf("Today (12 Oct)", "Tomorrow (13 Oct)", "Wednesday (14 Oct)")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(dates) { dateOption ->
                                val isSelected = selectedDateSlot.contains(dateOption.take(5))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) PrimaryContainerGreen else SurfaceContainerLowest,
                                    border = CardDefaults.outlinedCardBorder(),
                                    modifier = Modifier
                                        .clickable { onSelectDateSlot(dateOption) }
                                        .testTag("date_slot_${dateOption.take(5)}")
                                ) {
                                    Text(
                                        text = dateOption,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Available Time Slots",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(deliverySlots) { slot ->
                        val isSelected = selectedTimeSlot == slot.timeRange
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PrimaryContainerGreen.copy(alpha = 0.08f) else SurfaceContainerLowest
                            ),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = slot.isAvailable) { onSelectTimeSlot(slot.timeRange) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSelectTimeSlot(slot.timeRange) },
                                        enabled = slot.isAvailable,
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = slot.timeRange,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }

                                if (!slot.isAvailable) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Unavailable",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Express Delivery Addon
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.06f)),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Express 45-Min Delivery",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "+NRs. 150 (Guaranteed instant dispatch)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = isExpressAddon,
                                    onCheckedChange = { onToggleExpressAddon() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PrimaryGreen
                                    )
                                )
                            }
                        }
                    }
                }

                3 -> {
                    // Step 3: Payment Method
                    item {
                        Text(
                            text = "Select Payment Gateway",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    val paymentOptions = listOf(
                        Triple("cod", "Cash on Delivery", "Pay in cash or QR upon delivery"),
                        Triple("esewa", "eSewa Mobile Wallet", "Instant payment via Nepal's #1 digital wallet"),
                        Triple("khalti", "Khalti Digital Wallet", "Pay securely with your Khalti account"),
                        Triple("fonepay", "Fonepay / Dynamic QR", "Direct mobile banking scan & pay"),
                        Triple("card", "Credit / Debit Card", "Visa, MasterCard, SCT enabled")
                    )

                    items(paymentOptions) { (key, title, subtitle) ->
                        val isSelected = selectedPaymentMethod == key
                        val badgeColor = when (key) {
                            "esewa" -> ESewaGreen
                            "khalti" -> KhaltiPurple
                            "fonepay" -> FonepayRed
                            "cod" -> SecondaryContainerOrange
                            else -> TertiaryContainerBlue
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) badgeColor.copy(alpha = 0.08f) else SurfaceContainerLowest
                            ),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("payment_method_$key")
                                .clickable { onSelectPaymentMethod(key) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectPaymentMethod(key) },
                                    colors = RadioButtonDefaults.colors(selectedColor = badgeColor)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(badgeColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (key) {
                                            "cod" -> Icons.Default.LocalAtm
                                            "fonepay" -> Icons.Default.QrCode
                                            "card" -> Icons.Default.CreditCard
                                            else -> Icons.Default.Payment
                                        },
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom CTA Navigation Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Payable",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "NRs. $effectiveGrandTotal",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            fontSize = 20.sp
                        )
                    )
                }

                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            onPlaceOrder()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentStep == 3) PrimaryGreen else SecondaryContainerOrange
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("checkout_continue_button")
                ) {
                    Text(
                        text = if (currentStep == 3) "Place Order" else "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }

    // Add Address Dialog
    if (showAddAddressDialog) {
        var newLabel by remember { mutableStateOf("HOME") }
        var newName by remember { mutableStateOf("Bibek") }
        var newStreet by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("+977 9841234567") }

        AlertDialog(
            onDismissRequest = { showAddAddressDialog = false },
            title = {
                Text("Add Delivery Address", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("HOME", "OFFICE", "OTHER").forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (newLabel == tag) PrimaryGreen else SurfaceContainerLow,
                                modifier = Modifier.clickable { newLabel = tag }
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (newLabel == tag) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Recipient Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newStreet,
                        onValueChange = { newStreet = it },
                        label = { Text("Street Address / Landmark") },
                        placeholder = { Text("e.g. Near Bhatbhateni, Koteshwor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Contact Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStreet.isNotBlank()) {
                            onAddNewAddress(newLabel, newName, newStreet, newPhone)
                            showAddAddressDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Save Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAddressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
