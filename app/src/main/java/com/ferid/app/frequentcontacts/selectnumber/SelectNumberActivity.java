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

package com.ferid.app.frequentcontacts.selectnumber;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ferid.app.frequentcontacts.R;
import com.ferid.app.frequentcontacts.list.Contact;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by ferid.cafer on 11/12/2014.
 */
public class SelectNumberActivity extends AppCompatActivity {
    private Context context;

    private ListView list;
    private ArrayList<Contact> numberList = new ArrayList<Contact>();
    private NumberAdapter adapter;

    private ProgressWheel progressWheel;

    //searching elements
    private SearchView searchView;
    private ArrayList<Contact> wholeArrayList = new ArrayList<Contact>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_number);

        context = this;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        list = (ListView) findViewById(R.id.list);
        adapter = new NumberAdapter(context, R.layout.select_number_row, numberList);

        //list item animations
        AlphaInAnimationAdapter animatingAdapter
                = new AlphaInAnimationAdapter(adapter);
        animatingAdapter.setAbsListView(list);

        assert animatingAdapter.getViewAnimator() != null;
        animatingAdapter.getViewAnimator().setInitialDelayMillis(300);

        list.setAdapter(animatingAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Contact contact = numberList.get(position);
                Intent intent = new Intent();
                intent.putExtra("contact", contact);
                setResult(RESULT_OK, intent);
                closeWindow();
            }
        });

        progressWheel = (ProgressWheel) findViewById(R.id.progressWheel);

        new NumberListRetriever().execute();
    }

    /**
     * Get names and number of contacts
     * @return
     */
    private ArrayList<Contact> getNumbersList() {
        ArrayList<Contact> tmpList = new ArrayList<Contact>();
        Contact contact;

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        try {
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    //names
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    final String name = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (Integer.parseInt(
                            cur.getString(cur.getColumnIndex(
                                    ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        //phones
                        Cursor phones = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        ContactsContract.CommonDataKinds.Phone.TYPE},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                null, null);

                        while (phones.moveToNext()) {
                            String phoneNumber = phones.getString(
                                    phones.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER));

                            contact = new Contact();
                            contact.setName(name);
                            contact.setNumber(phoneNumber);

                            if (!tmpList.contains(contact))
                                tmpList.add(contact);
                        }
                        phones.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null)
                cur.close();
        }

        return tmpList;
    }


    /**
     * Read data and refresh the list
     */
    private class NumberListRetriever extends AsyncTask<Void, Void, ArrayList<Contact>> {

        @Override
        protected void onPreExecute() {
            progressWheel.spin();
        }

        @Override
        protected ArrayList<Contact> doInBackground(Void... params) {
            //retrieve elements
            ArrayList<Contact> tmpList = getNumbersList();

            //sort elements
            if (tmpList != null)
                Collections.sort(tmpList, new ContactsComparator());

            //send to UI thread
            return tmpList;
        }

        @Override
        protected void onPostExecute(ArrayList<Contact> result) {
            if (result != null) {
                numberList.addAll(result);
                wholeArrayList.addAll(result);
                adapter.notifyDataSetChanged();
            }

            progressWheel.stopSpinning();
        }
    }

    /**
     * Alphabetical order
     */
    public class ContactsComparator implements Comparator<Contact> {
        //prepare utf-8
        Collator collator = Collator.getInstance(new Locale("UTF-8"));

        @Override
        public int compare(Contact c1, Contact c2) {
            return collator.compare(c1.getName(), c2.getName());
        }
    }

    /**
     * Run time searching
     * @param searchText
     */
    private void searchEngine(final String searchText) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<Contact> tmpList = new ArrayList<Contact>();

                for (Contact location : wholeArrayList) {
                    if (location.getName().toLowerCase().startsWith(searchText.toLowerCase())) {
                        tmpList.add(location);
                    }
                }
                for (Contact location : wholeArrayList) {
                    if (location.getName().toLowerCase().contains(searchText.toLowerCase())
                            && !location.getName().toLowerCase().startsWith(searchText.toLowerCase())) {
                        tmpList.add(location);
                    }
                }

                numberList.clear();
                numberList.addAll(tmpList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void closeWindow() {
        finish();
        overridePendingTransition(R.anim.stand_still, R.anim.move_out_to_bottom);
    }

    @Override
    public void onBackPressed() {
        closeWindow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.ic_action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() >= 1)
                    searchEngine(s);
                else {
                    numberList.clear();
                    numberList.addAll(wholeArrayList);
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                closeWindow();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
