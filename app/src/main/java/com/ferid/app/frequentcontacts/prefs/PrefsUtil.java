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

package com.ferid.app.frequentcontacts.prefs;

import android.content.Context;

import com.ferid.app.frequentcontacts.R;
import com.ferid.app.frequentcontacts.list.Contact;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


/**
 * Created by ferid.cafer on 11/10/2014.
 */
public class PrefsUtil {

    /**
     * Write contacts list
     * @param list ArrayList<Contact>
     */
    public synchronized static void writeContacts(Context context, ArrayList<Contact> list) {
        FileOutputStream outputStream = null;

        try {
            outputStream = context.openFileOutput(context.getString(R.string.pref_contacts),
                    Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(list);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Read contacts list
     * @return ArrayList<Contact>
     */
    public synchronized static ArrayList<Contact> readContacts(Context context) {
        ArrayList<Contact> list = null;
        FileInputStream fis = null;

        try {
            fis = context.openFileInput(context.getString(R.string.pref_contacts));
            ObjectInputStream ois = new ObjectInputStream(fis);
            list = (ArrayList<Contact>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return list;
    }

}