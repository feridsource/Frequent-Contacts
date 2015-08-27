package com.ferid.app.frequentcontacts.custom_dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ferid.app.frequentcontacts.R;
import com.ferid.app.frequentcontacts.interfaces.ClickListener;

/**
 * Created by ferid.cafer on 3/19/2015.
 */
public class MaterialDialog extends Dialog {

    private TextView content;
    private Button positiveButton;

    private ClickListener clickListener;

    public MaterialDialog(Context context) {
        super(context);
        setContentView(R.layout.material_dialog);

        getWindow().setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.transparent)));

        content = (TextView) findViewById(R.id.content);
        positiveButton = (Button) findViewById(R.id.positiveButton);
    }

    public void setContent(String value) {
        content.setText(value);
    }

    public void setPositiveButton(String value) {
        positiveButton.setText(value);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null)
                    clickListener.OnClick();
            }
        });
    }

    public void setOnClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
