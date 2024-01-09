package com.example.timetable.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface HttpbinService {
    @GET
    @Headers({"User-Agent:Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36",
            "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
            "Accept-Encoding:gzip, deflate",
            "Accept-Language:zh-CN,zh;q=0.9",
            "Connection:keep-alive"})
    Call<ResponseBody> get_csrftoken(@Url String url);

    @GET
    @Headers({"User-Agent:Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36",
            "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
            "Accept-Encoding:gzip, deflate",
            "Accept-Language:zh-CN,zh;q=0.9",
            "Connection:keep-alive"})
    Call<ResponseBody> get_publicKey(@Url String url);

    @POST
    @FormUrlEncoded
    @Headers({"User-Agent:Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36",
            "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
            "Accept-Encoding:gzip, deflate",
            "Accept-Language:zh-CN,zh;q=0.9",
            "Connection:keep-alive"})
    Call<ResponseBody> post_login(@Url String url, @Field("csrftoken") String csrftoken, @Field("language") String language, @Field("yhm") String account, @Field("mm") String mm1, @Field("mm") String mm2);


    @POST
    @FormUrlEncoded
    @Headers({"User-Agent:Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36",
            "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
            "Accept-Encoding:gzip, deflate",
            "Accept-Language:zh-CN,zh;q=0.9",
            "Connection:keep-alive"})
    Call<ResponseBody> post_timetable(@Url String url, @Field("xnm") String xnm, @Field("xqm") String xqm, @Field("kzlx") String kzlx);

}
