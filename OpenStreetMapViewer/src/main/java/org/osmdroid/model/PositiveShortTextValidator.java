package org.osmdroid.model;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * created on 12/4/2016.
 *
 * @author Alex O'Ree
 * @since 5.6.1
 */

public class PositiveShortTextValidator implements TextWatcher {
    EditText parent;

    public PositiveShortTextValidator(EditText parent) {
        this.parent = parent;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {


        String txt = parent.getText().toString();
        if (txt == null || txt.length() == 0)
            parent.setError("Not a valid number");
        try {
            short val = Short.parseShort(txt);
            if (val < 1) {
                parent.setError("Must be at least 1");
            } else {
                parent.setError(null);
            }
        } catch (Exception ex) {
            parent.setError("Not a valid number");
        }

    }
}
