package com.example.myweatherapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.myweatherapplication.databinding.ActivityMainBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding dataBinding;
    TextView textView;
    FusedLocationProviderClient fusedLocationProviderClient;
    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    private LocationRequest locationRequest;
    LinearLayoutManager manager=new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
    String[] citynames={"New York", "Singapore","Mumbai","Delhi","Sydney", "Melbourne"};
    static ArrayList<Weather> cityWeather=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        textView = dataBinding.cityTitle;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
        sharedPreferences = getSharedPreferences("lastData", MODE_PRIVATE);
        recyclerView=dataBinding.recyclerView;
        recyclerView.setLayoutManager(manager);
        if (checkConnectivity()) {
            getCurrentWeatherList();
            getCurrentLocation();
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
            getDefaultData();

        }
    }


    private void loadData(String Latitude, String Longitude) {
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
//      RequestQueue requestQueue;
//      requestQueue = Volley.newRequestQueue(this);
//      requestQueue.start();

        String API_KEY = "2cab7d4d158694baeef5060316992318";
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + Latitude + "&lon=" + Longitude + "&appid=" + API_KEY;
        JsonObjectRequest
                jsonObjectRequest
                = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Weather weather=getDetails(response);
                setDetails(weather);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Error", error.getLocalizedMessage());
            }
        });
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private Weather getDetails(JSONObject response) {
        Weather weather = null;
        try {
            String cityName = response.getString("name");
            JSONArray array = response.getJSONArray("weather");
            JSONObject obj = array.getJSONObject(0);
            String imgCode = obj.getString("icon");
            String description = obj.getString("description");
            double temp1 = response.getJSONObject("main").getDouble("temp")-273;
            DecimalFormat precision = new DecimalFormat("0.00");
            String temp=precision.format(temp1)+"°C";
            String icon_url = "https://openweathermap.org/img/wn/" + imgCode + "@4x.png";
            weather = new Weather(cityName, icon_url, temp, description,System.currentTimeMillis());


        } catch (JSONException e) {
            Log.v("Tagg", e.getLocalizedMessage());
        }
        return weather;
    }
    private void setDetails(Weather weather){
        if(weather==null){
            Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
            return;
        }
        textView.setText(weather.getCity());
        dataBinding.weatherDesc.setText(weather.getDesc());
        dataBinding.weatherTemp.setText(weather.getTemp());
        Glide.with(getApplicationContext())
                .load(weather.getImg_code())
                .into(dataBinding.weatherImg);
        dataBinding.weatherImg.setVisibility(View.VISIBLE);
        setCacheData(weather);

    }
    private void getCurrentWeatherList() {
        for (String cityname : citynames) {
            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityname + "&appid=2cab7d4d158694baeef5060316992318";
            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    cityWeather.add(getDetails(response));
                    if(cityWeather.size()==6){
                        setAdaptar();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("Error", error.getLocalizedMessage());
                }
            });
            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }
       // Log.v("response",cityWeather.size()+"");

    }
    void setAdaptar(){
        WeatherAdaptar weatherAdaptar=new WeatherAdaptar(cityWeather,this);
        recyclerView.setAdapter(weatherAdaptar);
    }

    private boolean checkConnectivity() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //we are connected to a network
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
        return connected;
    }

    private void setCacheData(Weather weather) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("city", weather.city);
        editor.putString("icon", weather.getImg_code());
        editor.putString("temp", weather.getTemp());
        editor.putLong("time",weather.getTime());
        editor.putString("desc", weather.getDesc());
        editor.apply();

    }

    private void getDefaultData() {
        String value = sharedPreferences.getString("city", "Please Connect to internet");
        String desc=sharedPreferences.getString("desc","null");
        String temp=sharedPreferences.getString("temp","noTemp");
        String time=TimeUtils.getTime(sharedPreferences.getLong("time",System.currentTimeMillis()));
        textView.setText(value);
        dataBinding.weatherTemp.setText(temp);
        dataBinding.weatherDesc.setText(desc);
        dataBinding.weatherTime.setText(time);
        dataBinding.weatherImg.setImageResource(R.drawable.no_img);
        dataBinding.weatherImg.setVisibility(View.VISIBLE);



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    getCurrentLocation();

                } else {

                    turnOnGPS();
                }
            }
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (isGPSEnabled()) {

                LocationServices.getFusedLocationProviderClient(MainActivity.this)
                        .requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                        .removeLocationUpdates(this);

                                if (locationResult != null && locationResult.getLocations().size() > 0) {

                                    int index = locationResult.getLocations().size() - 1;
                                    double latitude = locationResult.getLocations().get(index).getLatitude();
                                    double longitude = locationResult.getLocations().get(index).getLongitude();
//IMP****
                                    loadData(latitude + "", longitude + "");
                                }
                            }
                        }, Looper.getMainLooper());

            } else {
                turnOnGPS();
            }

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }


    private void turnOnGPS() {


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }
}
//todo bonus task
/*• Below card view show list of following cities with their current weather conditions.


*/