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

package net.taler.anastasislib.model

import android.net.Uri

sealed class AnastasisBackupResult(
    open val resultCode: Int,
    open val message: String?,
) {
    data class Success(
        override val resultCode: Int,
        override val message: String? = null,
        val secretName: String,
        val expirationDateTime: String,
    ) : AnastasisBackupResult(resultCode, message)

    data class Error(
        override val resultCode: Int,
        override val message: String? = null,
    ) : AnastasisBackupResult(resultCode, message)
}

sealed class AnastasisRecoveryResult(
    open val resultCode: Int,
    open val message: String?,
) {
    data class Success(
        override val resultCode: Int,
        override val message: String? = null,
        val secretFileContentUri: Uri,
    ) : AnastasisRecoveryResult(resultCode, message)

    data class Error(
        override val resultCode: Int,
        override val message: String? = null,
    ) : AnastasisRecoveryResult(resultCode, message)
}

object AnastasisResultCode {
    /**
     * Similar to Activity.CANCELED.
     */
    const val ERROR_USER_ABORTED = 0

    /**
     * Invalid or missing data received from the caller app.
     */
    const val ERROR_INVALID_DATA = 1

    /**
     * Internal error in Anastasis.
     */
    const val ERROR_INTERNAL = 2

    /**
     * Secret not found.
     */
    const val SECRET_NOT_FOUND = 3

    /**
     * Backup success.
     */
    const val BACKUP_SUCCESS = 100

    /**
     * Recovery success.
     */
    const val RECOVERY_SUCCESS = 101
}