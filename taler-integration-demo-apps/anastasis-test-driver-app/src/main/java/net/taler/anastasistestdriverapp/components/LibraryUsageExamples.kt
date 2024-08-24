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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import net.taler.anastasislib.contract.AnastasisActivityResultContracts
import net.taler.anastasislib.model.AnastasisBackupResult
import net.taler.anastasislib.model.AnastasisRecoveryResult
import net.taler.anastasislib.utils.AnastasisFileUtils
import net.taler.anastasistestdriverapp.BuildConfig
import java.io.File

@Composable
fun LibraryUsageBackupExample(modifier: Modifier = Modifier) {
    val appId = "net.taler.anastasistestdriverapp"
    val appName = "Gmail App"
    val secretName = "Account password"
    val expirationDateTime = "2030-01-01T08:30:15"

    val context = LocalContext.current
    val file = File(
        AnastasisFileUtils.getBackupFilesDir(context),
        "example-lib-secret-file.txt"
    )
    file.writeText(
        "Long secret data included in the file.\n" +
                "Email: TestAcc@gmail.com\n" +
                "Password: 123456789"
    )
    val secretFileContentUri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        file,
    )

    var backupResult by remember { mutableStateOf("") }
    val anastasisLauncher = rememberLauncherForActivityResult(
        contract = AnastasisActivityResultContracts.BackupSecret(
            appId = appId,
            appName = appName,
            secretName = secretName,
            secretFileUri = secretFileContentUri,
            expirationDateTime = expirationDateTime,
        )
    ) { result ->
        backupResult = when (result) {
            is AnastasisBackupResult.Success -> {
                "${result.message}\n" +
                        "Secret name: ${result.secretName}\n" +
                        "Expiration: ${result.expirationDateTime}"
            }

            is AnastasisBackupResult.Error -> {
                "${result.message}"
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            "Library Usage Example (Backup)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
        Button(
            onClick = {
                anastasisLauncher.launch()
            },
        ) {
            Text("Back up using Anastasis")
        }
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

@Composable
fun LibraryUsageRecoveryExample(modifier: Modifier = Modifier) {
    val appId = "net.taler.anastasistestdriverapp"
    val appName = "Gmail App"
    val secretName = "Account password"

    var recoveryResult by remember { mutableStateOf("") }
    val anastasisLauncher = rememberLauncherForActivityResult(
        contract = AnastasisActivityResultContracts.RecoverSecret(
            appId = appId,
            appName = appName,
            secretName = secretName,
        )
    ) { result ->
        recoveryResult = when (result) {
            is AnastasisRecoveryResult.Success -> {
                "${result.message}\n Secret file content URI: ${result.secretFileContentUri}"
            }

            is AnastasisRecoveryResult.Error -> {
                "${result.message}"
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            "Library Usage Example (Recovery)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
        Button(
            onClick = {
                anastasisLauncher.launch()
            },
        ) {
            Text("Recover using Anastasis")
        }
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
