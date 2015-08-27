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

package com.ferid.app.frequentcontacts.custom_dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import com.ferid.app.frequentcontacts.R;
import com.ferid.app.frequentcontacts.interfaces.ClickListener;

/**
 * Created by ferid.cafer on 7/7/2015.<br />
 * Custom dialog that is shown in material design style
 */
public class CustomDialog {

    /**
     * Create and show dialog
     * @param context Context
     */
    public static void showDialog(Context context) {
        //if not lollipop, use custom material dialog
        if (Build.VERSION.SDK_INT < 21) {
            final MaterialDialog materialDialog = new MaterialDialog(context);
            materialDialog.setContent(context.getString(R.string.maxContactsNumber));
            materialDialog.setPositiveButton(context.getString(R.string.ok));
            materialDialog.setOnClickListener(new ClickListener() {
                @Override
                public void OnClick() {
                    materialDialog.dismiss();
                }
            });
            materialDialog.show();
        } else { //otherwise use default alert dialog
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setMessage(context.getString(R.string.maxContactsNumber));
            alertDialog.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        }
    }
}
