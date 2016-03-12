package by.schools.komlik.maxim.socketioclient;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Login extends AppCompatActivity {
    DBHelper DBH;

    EditText nameEditText;
    CheckBox rememberMe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nameEditText = (EditText) findViewById(R.id.NameEditText);
        rememberMe = (CheckBox) findViewById(R.id.remember);

        DBH = new DBHelper(this);
        tryAutologin();
    }


    public void onClickLogin(View v){
        SQLiteDatabase DB = DBH.getWritableDatabase();

        ContentValues CV = new ContentValues();

        Cursor c = DB.query("Login", null, null, null, null, null, null);
        Cursor c1 = DB.query("bool", null, null, null, null, null, null);

        int b = 0;
        if (rememberMe.isChecked()){
            b = 1;
        }else {
            b = -1;
        }
        if (c1.moveToFirst()) {

            DB.delete("bool", null, null);


            CV.put("rem", b);

        }
        else{
            CV.put("rem", b);
        }

        DB.insert("bool", null, CV);

        CV.clear();

        if (c.moveToFirst()) {

            DB.delete("Login", null, null);


            CV.put("name", nameEditText.getText().toString());

        }
        else{
            CV.put("name", nameEditText.getText().toString());
        }

        DB.insert("Login", null, CV);

        c.close();


        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public void tryAutologin (){
        SQLiteDatabase DB = DBH.getWritableDatabase();
        Cursor c1 = DB.query("bool", null, null, null, null, null, null);
        if (c1.moveToFirst()){
            if (c1.getInt(c1.getColumnIndex("rem")) == 1){
                Cursor c = DB.query("Login", null, null, null, null, null, null);
                if (c.moveToFirst()){
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                }
            }
        }
    }
}
