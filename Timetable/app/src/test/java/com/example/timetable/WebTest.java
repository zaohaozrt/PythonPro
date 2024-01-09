package com.example.timetable;


import com.example.timetable.network.HttpbinService;
import com.google.gson.Gson;
import com.example.timetable.bean.ModulusExponent;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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


public class WebTest {
    long  datetime;
    String account = "";
    String password = "";
    //存储host域名对应的cookies
    Map<String, List<Cookie>> cookies = new HashMap<>();


    @Test
    public void get_timetable() throws IOException, ScriptException, NoSuchMethodException, JSONException {
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
                                List<Cookie> cookies = WebTest.this.cookies.get(httpUrl.host());
                                return cookies==null? new ArrayList<Cookie>():cookies;

                            }
                        })
                        .build()).build();
        HttpbinService httpbinService= retrofit.create(HttpbinService.class);
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
        File file = new File("src/main/res/raw/rsa.js");

        FileReader reader = new FileReader(file);

        engine.eval(new FileReader("src/main/res/raw/rsa.js"));
        Invocable jsInvoke = (Invocable) engine;
        Object res = jsInvoke.invokeFunction("getPwd", modulus,exponent,password);
        String pwdByRsa = res.toString();
        reader.close();

        //登陆
        Call<ResponseBody> call3 = httpbinService.post_login("https://jwglxt1.qust.edu.cn/jwglxt/xtgl/login_slogin.html?time="+datetime,csrftoken,"zh_CN",account,pwdByRsa,pwdByRsa);
        Response<ResponseBody> response3 = call3.execute();

        if(response3.raw().toString().contains("login_slogin.html")){
            System.out.println("账号或密码错误!");
            System.exit(0);
        }else {
            System.out.println("登陆成功！");
        }

        Call<ResponseBody> call4 = httpbinService.post_timetable("https://jwglxt1.qust.edu.cn/jwglxt/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=N2151&su="+account,"2021","12","ck");
        Response<ResponseBody> response4 = call4.execute();
        String result4 = response4.body().string();
        System.out.println(result4);
        JSONObject jsonObject = new JSONObject(result4);
        JSONArray array = jsonObject.getJSONArray("kbList");
        for (int i = 0;i<array.length();i++){
            String xqjmc = array.getJSONObject(i).getString("xqjmc");
            String jcor = array.getJSONObject(i).getString("jcor");
            String cdmc = array.getJSONObject(i).getString("cdmc");
            String zcd = array.getJSONObject(i).getString("zcd");
            String kcmc = array.getJSONObject(i).getString("kcmc");
            System.out.println(xqjmc+" "+jcor+" "+cdmc+" "+zcd+" "+kcmc);

        }


    }


}