 package com.example.timetable.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.example.timetable.network.HttpbinService;
import com.example.timetable.R;
import com.example.timetable.bean.ModulusExponent;
import com.example.timetable.course.Course;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CourseDataBase extends SQLiteOpenHelper {
    private static final String TAG = "TAG";
    private String tableName = "tCourse";
    private Context mContext;

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    private SharedPreferences sp;

    public void setSp(SharedPreferences sp) {
        this.sp = sp;
    }

    //存储host域名对应的cookies
    Map<String, List<Cookie>> cookies = new HashMap<>();
    long  datetime;  //请求时间
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://jwglxt1.qust.edu.cn/jwglxt/")
            .callFactory(new OkHttpClient.Builder()
                    .cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                            //存储cookie
                            cookies.put(httpUrl.host(),list);
                        }

                        @NotNull
                        @Override
                        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                            //请求之前加入cookie
                            List<Cookie> cookies = CourseDataBase.this.cookies.get(httpUrl.host());
                            return cookies==null? new ArrayList<Cookie>():cookies;

                        }
                    })
                    .build()).build();
    HttpbinService httpbinService= retrofit.create(HttpbinService.class);


    public CourseDataBase(Context context){
        super(context, "timetable.db", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists tCourse( " +
                "day text," +
                "section text," +
                "classroom text," +
                "listWeeks text," +
                "weekType text," +
                "courseName text" +
                ");";
        db.execSQL(sql);
    }





    public boolean update(String username, String password) throws JSONException, IOException, ScriptException, NoSuchMethodException {



        cookies.clear();
        //获取csrftoken
        datetime = new Date().getTime();
        Call<ResponseBody> call1 = httpbinService.get_csrftoken("https://jwglxt1.qust.edu.cn/jwglxt/xtgl/login_slogin.html?time="+datetime);
        Response<ResponseBody> response1 = call1.execute();
        String result1 = response1.body().string();


        Document document =  Jsoup.parse(result1);
        Element content  = document.getElementById("csrftoken");
        String csrftoken =  content.attr("value");



        //获取modulus exponent
        Call<ResponseBody> call2 = httpbinService.get_csrftoken("https://jwglxt1.qust.edu.cn/jwglxt/xtgl/login_getPublicKey.html?time="+datetime);
        Response<ResponseBody> response2 = call2.execute();
        String result2 = response2.body().string();

        Gson gson=new Gson();
        ModulusExponent modulusExponent = gson.fromJson(result2,ModulusExponent.class);
        String modulus = modulusExponent.getModulus();
        String exponent = modulusExponent.getExponent();



        //加密密码
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");

        //读取raw下的文件
        Reader reader = new InputStreamReader(mContext.getResources().openRawResource(R.raw.rsa));

        engine.eval(reader);

        Invocable jsInvoke = (Invocable) engine;
        Object res = jsInvoke.invokeFunction("getPwd", modulus,exponent,password);
        String pwdByRsa = res.toString();

        //登陆
        Call<ResponseBody> call3 = httpbinService.post_login("https://jwglxt1.qust.edu.cn/jwglxt/xtgl/login_slogin.html?time="+datetime,csrftoken,"zh_CN",username,pwdByRsa,pwdByRsa);
        Response<ResponseBody> response3 = call3.execute();
        if(response3.raw().toString().contains("login_slogin.html")){
            System.out.println("账号或密码错误!");
            return  false;
//            System.exit(0);
        }else {
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("username",username);
            editor.putString("password",password);
            editor.commit();
            System.out.println("登陆成功！");

        }

        Call<ResponseBody> call4 = httpbinService.post_timetable("https://jwglxt1.qust.edu.cn/jwglxt/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=N2151&su="+username,"2023","3","ck");
        Response<ResponseBody> response4 = call4.execute();
        String result4 = response4.body().string();

        JSONObject jsonObject = new JSONObject(result4);
        //姓名班级
        SharedPreferences.Editor editor=sp.edit();
        JSONObject xsxx = jsonObject.getJSONObject("xsxx");
        String name = xsxx.getString("BJMC")+xsxx.getString("XM");
        editor.putString("name",name);
        editor.commit();



        JSONArray array = jsonObject.getJSONArray("kbList");
        SQLiteDatabase database = getWritableDatabase();   //打开数据库
        //删除之前的内容
        database.delete(tableName,null,null);


        //更新数据库
        for (int i = 0;i<array.length();i++){
            String xqjmc = array.getJSONObject(i).getString("xqjmc");
            String jcs = array.getJSONObject(i).getString("jcs");
            String cdmc = array.getJSONObject(i).getString("cdmc");
            String zcd = array.getJSONObject(i).getString("zcd");
            String kcmc = array.getJSONObject(i).getString("kcmc");

            ContentValues values = new ContentValues();
            values.put("day",xqjmc);
            values.put("section",jcs);
            values.put("classroom",cdmc);


            if (zcd.contains("单")){
                values.put("listWeeks",zcd.substring(0,zcd.length()-3));
                values.put("weekType","单");
            }else if(zcd.contains("双")){
                values.put("listWeeks",zcd.substring(0,zcd.length()-3));
                values.put("weekType","双");
            }else{
                values.put("listWeeks",zcd);
                values.put("weekType","非");
            }


            values.put("courseName",kcmc);
            database.insert(tableName,null,values);

        }
        database.close();

        return true;
    }

    public List<Course> listAll(){       //把数据库内容解析到course类中
        List<Course> list = new ArrayList<>();
        SQLiteDatabase database = getWritableDatabase();
        Cursor data = database.query(tableName, null, null, null, null, null, null);
        if(data.getCount() > 0){
            while(data.moveToNext()) {
                Course course = new Course();
                course.setDay(weekConvert(data.getString(0).substring(2,3)));  //day
                course.setS_section(Integer.valueOf(data.getString(1).split("-")[0]));//section
                course.setE_section(Integer.valueOf(data.getString(1).split("-")[1]));

                course.setClassroom(data.getString(2));   //classroom

                String StringWeeks = data.getString(3);     //listWeeks
                List<Map<String,Integer>> listWeek = new ArrayList<>();
                for(final String each :StringWeeks.split(",")){
                    if(each.contains("-")){

                        listWeek.add(new HashMap<String, Integer>(){{
                            put("start",Integer.valueOf(each.substring(0,each.length()-1).split("-")[0]));
                            put("end",Integer.valueOf(each.substring(0,each.length()-1).split("-")[1]) );
                        }});
                    }else{
                        listWeek.add(new HashMap<String, Integer>(){{
                            put("start",Integer.valueOf(each.substring(0,each.length()-1)));
                            put("end",Integer.valueOf(each.substring(0,each.length()-1)) );
                        }});
                    }
                }

                course.setListWeeks(listWeek);

                course.setWeekType(data.getString(4));   //weekType

                course.setCourseName(data.getString(5));   //courseName
                list.add(course);
            }
        }
        database.close();
        return list;

    }
    int weekConvert(String week){
        switch (week){
            case "一":
                return 1;
            case "二":
                return 2;
            case "三":
                return 3;
            case "四":
                return 4;
            case "五":
                return 5;
            case "六":
                return 6;
            case "日":
                return 7;
        }
        return  0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}
