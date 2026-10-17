package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.viewmodel.MainViewModel

@Composable
fun BusinessScreen(
    state: AppState,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Invoicing, 1: Khata Ledger, 2: Inventory, 3: Health Scorecard

    // Invoice Form State
    var showCreateInvoiceDialog by remember { mutableStateOf(false) }
    var customerNameInput by remember { mutableStateOf("") }
    var customerPhoneInput by remember { mutableStateOf("") }
    var gstinInput by remember { mutableStateOf("") }
    var itemDescInput by remember { mutableStateOf("") }
    var itemQtyInput by remember { mutableStateOf("1") }
    var itemRateInput by remember { mutableStateOf("") }
    var itemGstInput by remember { mutableStateOf("18") }

    // Khata Entry Form State
    var showAddKhataDialog by remember { mutableStateOf(false) }
    var khataPartyName by remember { mutableStateOf("") }
    var khataPhone by remember { mutableStateOf("") }
    var khataAmount by remember { mutableStateOf("") }
    var khataType by remember { mutableStateOf(KhataType.GAVE_UDHAAR) }
    var khataNote by remember { mutableStateOf("") }

    // Inventory Form State
    var showAddProductDialog by remember { mutableStateOf(false) }
    var prodName by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf("") }
    var prodQty by remember { mutableStateOf("10") }
    var prodCost by remember { mutableStateOf("500") }
    var prodSell by remember { mutableStateOf("800") }

    val totalRevenue = state.invoices.sumOf { it.grandTotal }
    val totalKhataUdhaar = state.khataEntries.filter { it.type == KhataType.GAVE_UDHAAR }.sumOf { it.amount }
    val totalKhataJama = state.khataEntries.filter { it.type == KhataType.GOT_JAMA }.sumOf { it.amount }
    val netReceivables = (totalKhataUdhaar - totalKhataJama).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp)
    ) {
        // Business Mode Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BUSINESS SUITE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = state.businessName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "💼", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Invoiced", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹ ${String.format("%,.0f", totalRevenue)}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Pending Dues", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹ ${String.format("%,.0f", netReceivables)}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Stock SKUs", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${state.inventory.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sub-tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("GST Invoices", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Khata Ledger", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Inventory", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Analytics", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // INVOICING TAB
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GST Invoices",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showCreateInvoiceDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("create_invoice_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create Bill (+50 XP, +1 Coin)", fontSize = 11.sp)
                        }
                    }
                }

                if (state.invoices.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No invoices generated yet. Tap 'Create Bill' to generate your first GST bill with WhatsApp & UPI link sharing.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    items(state.invoices, key = { it.invoiceNumber }) { inv ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth().testTag("invoice_card_${inv.invoiceNumber}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(inv.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "${inv.invoiceNumber} • ${if (inv.gstin.isNotEmpty()) "GST: " + inv.gstin else "Cash Invoice"}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "₹ ${String.format("%,.2f", inv.grandTotal)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val billText = """
                                                TAX INVOICE - ${state.businessName}
                                                Invoice: ${inv.invoiceNumber}
                                                Customer: ${inv.customerName}
                                                Total: ₹ ${String.format("%.2f", inv.grandTotal)}
                                                UPI Pay: upi://pay?pa=${state.businessUpiId}&pn=${Uri.encode(state.businessName)}&am=${inv.grandTotal}
                                            """.trimIndent()
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, billText)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Invoice"))
                                        },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share Bill", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val upiUri = Uri.parse("upi://pay?pa=${state.businessUpiId}&pn=${Uri.encode(state.businessName)}&am=${inv.grandTotal}&cu=INR")
                                            val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                            try {
                                                context.startActivity(Intent.createChooser(upiIntent, "Pay with UPI"))
                                            } catch (_: Exception) {
                                                Toast.makeText(context, "UPI Link: $upiUri", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("UPI Link", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // KHATA LEDGER TAB
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Khata Credit/Debit Ledger",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showAddKhataDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("add_khata_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Khata (+25 XP)", fontSize = 11.sp)
                        }
                    }
                }

                if (state.khataEntries.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No Khata transactions logged yet. Tap 'Add Khata' to record Udhaar (credit) or Jama (payment received).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    items(state.khataEntries, key = { it.id }) { entry ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(entry.partyName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (entry.note.isNotEmpty()) {
                                        Text(entry.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val isGave = entry.type == KhataType.GAVE_UDHAAR
                                    Text(
                                        text = "${if (isGave) "-" else "+"} ₹ ${String.format("%,.0f", entry.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isGave) "Udhaar (Gave)" else "Jama (Received)",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // INVENTORY TAB
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inventory SKUs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showAddProductDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("add_product_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New SKU", fontSize = 11.sp)
                        }
                    }
                }

                if (state.inventory.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No inventory SKUs added yet. Tap 'New SKU' to add your products with low-stock alerts.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    items(state.inventory, key = { it.id }) { prod ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Sell: ₹ ${prod.sellingPrice}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.updateInventoryQuantity(prod.id, -1) },
                                        modifier = Modifier.size(30.dp)
                                    ) { Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                    Text(
                                        text = "${prod.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateInventoryQuantity(prod.id, 1) },
                                        modifier = Modifier.size(30.dp)
                                    ) { Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // ANALYTICS TAB
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("BUSINESS HEALTH SCORECARD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Total Invoiced Volume: ₹ ${String.format("%,.2f", totalRevenue)}\nTotal Transactions: ${state.invoices.size}\nNet Pending Receivables: ₹ ${String.format("%,.2f", netReceivables)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Create GST Invoice
    if (showCreateInvoiceDialog) {
        AlertDialog(
            onDismissRequest = { showCreateInvoiceDialog = false },
            title = { Text("Smart GST Invoice", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customerNameInput,
                        onValueChange = { customerNameInput = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_name")
                    )
                    OutlinedTextField(
                        value = itemDescInput,
                        onValueChange = { itemDescInput = it },
                        label = { Text("Product Description") },
                        modifier = Modifier.fillMaxWidth().testTag("input_invoice_item_desc")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = itemQtyInput,
                            onValueChange = { itemQtyInput = it },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = itemRateInput,
                            onValueChange = { itemRateInput = it },
                            label = { Text("Rate (₹)") },
                            modifier = Modifier.weight(1.5f).testTag("input_invoice_rate")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = itemRateInput.toDoubleOrNull() ?: 0.0
                        val qty = itemQtyInput.toDoubleOrNull() ?: 1.0
                        val gst = itemGstInput.toDoubleOrNull() ?: 18.0
                        val lineItem = InvoiceLineItem(
                            description = itemDescInput.ifBlank { "Item" },
                            quantity = qty,
                            rate = rate,
                            gstRatePercent = gst
                        )
                        viewModel.addInvoice(
                            customerName = customerNameInput,
                            customerPhone = customerPhoneInput,
                            gstin = gstinInput,
                            items = listOf(lineItem),
                            isPaid = true
                        )
                        showCreateInvoiceDialog = false
                    },
                    modifier = Modifier.testTag("submit_create_invoice_button")
                ) {
                    Text("Generate Bill")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateInvoiceDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal Dialog: Add Khata Entry
    if (showAddKhataDialog) {
        AlertDialog(
            onDismissRequest = { showAddKhataDialog = false },
            title = { Text("Add Khata Entry", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = khataPartyName,
                        onValueChange = { khataPartyName = it },
                        label = { Text("Party Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_khata_party")
                    )
                    OutlinedTextField(
                        value = khataAmount,
                        onValueChange = { khataAmount = it },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_khata_amount")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = khataType == KhataType.GAVE_UDHAAR,
                            onClick = { khataType = KhataType.GAVE_UDHAAR },
                            label = { Text("Udhaar (Gave)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = khataType == KhataType.GOT_JAMA,
                            onClick = { khataType = KhataType.GOT_JAMA },
                            label = { Text("Jama (Received)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = khataAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.addKhataEntry(khataPartyName, khataPhone, amt, khataType, khataNote)
                            showAddKhataDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_khata_button")
                ) {
                    Text("Save to Ledger")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddKhataDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal Dialog: Add Product SKU
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Add Inventory SKU", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_product_name")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = prodQty,
                            onValueChange = { prodQty = it },
                            label = { Text("Quantity") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = prodSell,
                            onValueChange = { prodSell = it },
                            label = { Text("Price (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = prodQty.toIntOrNull() ?: 0
                        val sell = prodSell.toDoubleOrNull() ?: 0.0
                        val cost = prodCost.toDoubleOrNull() ?: 0.0
                        viewModel.addInventoryItem(prodName, prodCategory, qty, cost, sell)
                        showAddProductDialog = false
                    },
                    modifier = Modifier.testTag("submit_add_product_button")
                ) {
                    Text("Add SKU")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) { Text("Cancel") }
            }
        )
    }
}
