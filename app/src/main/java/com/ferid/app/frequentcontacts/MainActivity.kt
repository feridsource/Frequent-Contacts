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

package com.ferid.app.frequentcontacts

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ferid.app.frequentcontacts.list.Contact
import com.ferid.app.frequentcontacts.prefs.StorageManager
import com.ferid.app.frequentcontacts.selectnumber.SelectNumberActivity
import com.ferid.app.frequentcontacts.widget.ContactsWidget
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        setContactsListener()

        readList()
    }

    /**
     * Get all frequent contacts list
     */
    private fun readList() {
        for (i in 1..4) {
            readContact(i)
        }
    }

    /**
     * Set contacts
     */
    private fun setContactsListener() {
        ivContact1.setOnClickListener{
            addNewContact(1)
        }
        ivContact1.setOnLongClickListener{
            selectPhoto(11)
            true
        }

        ivContact2.setOnClickListener{
            addNewContact(2)
        }
        ivContact2.setOnLongClickListener{
            selectPhoto(12)
            true
        }

        ivContact3.setOnClickListener{
            addNewContact(3)
        }
        ivContact3.setOnLongClickListener{
            selectPhoto(13)
            true
        }

        ivContact4.setOnClickListener{
            addNewContact(4)
        }
        ivContact4.setOnLongClickListener{
            selectPhoto(14)
            true
        }
    }

    /**
     * Select a photo from the gallery
     */
    private fun selectPhoto(index: Int) {
        val intent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, index)
    }

    /**
     * Adds a new contact to the application list.<br />
     * Contacts are selected from the phone's contats' list.
     * @param contactIndex one of the four indices of the list
     */
    private fun addNewContact(contactIndex: Int) {
        val intent = Intent(context, SelectNumberActivity::class.java)
        startActivityForResult(intent, contactIndex)
        overridePendingTransition(R.anim.move_in_from_bottom, R.anim.stand_still)
    }

    /**
     * Set the chosen contact info
     */
    private fun setContactInfo(contact: Contact, imageView: ImageView, name: TextView) {
        if (!TextUtils.isEmpty(contact.number)) {
            //set name
            name.text = contact.name

            //set photo
            try {
                val contactPhoto: ByteArray = Base64.decode(contact.photo, 0)
                val bitmap = BitmapFactory.decodeByteArray(contactPhoto, 0, contactPhoto.size)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(R.drawable.ic_person)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            imageView.setImageResource(R.drawable.ic_action_add)
            name.text = ""
        }
    }

    /**
     * Arrange contact index and set info accordingly
     */
    private fun arrangeContactIndex(contact: Contact, index: Int) {
        when (index) {
            1 -> {
                setContactInfo(contact, ivContact1, tvName1)
            }
            2 -> {
                setContactInfo(contact, ivContact2, tvName2)
            }
            3 -> {
                setContactInfo(contact, ivContact3, tvName3)
            }
            4 -> {
                setContactInfo(contact, ivContact4, tvName4)
            }
        }
    }

    /**
     * Read contact
     */
    private fun readContact(index: Int) {
        val contact: Contact? = StorageManager.readContact(context, index)
        if (contact != null) {
            arrangeContactIndex(contact, index)
        }
    }

    /**
     * Save contact
     */
    private fun saveContact(contact: Contact, index: Int) {
        if (TextUtils.isEmpty(contact.number)) {    //remove current contact
            StorageManager.deleteContact(context, index)
        } else {    //add a new contact
            StorageManager.saveContact(context, contact, index)
        }

        //refresh photo
        arrangeContactIndex(contact, index)

        updateContactsWidget()
    }

    /**
     * Updates contacts widget
     */
    private fun updateContactsWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidget = ComponentName(context.packageName,
                this.javaClass.name)
        val updateWidget = Intent(context, ContactsWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
        updateWidget.action = ContactsWidget.APP_TO_WID
        updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        context.sendBroadcast(updateWidget)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode in 1..4) { //for contact
            lateinit var contact: Contact
            if (resultCode == Activity.RESULT_OK) {
                try {
                    contact = data!!.getSerializableExtra("contact") as Contact

                    saveContact(contact, requestCode)

                    arrangeContactIndex(contact, requestCode)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode in 11..14) { //for photo
            //a photo picked from the gallery
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val photoUri: Uri? = data!!.data
                    var inputStream: InputStream? = this.contentResolver.openInputStream(photoUri!!)
                    val dbo = BitmapFactory.Options()
                    dbo.inJustDecodeBounds = true
                    BitmapFactory.decodeStream(inputStream, null, dbo)
                    inputStream?.close()
                    val srcBitmap: Bitmap?
                    inputStream = this.contentResolver.openInputStream(photoUri)

                    val maxImageHeight = 400.0
                    val maxImageWidth = 300.0
                    if (dbo.outWidth > maxImageWidth || dbo.outHeight > maxImageHeight) {
                        val widthRatio = dbo.outWidth.toFloat() / maxImageWidth
                        val heightRatio = dbo.outHeight.toFloat() / maxImageHeight
                        val maxRatio = widthRatio.coerceAtLeast(heightRatio)

                        // Create the bitmap from file
                        val options = BitmapFactory.Options()
                        options.inSampleSize = maxRatio.toInt()
                        srcBitmap = BitmapFactory.decodeStream(inputStream, null, options)
                    } else {
                        srcBitmap = BitmapFactory.decodeStream(inputStream)
                    }

                    inputStream?.close()

                    val stream = ByteArrayOutputStream()
                    srcBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val byteArray: ByteArray = stream.toByteArray()

                    val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)

                    //anti-memory leak
                    srcBitmap.recycle()

                    //ready to set photo
                    val contact: Contact? = StorageManager.readContact(context, requestCode - 10)
                    contact!!.photo = encodedImage
                    saveContact(contact, requestCode - 10)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

}