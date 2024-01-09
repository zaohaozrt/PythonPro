package com.example.timetable.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.timetable.database.CourseDataBase;
import com.example.timetable.R;

import org.json.JSONException;

import java.io.IOException;

import javax.script.ScriptException;

public class Login extends AppCompatActivity {
    private EditText accountEdit;
    private EditText passwordEdit;
    private Button login;
    protected CourseDataBase courseDataBase = new CourseDataBase(this);
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //不在子线程中请求网络
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        accountEdit = findViewById(R.id.editText);
        passwordEdit = findViewById(R.id.editText2);
        login = findViewById(R.id.login);
        sp  = getSharedPreferences("config", MODE_MULTI_PROCESS );
        boolean isRemember = sp.getBoolean("auto_login",false);
        courseDataBase.setSp(sp);
        courseDataBase.setmContext(this);
        if (isRemember){
            //自动转跳
            Intent intent = new Intent(Login.this,MainActivity.class);
            startActivity(intent);
            finish();

        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                //如果登陆成功
                try {
                    if(courseDataBase.update(username,password)){
                        //记住账号密码功能
                        editor = sp.edit();

                        editor.putBoolean("auto_login",true);
                        editor.putString("username",username);
                        editor.putString("password",password);
                        editor.commit();
                        //跳转
                        Toast.makeText(Login.this,"登陆成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this,MainActivity.class);
                        startActivity(intent);



                        finish();
                    }else{
                        Toast.makeText(Login.this,"账号或密码错误!",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ScriptException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}