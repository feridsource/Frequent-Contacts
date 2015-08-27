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
import android.view.View;
import android.widget.RemoteViews;

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

    public static final String APP_TO_WID = "com.ferid.app.frequentcontacts.widget.APP_TO_WID"; //application triggers the widget

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
                //set photo
                try {
                    if (!contactsList.get(i).getPhoto().equals("")) {
                        byte[] contactPhoto = Base64.decode(contactsList.get(i).getPhoto(), 0);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(contactPhoto, 0, contactPhoto.length);
                        remoteViews.setImageViewBitmap(
                                context.getResources().getIdentifier(
                                        componentId, null, context.getPackageName()), bitmap);
                    } else {
                        remoteViews.setImageViewResource(
                                context.getResources().getIdentifier(
                                        componentId, null, context.getPackageName()), R.drawable.photo);
                    }
                    remoteViews.setViewVisibility(context.getResources().getIdentifier(
                            componentId, null, context.getPackageName()), View.VISIBLE);

                    intent.setData(Uri.parse("tel:" + contactsList.get(i).getNumber()));
                    pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(context.getResources().getIdentifier(
                            componentId, null, context.getPackageName()), pendingIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                remoteViews.setViewVisibility(context.getResources().getIdentifier(
                        componentId, null, context.getPackageName()), View.GONE);
            }
        }

        remoteViews.setBoolean(R.id.layoutBackground, "setEnabled", true);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

    }
}