/*
 * Copyright (C) 2014 Ferid Cafer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ferid.app.frequentcontacts.prefs

import android.content.Context
import com.ferid.app.frequentcontacts.list.Contact
import java.io.*

object StorageManager {

    /**
     * Save contact accordingly with the given index
     * @param context Context
     * @param contact Contact
     * @param index Index
     */
    fun saveContact(context: Context, contact: Contact, index: Int) {
        var outputStream: OutputStream? = null

        try {
            outputStream = context.openFileOutput("contact_$index", Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(outputStream)
            oos.writeObject(contact)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush()
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Read contact of the given index
     * @param context Context
     * @param index Index
     */
    fun readContact(context: Context, index: Int): Contact? {
        var contact: Contact? = null
        var fis: FileInputStream? = null

        try {
            fis = context.openFileInput("contact_$index")
            val ois = ObjectInputStream(fis)
            contact = ois.readObject() as Contact
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        return contact
    }

    /**
     * Delete contact according to the given index
     */
    fun deleteContact(context: Context, index: Int) {
        context.deleteFile("contact_$index")
    }
}