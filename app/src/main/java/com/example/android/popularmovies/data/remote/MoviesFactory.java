package com.example.android.popularmovies.data.remote;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Factory class that takes care of all HTTP requests through the retrofit library
 */
public class MoviesFactory {

    final String BASE_URL = "http://api.themoviedb.org/3/";
    protected MoviesApi mApi;

    @SuppressWarnings("ConstantConditions")
    public MoviesApi getInstance() {
        if (mApi == null) {

            //For debugging purposes only, use a logging interceptor to see the json result
            //from the http request. HttpLoggingInterceptor.Level.BODY returns the entire message.
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new StethoInterceptor())
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build();

            //prepare call
            mApi = retrofit.create(MoviesApi.class);
            return mApi;
        } else {
            return mApi;
        }
    }

}
