/*
 * Copyright (C) 2015 Ferid Cafer
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

package com.ferid.app.frequentcontacts.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.RemoteViews;

import com.ferid.app.frequentcontacts.MainActivity;
import com.ferid.app.frequentcontacts.R;
import com.ferid.app.frequentcontacts.list.Contact;
import com.ferid.app.frequentcontacts.prefs.PrefsUtil;

import java.util.ArrayList;

/**
 * Created by ferid.cafer on 11/10/2014.
 */
public class FrequentContactsWidget extends AppWidgetProvider {
    private Context context;

    protected RemoteViews remoteViews;
    protected AppWidgetManager appWidgetManager;
    protected ComponentName thisWidget;

    //application triggers the widget
    public static final String APP_TO_WID = "com.ferid.app.frequentcontacts.widget.APP_TO_WID";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.frequent_contacts_widget);
        thisWidget = new ComponentName(context, FrequentContactsWidget.class);

        getContacts();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        this.appWidgetManager = appWidgetManager;
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.frequent_contacts_widget);
        thisWidget = new ComponentName(context, FrequentContactsWidget.class);

        if (intent.getAction().equals(APP_TO_WID)) {
            if (remoteViews != null) {
                getContacts();
            }
        }
        super.onReceive(context, intent);
    }

    /**
     * Read frequently used contacts list
     */
    private void getContacts() {
        ArrayList<Contact> contactsList = PrefsUtil.getInstance(context).readContacts();

        Intent intent = new Intent(Intent.ACTION_CALL);
        PendingIntent pendingIntent;

        for (int i = 0; i < context.getResources().getInteger(R.integer.maxContactSize); i++) {
            String componentId = "id/contact" + (i+1);

            if (i < contactsList.size()) {
                //if contact is set
                try {
                    if (!contactsList.get(i).getPhoto().equals("")) {
                        //set photo if exists
                        setCustomPhoto(componentId, contactsList.get(i).getPhoto());
                    } else {
                        //put default photo if there is no any
                        setDefaultPhoto(componentId);
                    }

                    //call contact on click
                    setFrequentContactClickListener(componentId, contactsList.get(i).getNumber());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //put default photo for empty items
                setDefaultPhoto(componentId);

                //if contact is not set, open main activity
                setEmptyItemClickListener(componentId);
            }
        }

        remoteViews.setBoolean(R.id.layoutBackground, "setEnabled", true);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

    }

    /**
     * Put a default photo
     * @param componentId String
     */
    private void setDefaultPhoto(String componentId) {
        remoteViews.setImageViewResource(
                context.getResources().getIdentifier(
                        componentId, null,
                        context.getPackageName()), R.drawable.photo);
    }

    /**
     * Set its selected photo on the given contact
     * @param componentId String
     * @param photo String
     */
    private void setCustomPhoto(String componentId, String photo) {
        byte[] contactPhoto = Base64.decode(photo, 0);
        Bitmap bitmap = BitmapFactory.decodeByteArray(contactPhoto,
                0, contactPhoto.length);

        remoteViews.setImageViewBitmap(
                context.getResources().getIdentifier(
                        componentId, null, context.getPackageName()), bitmap);
    }

    /**
     * Set click listener for frequent contacts
     * @param componentId String
     * @param number String
     */
    private void setFrequentContactClickListener(String componentId, String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse("tel:" + number));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(context.getResources().getIdentifier(
                componentId, null, context.getPackageName()), pendingIntent);
    }

    /**
     * Set click listener for empty items
     * @param componentId String
     */
    private void setEmptyItemClickListener(String componentId) {
        Intent intentMainActivity = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentMainActivity, 0);
        remoteViews.setOnClickPendingIntent(context.getResources().getIdentifier(
                componentId, null, context.getPackageName()), pendingIntent);
    }
}