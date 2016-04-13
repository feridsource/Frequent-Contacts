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

package com.ferid.app.frequentcontacts;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.ferid.app.frequentcontacts.enums.Process;
import com.ferid.app.frequentcontacts.interfaces.ItemClickListener;
import com.ferid.app.frequentcontacts.list.Contact;
import com.ferid.app.frequentcontacts.list.ContactsAdapter;
import com.ferid.app.frequentcontacts.prefs.PrefsUtil;
import com.ferid.app.frequentcontacts.selectnumber.SelectNumberActivity;
import com.ferid.app.frequentcontacts.widget.FrequentContactsWidget;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by ferid.cafer on 11/10/2014.
 */
public class MainActivity extends AppCompatActivity implements ItemClickListener {
    private Context context;

    private GridView list;
    private ArrayList<Contact> contactsList = new ArrayList<>();
    private ContactsAdapter adapter;

    private Contact contact = new Contact();

    private final int SELECT_NUMBER = 0;

    //photo
    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        list = (GridView) findViewById(R.id.list);
        adapter = new ContactsAdapter(context, R.layout.contacts_row, contactsList);
        list.setAdapter(adapter);

        TextView emptyText = (TextView) findViewById(R.id.emptyText);
        list.setEmptyView(emptyText);

        FloatingActionButton actionButtonAdd = (FloatingActionButton) findViewById(R.id.actionButtonAdd);
        actionButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewContact();
            }
        });

        setListItemClickListener();

        new ContactsRetriever().execute();
    }

    /**
     * Set list view item click listener
     */
    private void setListItemClickListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (contactsList != null && contactsList.size() > position) {
                    contact = contactsList.get(position);

                    callContact();
                }
            }
        });
    }

    /**
     * Menu to select operation, on click on any contact item
     * @param contactPosition Position of selected contact
     * @param menuItemPosition Position of menu item
     */
    @Override
    public void OnItemClick(int contactPosition, int menuItemPosition) {
        if (contactsList != null && contactsList.size() > contactPosition) {
            contact = contactsList.get(contactPosition);

            if (menuItemPosition == Process.CHANGE_PHOTO.getValue()) {
                selectPhoto();
            } else if (menuItemPosition == Process.DELETE_CONTACT.getValue()) {
                removeCurrentContact();
            }
        }
    }

    /**
     * Read data and refresh the list
     */
    private class ContactsRetriever extends AsyncTask<Void, Void, ArrayList<Contact>> {

        @Override
        protected ArrayList<Contact> doInBackground(Void... params) {
            return PrefsUtil.getInstance(context).readContacts();
        }

        @Override
        protected void onPostExecute(ArrayList<Contact> result) {
            contactsList.addAll(result);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Save data and refresh the list
     */
    private void save_refresh() {
        PrefsUtil.getInstance(context).writeContacts(contactsList);
        adapter.notifyDataSetChanged();

        updateFrequentContactsWidget();
    }

    /**
     * Select a photo from the gallery
     */
    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    /**
     * Adds a new contact to the application list.<br />
     * Contacts are selected from the phone's contats' list.
     */
    private void addNewContact() {
        if (contactsList != null) {
            if (contactsList.size() < getResources().getInteger(R.integer.maxContactSize)) {
                Intent intent = new Intent(context, SelectNumberActivity.class);
                startActivityForResult(intent, SELECT_NUMBER);
                overridePendingTransition(R.anim.move_in_from_bottom, R.anim.stand_still);
            } else {
                //alert
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getString(R.string.maxContactsNumber));
                builder.setPositiveButton(context.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

    /**
     * Removes a current contact from the application list.<br />
     * The contact is not removed from the phone's contacts' list.
     */
    private void removeCurrentContact() {
        contactsList.remove(contact);
        save_refresh();
    }

    /**
     * Call the selected contact
     */
    private void callContact() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + contact.getNumber()));
        startActivity(callIntent);
    }

    /**
     * Updates frequent contacts widget
     */
    private void updateFrequentContactsWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                this.getClass().getName());
        Intent updateWidget = new Intent(context, FrequentContactsWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        updateWidget.setAction(FrequentContactsWidget.APP_TO_WID);
        updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(updateWidget);
    }

    /**
     * Needed for orientation of selected photo
     * @param context
     * @param photoUri
     * @return
     */
    private static int getOrientation(Context context, Uri photoUri) {
        int orientation = -1;

	    /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() != 1) {
                return orientation;
            }

            cursor.moveToFirst();

            orientation = cursor.getInt(0);
            cursor.close();
        }

        return orientation;
    }

    /**
     * After photo process return
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE) {
            //a photo picked from the gallery
            if (resultCode == RESULT_OK)	{
                try {
                    Uri photoUri = data.getData();

                    InputStream is = this.getContentResolver().openInputStream(photoUri);
                    BitmapFactory.Options dbo = new BitmapFactory.Options();
                    dbo.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(is, null, dbo);
                    if (is != null) is.close();

                    int rotatedWidth, rotatedHeight;
                    int orientation = getOrientation(this, photoUri);

                    if (orientation == 90 || orientation == 270) {
                        rotatedWidth = dbo.outHeight;
                        rotatedHeight = dbo.outWidth;
                    } else {
                        rotatedWidth = dbo.outWidth;
                        rotatedHeight = dbo.outHeight;
                    }

                    Bitmap srcBitmap;
                    is = this.getContentResolver().openInputStream(photoUri);
                    int MAX_IMAGE_HEIGHT = 250;
                    int MAX_IMAGE_WIDTH = 250;
                    if (rotatedWidth > MAX_IMAGE_WIDTH || rotatedHeight > MAX_IMAGE_HEIGHT) {
                        float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_WIDTH);
                        float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_HEIGHT);
                        float maxRatio = Math.max(widthRatio, heightRatio);

                        // Create the bitmap from file
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = (int) maxRatio;
                        srcBitmap = BitmapFactory.decodeStream(is, null, options);
                    } else {
                        srcBitmap = BitmapFactory.decodeStream(is);
                    }
                    if (is != null) is.close();

                    //if the orientation is not 0 (or -1, which means we don't know), we have to do a rotation.
                    if (orientation > 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(orientation);

                        srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                                srcBitmap.getHeight(), matrix, true);
                    }
                    //srcBitmap is ready now

                    //artık encode edip gönderiyoruz
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    // byte[] to String convert.
                    String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    //anti-memory leak
                    srcBitmap.recycle();

                    //ready to set photo
                    contact.setPhoto(encodedImage);
                    save_refresh();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == SELECT_NUMBER) {
            //a contact selected from the list
            if (resultCode == RESULT_OK) {
                try {
                    Contact contact = (Contact) data.getSerializableExtra("contact");
                    if (contact != null) {
                        contactsList.add(contact);
                        save_refresh();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}