package com.example.visualmoney.newAsset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.visualmoney.newAsset.event.ManualAssetInputEvent
import kotlin.math.max

class NewAssetViewModel: ViewModel() {
    var fixedAssetInputState by mutableStateOf(ManualAssetInputState())
        private set


    fun onFixedAssetInputEvent(event: ManualAssetInputEvent){
        when (event) {
            is ManualAssetInputEvent.NameChanged -> {
                fixedAssetInputState = fixedAssetInputState.copy(name = event.value)
                recalc()
            }

            is ManualAssetInputEvent.QuantityChanged -> {
                // allow empty while typing; sanitize later
                fixedAssetInputState = fixedAssetInputState.copy(quantityText = event.value)
                recalc()
            }

            is ManualAssetInputEvent.UnitPriceChanged -> {
                fixedAssetInputState = fixedAssetInputState.copy(unitPriceText = event.value)
                recalc()
            }

            is ManualAssetInputEvent.PurchaseDateChanged -> {
                fixedAssetInputState = fixedAssetInputState.copy(purchaseDate = event.value)
                recalc()
            }

            is ManualAssetInputEvent.CountryChanged -> {
                fixedAssetInputState = fixedAssetInputState.copy(country = event.value)
                recalc()
            }

            is ManualAssetInputEvent.SectorChanged -> {
                fixedAssetInputState = fixedAssetInputState.copy(sector = event.value)
                recalc()
            }

            ManualAssetInputEvent.Submit -> {
                // MVP: just validate and you’d persist/create the asset
                val s = fixedAssetInputState
                if (!s.canSubmit) {
                    fixedAssetInputState = s.copy(error = s.error ?: "Please complete required fields.")
                    return
                }

                val qty = parseQuantity(s.quantityText)
                val unitPrice = parseMoney(s.unitPriceText)

                // TODO: Persist your asset (repository call)
                // createManualAsset(name=s.name, quantity=qty, unitPrice=unitPrice, date=s.purchaseDate, ...)

                // Optional: reset
                fixedAssetInputState = ManualAssetInputState()
            }
        }

    }
    private fun recalc() {
        val s = fixedAssetInputState

        val nameOk = s.name.trim().isNotEmpty()
        val qty = parseQuantity(s.quantityText)
        val price = parseMoney(s.unitPriceText)

        val qtyOk = qty > 0
        val priceOk = price > 0.0

        val total = qty * price

        val error = when {
            !nameOk -> "Name is required."
            !qtyOk -> "Quantity must be at least 1."
            !priceOk -> "Enter a valid price."
            else -> null
        }

        fixedAssetInputState = s.copy(
            computedTotalValue = total,
            canSubmit = (error == null),
            error = null // clear error as user edits; keep only on submit if you want
        )
    }

    private fun parseQuantity(text: String): Int {
        // Treat empty as 0 while typing
        val n = text.trim().toIntOrNull() ?: 0
        return max(0, n)
    }

    private fun parseMoney(text: String): Double {
        // Basic parser: supports "12.34" and also "12,34"
        val normalized = text.trim().replace(',', '.')
        return normalized.toDoubleOrNull() ?: 0.0
    }
}