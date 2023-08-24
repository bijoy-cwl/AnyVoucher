package com.anyvoucher.app.printer

data class PrintData(
    val type: String,
    val date: String,
    val invoiceId: String,
    val name: String,
    val mobile: String,
    val address: String,
    val product: String,
    val total: String,
    val note: String
)