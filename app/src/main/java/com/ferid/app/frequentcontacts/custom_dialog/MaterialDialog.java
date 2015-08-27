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
