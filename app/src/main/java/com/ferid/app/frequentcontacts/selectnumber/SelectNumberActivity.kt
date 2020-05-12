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

package com.ferid.app.frequentcontacts.selectnumber

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferid.app.frequentcontacts.R
import com.ferid.app.frequentcontacts.list.Contact
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_select_number.toolbar
import kotlinx.android.synthetic.main.activity_select_number.*
import java.text.Collator
import java.util.*
import kotlin.collections.ArrayList

class SelectNumberActivity : AppCompatActivity() {

    private var progressBar: ProgressBar? = null

    private lateinit var mContext: Context
    private lateinit var mAdapter: NumberAdapter
    private var mContactList: ArrayList<Contact> = ArrayList()

    //whole number list, used for searching any contact
    private val wholeArrayList = java.util.ArrayList<Contact>()

    private val requestPermissions: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_number)

        mContext = this

        setSupportActionBar(toolbar as Toolbar?)

        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar!!.setHomeButtonEnabled(true)
        }

        progressBar = findViewById(R.id.progressBar)

        initRecyclerView()

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) { //if permission is already granted

            getContactsList()
        } else {
            getPermissions()
        }
    }

    /**
     * Initialise recycler view
     */
    private fun initRecyclerView() {
        rvList.layoutManager = LinearLayoutManager(mContext)
        mAdapter = NumberAdapter(mContactList) {
            setItemSelection(it)
        }
        rvList.adapter = mAdapter
    }

    /**
     * Set selected contact. Retrieve photo if exists and close window.
     * @param contact Selected contact
     */
    private fun setItemSelection(contact: Contact) {
        //retrieve and set photo of the selected contact
        contact.photo = retrievePhoto(contact.id)

        val intent = Intent()
        intent.putExtra("contact", contact)
        setResult(Activity.RESULT_OK, intent)
        closeWindow()
    }

    /**
     * Retrieve list of contacts
     */
    private fun getContactsList() {
        NumberListRetriever(mContext, progressBar!!, mAdapter, mContactList, wholeArrayList).execute()
    }

    /**
     * Request and get the permission for reading contacts and phone calling
     */
    private fun getPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CALL_PHONE)) {
            Snackbar.make(progressBar!!, R.string.grantPermission,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.ok) {
                        ActivityCompat.requestPermissions(this@SelectNumberActivity,
                                arrayOf(Manifest.permission.READ_CONTACTS,
                                Manifest.permission.CALL_PHONE),
                                requestPermissions)
                    }.show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CALL_PHONE),
                    requestPermissions)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestPermissions) {
            if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                //permission granted
                getContactsList()
            } else {
                //permission denied
                closeWindow()
            }
        }
    }

    /**
     * Retrieve photo of the given contact with contact ID
     * @param contactId
     * @return
     */
    private fun retrievePhoto(contactId: Int): String? {
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                contactId.toLong())
        val photoUri = Uri.withAppendedPath(contactUri,
                ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
        val cursor: Cursor = contentResolver.query(photoUri,
                arrayOf(ContactsContract.Contacts.Photo.PHOTO),
                null, null, null)
                ?: return ""
        cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                val data = cursor.getBlob(0)
                if (data != null) {
                    //byte[] to String conversion
                    return Base64.encodeToString(data, Base64.DEFAULT)
                }
            }
        }
        return ""
    }

    /**
     * Read data and refresh the list
     */
    class NumberListRetriever(var context: Context, var progressBar: ProgressBar, var adapter: NumberAdapter,
                              var numberList: ArrayList<Contact>, var wholeArrayList: ArrayList<Contact>)
        : AsyncTask<Void?, Void?, ArrayList<Contact>?>() {

        /**
         * Get names and number of contacts
         * @return
         */
        private fun getNumbersList(): ArrayList<Contact> {
            //keep track of added phones in order to handle redundancy
            val addedPhoneNumbers = ArrayList<String?>()

            //contacts to be showed
            val tmpList: ArrayList<Contact> = ArrayList()
            var contact: Contact
            val cr: ContentResolver = context.contentResolver
            val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            cur?.use { cur ->
                if (cur.count > 0) {
                    while (cur.moveToNext()) {
                        //names
                        val id = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts._ID))
                        val name = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        if (
                                cur.getString(cur.getColumnIndex(
                                        ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt() > 0) {
                            //phones
                            val phones = context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.TYPE),
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null)
                            if (phones != null) {
                                while (phones.moveToNext()) {
                                    val phoneNumber = phones.getString(
                                            phones.getColumnIndex(
                                                    ContactsContract.CommonDataKinds.Phone.NUMBER))

                                    //create a new Contact object
                                    contact = Contact()
                                    contact.id = id
                                    contact.name = name
                                    contact.number = phoneNumber.replace(" ", "")
                                    if (!TextUtils.isEmpty(contact.number)
                                            && !addedPhoneNumbers.contains(contact.number)) {
                                        tmpList.add(contact)
                                        addedPhoneNumbers.add(contact.number)
                                    }
                                }
                                phones.close()
                            }
                        }
                    }
                }
            }

            return tmpList
        }

        override fun onPreExecute() {
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<Contact>? {
            //retrieve elements
            val tmpList: ArrayList<Contact> = getNumbersList()

            //sort elements
            Collections.sort(tmpList, ContactsComparator())
            val contact = Contact()
            contact.name = "None"
            tmpList.add(0, contact)

            //send to UI thread
            return tmpList
        }

        override fun onPostExecute(result: ArrayList<Contact>?) {
            if (result != null) {
                numberList.addAll(result)
                wholeArrayList.addAll(result)
                adapter.notifyDataSetChanged()
            }
            progressBar.setVisibility(View.GONE)
        }
    }

    /**
     * Alphabetical order
     */
    class ContactsComparator : Comparator<Contact?> {
        //prepare utf-8
        var collator = Collator.getInstance(Locale("UTF-8"))!!

        override fun compare(c1: Contact?, c2: Contact?): Int {
            return collator.compare(c1!!.name, c2!!.name)
        }
    }

    /**
     * Run time searching
     * @param searchText
     */
    private fun searchEngine(searchText: String) {
        val handler = Handler()
        handler.post {
            val tmpList = java.util.ArrayList<Contact>()

            //first, add names that start with searchText
            for (location in wholeArrayList) {
                if (location.name!!.toLowerCase(Locale.getDefault()).startsWith(
                                searchText.toLowerCase(Locale.getDefault()))) {
                    tmpList.add(location)
                }
            }
            //then, add names that contain searchText
            for (location in wholeArrayList) {
                if (location.name!!.toLowerCase(Locale.getDefault()).contains(
                                searchText.toLowerCase(Locale.getDefault()))
                        && !location.name!!.toLowerCase(Locale.getDefault()).startsWith(
                                searchText.toLowerCase(Locale.getDefault()))) {
                    tmpList.add(location)
                }
            }
            mContactList.clear()
            mContactList.addAll(tmpList)
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun closeWindow() {
        finish()
        overridePendingTransition(R.anim.stand_still, R.anim.move_out_to_bottom)
    }

    override fun onBackPressed() {
        closeWindow()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.ic_action_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                if (s.isNotEmpty()) searchEngine(s) else {
                    mContactList.clear()
                    mContactList.addAll(wholeArrayList)
                    mAdapter.notifyDataSetChanged()
                }
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            closeWindow()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}