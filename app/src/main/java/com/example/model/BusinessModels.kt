package com.example.model

data class InvoiceLineItem(
    val description: String,
    val quantity: Double,
    val rate: Double,
    val gstRatePercent: Double // 0, 5, 12, 18, 28
) {
    val subtotal: Double get() = quantity * rate
    val taxAmount: Double get() = subtotal * (gstRatePercent / 100.0)
    val totalAmount: Double get() = subtotal + taxAmount
}

data class Invoice(
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String = "",
    val gstin: String = "",
    val dateEpochMs: Long,
    val items: List<InvoiceLineItem>,
    val isPaid: Boolean = true
) {
    val totalSubtotal: Double get() = items.sumOf { it.subtotal }
    val totalTax: Double get() = items.sumOf { it.taxAmount }
    val grandTotal: Double get() = items.sumOf { it.totalAmount }
}

enum class KhataType {
    GAVE_UDHAAR, // You gave money/credit (Asset/Receivable)
    GOT_JAMA     // You received money (Credit/Payment)
}

data class KhataEntry(
    val id: String,
    val partyName: String,
    val partyPhone: String = "",
    val amount: Double,
    val type: KhataType,
    val note: String = "",
    val dateEpochMs: Long
)

data class InventoryItem(
    val id: String,
    val name: String,
    val category: String,
    val quantity: Int,
    val minThresholdAlert: Int = 5,
    val costPrice: Double,
    val sellingPrice: Double
) {
    val isLowStock: Boolean get() = quantity <= minThresholdAlert
}
