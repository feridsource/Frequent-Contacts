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

package com.ferid.app.frequentcontacts.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.View
import android.widget.RemoteViews
import com.ferid.app.frequentcontacts.MainActivity
import com.ferid.app.frequentcontacts.R
import com.ferid.app.frequentcontacts.list.Contact
import com.ferid.app.frequentcontacts.prefs.StorageManager

class ContactsWidget : AppWidgetProvider() {
    private var context: Context? = null
    private var remoteViews: RemoteViews? = null
    private var appWidgetManager: AppWidgetManager? = null
    private var thisWidget: ComponentName? = null

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        this.context = context
        this.appWidgetManager = appWidgetManager
        remoteViews = RemoteViews(context.packageName, R.layout.widget_contacts)
        thisWidget = ComponentName(context, ContactsWidget::class.java)
        contacts
    }

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        appWidgetManager = AppWidgetManager.getInstance(context)
        remoteViews = RemoteViews(context.packageName, R.layout.widget_contacts)
        thisWidget = ComponentName(context, ContactsWidget::class.java)
        if (intent.action == APP_TO_WID || intent.action == WIDGET_ENABLED) {
            if (remoteViews != null) {
                contacts
            }
        }
        super.onReceive(context, intent)
    }

    /**
     * Read frequently used contacts list
     */
    private val contacts: Unit
        get() {
            val contactsList: ArrayList<Contact> = readList()

            if (contactsList.size == 0) {
                remoteViews!!.setBoolean(R.id.layoutBackground, "setEnabled", false)

                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context!!.startActivity(intent)
            } else {
                for (i in 0..3) {
                    val imageId: String = "id/ivContact" + (i + 1)
                    val layoutId: String = "id/rlContact" + (i + 1)

                    if (contactsList.size > i) {
                        val contact: Contact = contactsList[i]

                        //if contact is set
                        if (!contact.photo.equals("")) {
                            //set photo if exits
                            setCustomPhoto(imageId, contact.photo!!)
                        } else {
                            //put default photo if there is no any
                            setDefaultPhoto(imageId)
                        }

                        //show item
                        setVisible(layoutId)

                        //call contact on click
                        setContactClickListener(imageId, contact.number!!)
                    } else {
                        //make its visibility gone
                        setInvisible(layoutId)
                    }
                }
            }

            appWidgetManager!!.updateAppWidget(thisWidget, remoteViews)
        }

    /**
     * Make item visible
     * @param componentId
     */
    private fun setVisible(componentId: String) {
        remoteViews!!.setViewVisibility(
                context!!.resources.getIdentifier(
                        componentId, null,
                        context!!.packageName), View.VISIBLE)
    }

    /**
     * Make item invisible
     * @param componentId
     */
    private fun setInvisible(componentId: String) {
        remoteViews!!.setViewVisibility(
                context!!.resources.getIdentifier(
                        componentId, null,
                        context!!.packageName), View.GONE)
    }

    /**
     * Set click listener for frequent contacts
     * @param componentId String
     * @param number String
     */
    private fun setContactClickListener(componentId: String, number: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$number")
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        remoteViews!!.setOnClickPendingIntent(context!!.resources.getIdentifier(
                componentId, null, context!!.packageName), pendingIntent)
    }

    /**
     * Put a default photo
     * @param componentId String
     */
    private fun setDefaultPhoto(componentId: String) {
        remoteViews!!.setImageViewResource(
                context!!.resources.getIdentifier(
                        componentId, null,
                        context!!.packageName), R.drawable.ic_person)
    }

    /**
     * Set its selected photo on the given contact
     * @param componentId String
     * @param photo String
     */
    private fun setCustomPhoto(componentId: String, photo: String) {
        val contactPhoto = Base64.decode(photo, 0)
        val bitmap = BitmapFactory.decodeByteArray(contactPhoto,
                0, contactPhoto.size)

        //setting the rounded-corner image to imageview
        if (bitmap != null) {
            remoteViews!!.setImageViewBitmap(
                    context!!.resources.getIdentifier(
                            componentId, null, context!!.packageName), bitmap)
        } else {
            setDefaultPhoto(componentId)
        }
    }

    /**
     * Get all frequent contacts list
     */
    private fun readList(): ArrayList<Contact> {
        val contactsList: ArrayList<Contact> = ArrayList()
        for (i in 1..4) {
            val contact: Contact? = StorageManager.readContact(context!!, i)
            if (contact != null) {
                contactsList.add(contact)
            }
        }

        return contactsList
    }

    companion object {
        //application triggers the widget
        const val APP_TO_WID = "com.ferid.app.frequentcontacts.widget.APP_TO_WID"
        //drag and drop triggers the widget
        const val WIDGET_ENABLED = "android.appwidget.action.APPWIDGET_ENABLED"
    }
}