/*
 * This file is part of GNU Taler
 * (C) 2024 Taler Systems S.A.
 *
 * GNU Taler is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * GNU Taler is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GNU Taler; see the file COPYING.  If not, see <http://www.gnu.org/licenses/>
 */

package net.taler.wallettestdriverapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.taler.wallettestdriverapp.ui.theme.TalerandroidTheme
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TalerandroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        SinglePaymentExample()
        Divider(Modifier.padding(vertical = 8.dp))
        MultiPaymentExample()
    }
}

@Composable
private fun SinglePaymentExample() {
    val coroutineScope = rememberCoroutineScope();
    var price by remember { mutableStateOf("0") }
    var isCreatingOrder by remember { mutableStateOf(false) }
    var paymentResult by remember { mutableStateOf("") }
    val walletLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        when (activityResult.resultCode) {
            -1 -> paymentResult = "No status yet"
            0 -> paymentResult = "User aborted the payment"
            1 -> paymentResult = "Insufficient balance"
            2 -> paymentResult = "Payment was still pending"
            3 -> paymentResult = "Payment failed"
            4 -> paymentResult = "Already paid"
            100 -> paymentResult = "Payment succeeded"
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Single-Payment Example")
        TextField(
            value = price,
            onValueChange = { price = it },
            label = { Text(text = "Order price (KUDOS)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(8.dp)
        )
        Button(onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                isCreatingOrder = true
                val orderID = createOrder(
                    merchantUrl = "https://backend.demo.taler.net/private/orders",
                    price = price.toDouble(),
                    currency = "KUDOS",
                    summary = "Donate to make the wallet works",
                )
                isCreatingOrder = false
                val orderTalerUri = "taler://pay/backend.demo.taler.net/$orderID/"
                Uri.parse(orderTalerUri).let { uri ->
                    val intent = Intent().apply {
                        action = "android.intent.action.VIEW"
                        data = uri
                    }
                    Log.d("wallet-test-driver-app", intent.toString())
                    walletLauncher.launch(Intent.createChooser(intent, "Choose App"))
                }
            }
        }) {
            Text("Create the order")
        }
        if (isCreatingOrder) {
            CircularProgressIndicator()
        }
        Text(text = paymentResult, modifier = Modifier.padding(8.dp))
    }
}

@Composable
private fun MultiPaymentExample() {
    val coroutineScope = rememberCoroutineScope();
    var order1Price by remember { mutableStateOf("0") }
    var order2Price by remember { mutableStateOf("0") }
    var order3Price by remember { mutableStateOf("0") }
    var isCreatingOrders by remember { mutableStateOf(false) }
    var paymentResult by remember { mutableStateOf("") }
    val walletLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        when (activityResult.resultCode) {
            0 -> paymentResult = "User aborted the payments."
            5 -> paymentResult = "Missing Data: Empty list of Taler pay URIs."
            3 -> {
                val payUrisToStatus =
                    activityResult.data?.getSerializableExtra("results") as HashMap<String, Int>
                paymentResult = "Failure: \n$payUrisToStatus"
            }

            100 -> paymentResult = "All payments succeeded"
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Multi-Payment Example")
        TextField(
            value = order1Price,
            onValueChange = { order1Price = it },
            label = { Text(text = "Order 1 price (KUDOS)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(8.dp)
        )
        TextField(
            value = order2Price,
            onValueChange = { order2Price = it },
            label = { Text(text = "Order 2 price (TESTKUDOS)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(8.dp)
        )
        TextField(
            value = order3Price,
            onValueChange = { order3Price = it },
            label = { Text(text = "Order 3 price (TESTKUDOS)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(8.dp)
        )
        Button(onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                isCreatingOrders = true
                val order1ID = createOrder(
                    merchantUrl = "https://backend.demo.taler.net/private/orders",
                    price = order1Price.toDouble(),
                    currency = "KUDOS",
                    summary = "Payment A"
                )
                val order2ID = createOrder(
                    merchantUrl = "https://backend.test.taler.net/private/orders",
                    price = order2Price.toDouble(),
                    currency = "TESTKUDOS",
                    summary = "Payment B"
                )
                val order3ID = createOrder(
                    merchantUrl = "https://backend.test.taler.net/private/orders",
                    price = order3Price.toDouble(),
                    currency = "TESTKUDOS",
                    summary = "Payment C"
                )
                isCreatingOrders = false
                val orderTalerUri1 = "taler://pay/backend.demo.taler.net/$order1ID/"
                val orderTalerUri2 = "taler://pay/backend.test.taler.net/$order2ID/"
                val orderTalerUri3 = "taler://pay/backend.test.taler.net/$order3ID/"
                val intent = Intent().apply {
                    action = "net.taler.wallet.MULTI_PAYMENT"
                    putStringArrayListExtra(
                        "talerUris",
                        arrayListOf(orderTalerUri1, orderTalerUri2, orderTalerUri3)
                    )
                }
                Log.d("wallet-test-driver-app", "$intent")
                walletLauncher.launch(Intent.createChooser(intent, "Choose App"))
            }
        }) {
            Text("Create the orders")
        }
        if (isCreatingOrders) {
            CircularProgressIndicator()
        }
        Text(text = paymentResult, modifier = Modifier.padding(8.dp))
    }
}

fun createOrder(
    merchantUrl: String,
    price: Double,
    currency: String,
    summary: String,
): String {
    val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val bodyContent = JSONObject().apply {
        put("order", JSONObject().apply {
            put("amount", "$currency:$price")
            put("summary", summary)
        })
        put("create_token", false)
    }.toString()

    val body: RequestBody = bodyContent.toRequestBody(jsonMediaType)

    val request = Request.Builder()
        .url(merchantUrl)
        .addHeader("Authorization", "Bearer secret-token:sandbox")
        .post(body)
        .build()

    val response = OkHttpClient().newCall(request).execute()
    if (response.isSuccessful) {
        val orderID = JSONObject(response.body!!.string()).getString("order_id")
        response.close()
        Log.d("wallet-test-driver-app", orderID)
        return orderID
    } else {
        Log.d("wallet-test-driver-app", "Request failed: $response")
        throw Exception()
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TalerandroidTheme {
        HomeScreen()
    }
}