package com.ferid.app.frequentcontacts.prefs;

import android.content.Context;
import android.content.SharedPreferences;

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

    public synchronized static void writeContacts(ArrayList<Contact> contactsList) {
        try {
            String tempPath = context.getFilesDir() + context.getString(R.string.contacts);
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
            String tempPath = context.getFilesDir() + context.getString(R.string.contacts);
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
