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

package net.taler.anastasislib.utils

import android.content.Context
import android.net.Uri
import java.io.File

object AnastasisFileUtils {
    /**
     * Returns the directory in which caller apps can use to create their own files to be backed up.
     */
    fun getBackupFilesDir(context: Context): File = context.cacheDir

    /**
     * Returns file name of a file content URI.
     */
    fun getFilename(uri: Uri): String? = uri.lastPathSegment

    /**
     * Returns the MIME type of a file content URI.
     */
    fun getFileMimeType(context: Context, uri: Uri): String? = context.contentResolver.getType(uri)

    /**
     * Returns the plain text content of a text file content URI.
     */
    fun getFileContent(context: Context, uri: Uri): String? =
        context.contentResolver.openInputStream(uri).use { it?.readBytes()?.decodeToString() }
}
