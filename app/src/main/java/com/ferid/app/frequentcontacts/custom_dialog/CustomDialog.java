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
