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

package net.taler.anastasistestdriverapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.taler.anastasistestdriverapp.components.ExternalFileExample
import net.taler.anastasistestdriverapp.components.LibraryUsageBackupExample
import net.taler.anastasistestdriverapp.components.LibraryUsageRecoveryExample
import net.taler.anastasistestdriverapp.components.OwnFileExample
import net.taler.anastasistestdriverapp.components.RecoveryExample
import net.taler.anastasistestdriverapp.components.TextExample
import net.taler.anastasistestdriverapp.ui.theme.AnastasisTestDriverAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnastasisTestDriverAppTheme {
                Scaffold(Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TextExample()
        Divider(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        )
        ExternalFileExample()
        Divider(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        )
        OwnFileExample()
        Divider(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        )
        RecoveryExample()
        Divider(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        )
        LibraryUsageBackupExample()
        Divider(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        )
        LibraryUsageRecoveryExample()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    AnastasisTestDriverAppTheme {
        HomeScreen()
    }
}
