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

package com.ferid.app.frequentcontacts.list;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.ListPopupWindow;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ferid.app.frequentcontacts.R;
import com.ferid.app.frequentcontacts.interfaces.ItemClickListener;

import java.util.ArrayList;


/**
 * Created by ferid.cafer on 11/12/2014.
 */
public class ContactsAdapter extends ArrayAdapter<Contact> {
    private Context context;
    private int layoutResID;
    private ArrayList<Contact> items;

    private ListPopupWindow listPopupWindow;

    private ItemClickListener itemClickListener;

    public ContactsAdapter(Context context, int layoutResID, ArrayList<Contact> objects) {
        super(context, layoutResID, objects);
        this.items = objects;
        this.context = context;
        this.layoutResID = layoutResID;
        listPopupWindow = new ListPopupWindow(context);
        itemClickListener = (ItemClickListener) context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ContactHolder contactHolder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResID, parent, false);
            contactHolder = new ContactHolder();

            contactHolder.photo = (ImageView) convertView.findViewById(R.id.photo);
            contactHolder.name = (TextView) convertView.findViewById(R.id.name);
            contactHolder.number = (TextView) convertView.findViewById(R.id.number);
            contactHolder.settings = (ImageButton) convertView.findViewById(R.id.settings);

            convertView.setTag(contactHolder);
        } else {
            contactHolder = (ContactHolder) convertView.getTag();
        }

        final Contact contact = items.get(position);

        //set photo
        try {
            byte[] contactPhoto = Base64.decode(contact.getPhoto(), 0);
            setImageViewStraighPortrait(contactHolder.photo, contactPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }

        contactHolder.name.setText(contact.getName());
        contactHolder.number.setText(contact.getNumber());

        contactHolder.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listPopupWindow != null) {
                    setListPopUpWindow(v, position);
                }
            }
        });

        return convertView;
    }

    private static class ContactHolder {
        ImageView photo;
        TextView name;
        TextView number;
        ImageButton settings;
    }

    /**
     * List pop up menu window
     * @param anchor
     */
    private void setListPopUpWindow(View anchor, final int contactPosition) {
        listPopupWindow.dismiss();

        listPopupWindow.setAdapter(new ArrayAdapter(context, android.R.layout.simple_list_item_1,
                context.getResources().getStringArray(R.array.process)));
        listPopupWindow.setAnchorView(anchor);
        listPopupWindow.setContentWidth(400);
        listPopupWindow.setDropDownGravity(Gravity.START);
        listPopupWindow.setModal(true);
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int menuItemPosition, long id) {
                if (itemClickListener != null) {
                    itemClickListener.OnItemClick(contactPosition, menuItemPosition);
                }

                listPopupWindow.dismiss();
            }
        });
        listPopupWindow.show();
    }

    /**
     * Photo without any rounded corners
     * @param view
     * @param data
     */
    private void setImageViewStraighPortrait(ImageView view, byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        // setting the rounded-corner image to imageview.
        if (bitmap != null) {
            view.setImageBitmap(getFormattedBitmap(bitmap));
            //anti-memory leak
            try {
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Exception ignored) {	}
        } else {
            view.setImageResource(R.drawable.ic_person);
        }
    }

    /**
     * Formatted bitmap
     * @param bitmap
     * @return
     */
    private Bitmap getFormattedBitmap(Bitmap bitmap) {
        Bitmap output;
        try {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            return bitmap;
        }

        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 0;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}