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

package net.taler.anastasistestdriverapp.utils

import android.net.Uri

object Utils {
    private const val encTableCrock = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"

    fun encodeSecretToBase32(bytes: ByteArray): String {
        var sb = ""
        val size = bytes.size
        var bitBuf = 0
        var numBits = 0
        var pos = 0
        while (pos < size || numBits > 0) {
            if (pos < size && numBits < 5) {
                val d = bytes[pos++]
                bitBuf = bitBuf.shl(8).or(d.toInt())
                numBits += 8
            }
            if (numBits < 5) {
                // zero-padding
                bitBuf = bitBuf.shl(5 - numBits)
                numBits = 5
            }
            val v = bitBuf.ushr(numBits - 5).and(31)
            sb += encTableCrock[v]
            numBits -= 5
        }
        return sb
    }

    fun resolveDocFilename(uri: Uri): String? = uri.lastPathSegment
}
