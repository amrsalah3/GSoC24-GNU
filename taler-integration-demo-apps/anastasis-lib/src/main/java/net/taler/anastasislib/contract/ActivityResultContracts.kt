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

package net.taler.anastasislib.contract

import android.app.PendingIntent
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import net.taler.anastasislib.model.AnastasisBackupResult
import net.taler.anastasislib.model.AnastasisRecoveryResult
import net.taler.anastasislib.model.AnastasisResultCode
import net.taler.anastasislib.utils.SecretCodec

class AnastasisActivityResultContracts {

    /**
     * Contract for launching Anastasis for backup.
     */
    class BackupSecret private constructor(
        private val appId: String?,
        private val appName: String?,
        private val secretName: String,
        private val secretText: String?,
        private val secretFileContentUri: Uri?,
        private val expirationDateTime: String?,
    ) : ActivityResultContract<Unit, AnastasisBackupResult>() {

        constructor(
            appId: String? = null,
            appName: String? = null,
            secretName: String,
            secretText: String,
            expirationDateTime: String? = null
        ) : this(appId, appName, secretName, secretText, null, expirationDateTime)

        constructor(
            appId: String? = null,
            appName: String? = null,
            secretName: String,
            secretFileUri: Uri,
            expirationDateTime: String? = null
        ) : this(appId, appName, secretName, null, secretFileUri, expirationDateTime)

        override fun createIntent(context: Context, input: Unit): Intent {
            val uriBuilder = Uri.Builder()
                .scheme("anastasis")
                .authority("backup")
                .appendEncodedPath(appId)
                .appendPath(secretName)

            if (!secretText.isNullOrBlank()) {
                uriBuilder.appendQueryParameter(
                    "secret",
                    SecretCodec.encode(secretText.toByteArray())
                )
            }
            if (!appName.isNullOrBlank()) {
                uriBuilder.appendQueryParameter("app_name", appName)
            }
            if (!expirationDateTime.isNullOrEmpty()) {
                uriBuilder.appendQueryParameter("expiration_datetime", expirationDateTime)
            }

            val intent = Intent("anastasis.intent.action.backup").run {
                data = uriBuilder.build()
                if (secretFileContentUri != null) {
                    clipData = ClipData.newRawUri("", secretFileContentUri)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(),
                    PendingIntent.FLAG_IMMUTABLE
                )
                putExtra("pending_intent", pendingIntent)
                Intent.createChooser(this, "")
            }

            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): AnastasisBackupResult {
            var resultData: AnastasisBackupResult
            try {
                val data = intent?.data?.pathSegments
                when (resultCode) {
                    AnastasisResultCode.BACKUP_SUCCESS -> {
                        resultData = AnastasisBackupResult.Success(
                            resultCode = resultCode,
                            secretName = data!![1],
                            expirationDateTime = data[2],
                            message = data[3],
                        )
                    }

                    else -> {
                        resultData = AnastasisBackupResult.Error(
                            resultCode = resultCode,
                            message = data!![1],
                        )
                    }
                }
            } catch (e: Exception) {
                resultData = AnastasisBackupResult.Error(
                    resultCode = AnastasisResultCode.ERROR_INTERNAL,
                    message = "Internal error occurred while handling the result."
                )
            }

            return resultData
        }
    }

    /**
     * Contract for launching Anastasis for recovery.
     */
    class RecoverSecret(
        private val _appId: String? = null,
        private val _appName: String? = null,
        private val _secretName: String? = null,
        private val _isPrimaryConstructor: Boolean, /* Just to resolve JVM constructors clash */
    ) : ActivityResultContract<Unit, AnastasisRecoveryResult>() {

        constructor(
            appId: String,
            appName: String? = null,
            secretName: String,
        ) : this(
            _appId = appId,
            _appName = appName,
            _secretName = secretName,
            _isPrimaryConstructor = false
        )

        constructor(
            appId: String? = null,
            appName: String? = null
        ) : this(
            _appId = appId,
            _appName = appName,
            _isPrimaryConstructor = false
        )

        override fun createIntent(context: Context, input: Unit): Intent {
            val uriBuilder = Uri.Builder()
                .scheme("anastasis")
                .authority("recovery")

            if (!_appId.isNullOrBlank()) {
                uriBuilder.appendEncodedPath(_appId)
                // Secret name can only be provided if an app id is provided
                if (!_secretName.isNullOrBlank()) {
                    uriBuilder.appendPath(_secretName)
                }
            }
            if (!_appName.isNullOrBlank()) {
                uriBuilder.appendQueryParameter("app_name", _appName)
            }

            val intent = Intent("anastasis.intent.action.recovery").run {
                data = uriBuilder.build()
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(),
                    PendingIntent.FLAG_IMMUTABLE
                )
                putExtra("pending_intent", pendingIntent)
                Intent.createChooser(this, "")
            }

            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): AnastasisRecoveryResult {
            var resultData: AnastasisRecoveryResult
            try {
                val data = intent?.data?.pathSegments
                when (resultCode) {
                    AnastasisResultCode.RECOVERY_SUCCESS -> {
                        resultData = AnastasisRecoveryResult.Success(
                            resultCode = resultCode,
                            secretFileContentUri = intent?.clipData?.getItemAt(0)!!.uri,
                            message = data!![1],
                        )
                    }

                    else -> {
                        resultData = AnastasisRecoveryResult.Error(
                            resultCode = resultCode,
                            message = data!![1],
                        )
                    }
                }
            } catch (e: Exception) {
                resultData = AnastasisRecoveryResult.Error(
                    resultCode = AnastasisResultCode.ERROR_INTERNAL,
                    message = "Internal error occurred while handling the result."
                )
            }

            return resultData
        }
    }
}
