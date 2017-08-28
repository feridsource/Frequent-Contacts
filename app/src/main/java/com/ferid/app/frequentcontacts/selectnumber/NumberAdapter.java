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

package com.ferid.app.frequentcontacts.selectnumber;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ferid.app.frequentcontacts.R;
import com.ferid.app.frequentcontacts.list.Contact;

import java.util.ArrayList;


/**
 * Created by ferid.cafer on 11/12/2014.
 */
public class NumberAdapter extends ArrayAdapter<Contact> {
    private Context context;
    private int layoutResID;
    private ArrayList<Contact> items;

    public NumberAdapter(Context context, int layoutResID, ArrayList<Contact> objects) {
        super(context, layoutResID, objects);
        this.items = objects;
        this.context = context;
        this.layoutResID = layoutResID;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ContactHolder contactHolder;

        if (convertView == null) {
            // return your progress view goes here. Ensure that it has the ID
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResID, parent, false);
            contactHolder = new ContactHolder();

            contactHolder.name = (TextView) convertView.findViewById(R.id.name);
            contactHolder.number = (TextView) convertView.findViewById(R.id.number);

            convertView.setTag(contactHolder);
        } else {
            contactHolder = (ContactHolder) convertView.getTag();
        }

        final Contact contact = items.get(position);

        contactHolder.name.setText(contact.getName());
        contactHolder.number.setText(contact.getNumber());

        return convertView;
    }

    private static class ContactHolder {
        TextView name;
        TextView number;
    }
}