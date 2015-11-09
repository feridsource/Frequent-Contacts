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

package com.ferid.app.frequentcontacts.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.ferid.app.frequentcontacts.R;
import com.ferid.app.frequentcontacts.list.Contact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by ferid.cafer on 11/10/2014.
 */
public class PrefsUtil {
    private static volatile PrefsUtil instance = null;
    private static SharedPreferences prefs;
    private static Context context;

    public static PrefsUtil getInstance(Context context__) {
        if (instance == null) {
            synchronized (PrefsUtil.class){
                if (instance == null) {
                    instance = new PrefsUtil();
                    context = context__;
                    prefs = context.getSharedPreferences(context.getString(R.string.sharedPreferences), 0);
                }
            }
        }
        return instance;
    }

    private static String getPathPrefix() {
        String path = Environment.getExternalStorageDirectory() + "/frequent_contacts_widget/";
        // create a File object for the parent directory
        File directory = new File(path);
        // have the object build the directory structure, if needed.
        directory.mkdirs();

        return path;
    }

    public synchronized static void writeContacts(ArrayList<Contact> contactsList) {
        try {
            String tempPath = getPathPrefix() + context.getString(R.string.pref_contacts);
            File file = new File(tempPath);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(contactsList);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static ArrayList<Contact> readContacts() {
        ArrayList<Contact> tempList = new ArrayList<Contact>();
        try {
            String tempPath = getPathPrefix() + context.getString(R.string.pref_contacts);
            File file = new File(tempPath);
            if (file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                tempList = (ArrayList<Contact>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tempList;
    }
}
