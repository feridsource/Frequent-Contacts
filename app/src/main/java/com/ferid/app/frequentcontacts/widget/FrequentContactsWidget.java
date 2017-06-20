/*
 * Copyright (C) 2016 Ferid Cafer
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
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

    private RemoteViews remoteViews;
    private AppWidgetManager appWidgetManager;
    private ComponentName thisWidget;

    //application triggers the widget
    public static final String APP_TO_WID = "com.ferid.app.frequentcontacts.widget.APP_TO_WID";
    public static final String WIDGET_ENABLED = "android.appwidget.action.APPWIDGET_ENABLED";

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
        this.appWidgetManager = AppWidgetManager.getInstance(context);
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.frequent_contacts_widget);
        thisWidget = new ComponentName(context, FrequentContactsWidget.class);

        if (intent.getAction().equals(APP_TO_WID)
                || intent.getAction().equals(WIDGET_ENABLED)) {
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
        ArrayList<Contact> contactsList = PrefsUtil.readContacts(context);

        if (contactsList == null || contactsList.size() == 0) {
            remoteViews.setBoolean(R.id.layoutBackground, "setEnabled", false);

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            for (int i = 0; i < context.getResources().getInteger(R.integer.maxContactSize); i++) {
                final String componentId = "id/contact" + (i + 1);

                if (contactsList.size() > i) {
                    Contact contact = contactsList.get(i);
                    //if contact is set
                    try {
                        if (!contact.getPhoto().equals("")) {
                            //set photo if exists
                            setCustomPhoto(componentId, contact.getPhoto());
                        } else {
                            //put default photo if there is no any
                            setDefaultPhoto(componentId);
                        }

                        //show item
                        setVisible(componentId);

                        //call contact on click
                        setFrequentContactClickListener(componentId, contact.getNumber());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //make it invisible (gone)
                    setInvisible(componentId);
                }
            }

            remoteViews.setBoolean(R.id.layoutBackground, "setEnabled", true);
        }


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
                        context.getPackageName()), R.drawable.ic_person);
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
                        componentId, null, context.getPackageName()), getCroppedBitmap(bitmap, 200));
    }

    /**
     * Make item visible
     * @param componentId
     */
    private void setVisible(String componentId) {
        remoteViews.setViewVisibility(
                context.getResources().getIdentifier(
                        componentId, null,
                        context.getPackageName()), View.VISIBLE);
    }

    /**
     * Make item invisible
     * @param componentId
     */
    private void setInvisible(String componentId) {
        remoteViews.setViewVisibility(
                context.getResources().getIdentifier(
                        componentId, null,
                        context.getPackageName()), View.GONE);
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
     * Get rounded version of the given photo
     * @param bmp
     * @param radius
     * @return
     */
    private Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;

        if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
            float smallest = Math.min(bmp.getWidth(), bmp.getHeight());
            float factor = smallest / radius;
            sbmp = Bitmap.createScaledBitmap(bmp,
                    (int) (bmp.getWidth() / factor),
                    (int) (bmp.getHeight() / factor), false);
        } else {
            sbmp = bmp;
        }

        Bitmap output = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final String color = "#BAB399";
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, radius, radius);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor(color));
        canvas.drawCircle(radius / 2 + 0.7f, radius / 2 + 0.7f,
                radius / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }
}