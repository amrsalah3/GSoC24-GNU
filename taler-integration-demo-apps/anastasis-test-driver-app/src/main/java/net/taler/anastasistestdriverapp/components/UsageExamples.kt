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

package net.taler.anastasistestdriverapp.components

import android.app.PendingIntent
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import net.taler.anastasistestdriverapp.utils.Utils
import java.io.File

@Composable
fun TextExample(modifier: Modifier = Modifier) {
    var appId by remember {
        mutableStateOf("net.taler.anastasistestdriverapp")
    }

    var appName by remember {
        mutableStateOf("Gmail App")
    }

    var secretName by remember {
        mutableStateOf("Account password")
    }

    var secret by remember {
        mutableStateOf("123456")
    }

    var expirationDateTime by remember {
        mutableStateOf("2030-01-01T08:30:15")
    }

    val uri by remember {
        derivedStateOf {
            val uriBuilder = Uri.Builder()
                .scheme("anastasis")
                .authority("backup")
                .appendEncodedPath(appId) // appId is actually not encoded and we don't want to.
                .appendPath(secretName)
                .appendQueryParameter(
                    "secret",
                    Utils.encodeSecretToBase32(secret.toByteArray())
                )
            if (appName.isNotBlank()) {
                uriBuilder.appendQueryParameter("app_name", appName)
            }
            if (expirationDateTime.isNotBlank()) {
                uriBuilder.appendQueryParameter("expiration_datetime", expirationDateTime)
            }
            uriBuilder.build()
        }
    }

    var backupResult by remember { mutableStateOf("") }
    val anastasisLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val data = activityResult.data?.data?.pathSegments
        when (activityResult.resultCode) {
            0 -> backupResult = "User aborted the process"
            1 -> backupResult = "Invalid input: ${data!![1]}"
            2 -> backupResult = "Internal error occurred"
            100 -> backupResult = "Backup succeeded: \n" +
                    "(secret name: ${data!![1]}) \n" +
                    "(expiration date: ${data[2]})"
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            "Text Example",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            modifier = Modifier.padding(16.dp),
        )

        TextField(
            label = {
                Text(text = "App ID")
            },
            value = appId,
            onValueChange = { appId = it },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            label = {
                Text(text = "App Name (Optional)")
            },
            value = appName,
            onValueChange = { appName = it },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            label = {
                Text(text = "Secret Name")
            },
            value = secretName,
            onValueChange = { secretName = it },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            label = {
                Text(text = "Secret")
            },
            value = secret,
            onValueChange = { secret = it },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            label = {
                Text(text = "Expiration date-time (Optional)")
            },
            value = expirationDateTime,
            onValueChange = { expirationDateTime = it },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            "URI:",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            "$uri",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        val context = LocalContext.current
        Button(
            onClick = {
                val intent = Intent("anastasis.intent.action.backup").run {
                    data = uri
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        Intent(),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    putExtra("pending_intent", pendingIntent)
                    Intent.createChooser(this, "")
                }
                anastasisLauncher.launch(intent)
            },
        ) {
            Text("Back up using Anastasis")
        }

        Text(
            text = backupResult,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun ExternalFileExample(modifier: Modifier = Modifier) {
    var secretFileContentUri by remember { mutableStateOf<Uri?>(null) }
    val uri by remember {
        derivedStateOf {
            Uri.Builder()
                .scheme("anastasis")
                .authority("backup")
                .appendEncodedPath("net.taler.anastasistestdriverapp") // appId is actually not encoded and we don't want to.
                .appendPath("Account Credentials")
                .build()
        }
    }

    var backupResult by remember { mutableStateOf("") }
    val anastasisLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val data = activityResult.data?.data?.pathSegments
        when (activityResult.resultCode) {
            0 -> backupResult = "User aborted the process"
            1 -> backupResult = "Invalid input: ${data!![1]}"
            2 -> backupResult = "Internal error occurred"
            100 -> backupResult = "Backup succeeded: \n" +
                    "(secret name: ${data!![1]}) \n" +
                    "(expiration date: ${data[2]})"
        }
    }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            it?.let { uri -> secretFileContentUri = uri }
        }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {

        Text(
            "External File Example",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        OutlinedButton(
            onClick = { filePickerLauncher.launch("*/*") },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Pick a file")
        }

        SelectionContainer {
            Text(
                text = secretFileContentUri?.toString() ?: "No selected file",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )
        }

        val context = LocalContext.current
        Button(
            onClick = {
                val intent = Intent("anastasis.intent.action.backup").run {
                    data = uri
                    clipData = ClipData.newRawUri("", secretFileContentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        Intent(),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    putExtra("pending_intent", pendingIntent)
                    Intent.createChooser(this, "")
                }
                anastasisLauncher.launch(intent)
            },
        ) {
            Text("Back up using Anastasis")
        }

        Text(
            text = backupResult,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun OwnFileExample(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uri by remember {
        derivedStateOf {
            Uri.Builder()
                .scheme("anastasis")
                .authority("backup")
                .appendEncodedPath("net.taler.anastasistestdriverapp") // appId is actually not encoded and we don't want to.
                .appendPath("Account password")
                .build()
        }
    }

    var backupResult by remember { mutableStateOf("") }
    val anastasisLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val data = activityResult.data?.data?.pathSegments
        when (activityResult.resultCode) {
            0 -> backupResult = "User aborted the process"
            1 -> backupResult = "Invalid input: ${data!![1]}"
            2 -> backupResult = "Internal error occurred"
            100 -> backupResult = "Backup succeeded: \n" +
                    "(secret name: ${data!![1]}) \n" +
                    "(expiration date: ${data!![2]})"
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            "Own File Example",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        val file = File(context.cacheDir, "example-secret-file.txt")
        file.writeText(
            "Long secret data included in the file.\n" +
                    "Email: TestAcc@gmail.com\n" +
                    "Password: 123456789"
        )

        val secretFileContentUri = FileProvider.getUriForFile(
            context,
            "net.taler.anastasistestdriverapp.fileprovider",
            file,
        )

        SelectionContainer {
            Text(
                text = secretFileContentUri!!.toString(),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }

        Button(
            onClick = {
                val intent = Intent("anastasis.intent.action.backup").run {
                    data = uri
                    clipData = ClipData.newRawUri("", secretFileContentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        Intent(),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    putExtra("pending_intent", pendingIntent)
                    Intent.createChooser(this, "")
                }
                anastasisLauncher.launch(intent)
            },
        ) {
            Text("Back up using Anastasis")
        }

        Text(
            text = backupResult,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun RecoveryExample(modifier: Modifier = Modifier) {
    var appId by remember {
        mutableStateOf("net.taler.anastasistestdriverapp")
    }

    var appName by remember {
        mutableStateOf("Gmail App")
    }

    var secretName by remember {
        mutableStateOf("Account password")
    }

    val uri by remember {
        derivedStateOf {
            val uriBuilder = Uri.Builder()
                .scheme("anastasis")
                .authority("recovery")
            if (appId.isNotBlank()) {
                uriBuilder.appendEncodedPath(appId)  // appId is actually not encoded and we don't want to.
            }
            if (appId.isNotBlank() && secretName.isNotBlank()) {
                uriBuilder.appendPath(secretName)
            }
            if (appName.isNotBlank()) {
                uriBuilder.appendQueryParameter("app_name", appName)
            }
            uriBuilder.build()
        }
    }

    var recoveryResult by remember { mutableStateOf("") }
    val anastasisLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val data = activityResult.data?.data?.pathSegments
        when (activityResult.resultCode) {
            0 -> recoveryResult = "User aborted the process"
            1 -> recoveryResult = "Invalid input: ${data!![1]}"
            2 -> recoveryResult = "Internal error occurred"
            3 -> recoveryResult = "Secret not found."
            101 -> {
                val secretUri = activityResult.data?.clipData?.getItemAt(0)?.uri
                recoveryResult = "Recovered secret Content URI: $secretUri\n" +
                        "File name: ${Utils.resolveDocFilename(secretUri!!)}\n"
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            "Recovery Example",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            modifier = Modifier.padding(16.dp),
        )

        TextField(
            label = {
                Text(text = "App ID")
            },
            value = appId,
            onValueChange = { appId = it },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            label = {
                Text(text = "App Name (Optional)")
            },
            value = appName,
            onValueChange = { appName = it },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            label = {
                Text(text = "Secret Name")
            },
            value = secretName,
            onValueChange = { secretName = it },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            "URI:",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            "$uri",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        val context = LocalContext.current
        Button(
            onClick = {
                val intent = Intent("anastasis.intent.action.recovery").run {
                    data = uri
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        Intent(),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    putExtra("pending_intent", pendingIntent)
                    Intent.createChooser(this, "")
                }
                anastasisLauncher.launch(intent)
            },
        ) {
            Text("Recover using Anastasis")
        }

        Text(
            text = recoveryResult,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )
    }
}
