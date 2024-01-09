package com.example.timetable.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.example.timetable.R;
import com.example.timetable.database.CourseDataBase;
import com.example.timetable.course.Course;
import com.example.timetable.layout.TimeTableLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.script.ScriptException;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout mDrawerLayout;
    private CourseDataBase courseDataBase= new CourseDataBase(this);
    private TimeTableLayout timeTable;
    private SharedPreferences sp;
    private SwipeRefreshLayout refreshP;
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));

    long date; //开学时间戳
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //不在子线程中请求网络
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);
        refreshP=findViewById(R.id.refresh_layout);
        sp = getSharedPreferences("config", MODE_MULTI_PROCESS );
        timeTable = findViewById(R.id.timeTable);
        navigationView = findViewById(R.id.nav_view);
        timeTable.setNavigationView(navigationView);//供flush刷新使用
        timeTable.setSp(sp);
        courseDataBase.setSp(sp);
        courseDataBase.setmContext(this);




    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            //获取开学时间
            date = sp.getLong("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-02-28 00:00:00").getTime());
            //把数据库内容写入界面
            timeTable.loadData(acquireData(), new Date(date));
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        //item
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_quit:
                        SharedPreferences.Editor editor  =sp.edit();
                        editor.putBoolean("auto_login",false);
                        editor.commit();
                        Intent intent = new Intent(MainActivity.this,Login.class);
                        startActivity(intent);
                        finish();
                        break;
                    case  R.id.nav_start_date:
                        item.setCheckable(false);
                        DatePickerDialog dlg = new DatePickerDialog(MainActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                                        SharedPreferences.Editor editor  =sp.edit();
                                        try {date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(String.format("%d-%d-%d 00:00:00",year,month+1,dayOfMonth)).getTime();
                                            editor.putLong("date",date);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        editor.commit();
                                        try {
                                            timeTable.loadData(acquireData(), new Date(date));

                                        } catch (NoSuchMethodException e) {
                                            e.printStackTrace();
                                        } catch (ScriptException e) {
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },       //监听对象 DatePickerDialog.OnDateSetListener
                                calendar.get(Calendar.YEAR),    //设置默认年
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
                        dlg.show();
                        break;
                    case R.id.nav_time:
                        item.setCheckable(false);
                        String[] time = {"春夏时刻表", "秋冬时刻表"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("选择时刻表");
                        builder.setItems(time, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //which 夏季 0 冬季 1
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putInt("time",which);
                                editor.commit();
                                try {
                                    timeTable.loadData(acquireData(), new Date(date));
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                } catch (ScriptException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        builder.show();
                        break;

                }

                return true;
            }
        });

        //下拉刷新
        refreshP.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    courseDataBase.update(sp.getString("username",null),sp.getString("password",null));
                    date = sp.getLong("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-02-28 00:00:00").getTime());
                    timeTable.loadData(acquireData(), new Date(date));
                    Toast.makeText(MainActivity.this,"课表刷新成功",Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ScriptException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                refreshP.setEnabled(true);
                refreshP.setRefreshing(false);
            }
        });

        //菜单栏
        timeTable.addListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

    private List<Course> acquireData() throws NoSuchMethodException, ScriptException, JSONException, IOException {
        List<Course> courses = new ArrayList<>();
        sp = getSharedPreferences("config", MODE_PRIVATE);
        if (sp.getBoolean("isFirstUse", true)) {//首次使用 先更新数据库
            String username = sp.getString("username",null);
            String password = sp.getString("password",null);
            courseDataBase.update(username,password);    //更新数据库内容
            courses = courseDataBase.listAll();
            sp.edit().putBoolean("isFirstUse", false).apply();
        }else {

            courses = courseDataBase.listAll();   //获取数据库内容（要解析过的）

        }
        return courses;
    }

    @Override
    //在打开界面按下返回键
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}
