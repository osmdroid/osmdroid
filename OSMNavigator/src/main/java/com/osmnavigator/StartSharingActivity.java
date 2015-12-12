package com.osmnavigator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class StartSharingActivity extends Activity {

    EditText eNickName, eGroup, eMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_sharing);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        eNickName = (EditText) findViewById(R.id.nickname);
        eNickName.setText(SP.getString("NICKNAME", ""));

        eGroup = (EditText) findViewById(R.id.group);
        eGroup.setText(SP.getString("GROUP", ""));

        eMessage = (EditText) findViewById(R.id.message);

        Button btnOk = (Button) findViewById(R.id.btnOK);
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveAndFinish();
            }
        });
    }

    protected void saveAndFinish() {
        String nickname = eNickName.getText().toString();
        String group = eGroup.getText().toString();
        String message = eMessage.getText().toString();

        //Keep in prefs
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = SP.edit();
        editor.putString("NICKNAME", nickname);
        editor.putString("GROUP", group);
        editor.commit();

        Intent intent = new Intent();
        intent.putExtra("NICKNAME", nickname);
        intent.putExtra("GROUP", group);
        intent.putExtra("MESSAGE", message);
        setResult(RESULT_OK, intent);
        finish();
    }

}
