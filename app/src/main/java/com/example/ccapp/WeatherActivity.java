package com.example.ccapp;

import android.Manifest;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ParseException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ccapp.Adapters.RecyclerViewAdapter;
import com.example.ccapp.Database.DatabaseQuery;
import com.example.ccapp.Entities.WeatherObject;
import com.example.ccapp.Helper.CustomSharedPreference;
import com.example.ccapp.Helper.Helper;
import com.example.ccapp.Json.FiveDaysForecast;
import com.example.ccapp.Json.FiveWeathers;
import com.example.ccapp.Json.Forecast;
import com.example.ccapp.Json.LocationMapObject;
import com.github.pavlospt.CircleView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = WeatherActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private TextView cityCountry;
    private TextView currentDate;
    private ImageView weatherImage;
    private CircleView circleTitle;
    private TextView windResult;
    private TextView humidityResult;
    private RequestQueue queue;
    private LocationMapObject locationMapObject;
    private LocationManager locationManager;
    private Location location;
    private final int REQUEST_LOCATION = 200;
    private CustomSharedPreference sharedPreference;
    private String isLocationSaved;
    private DatabaseQuery query;
    private String apiUrl;
    private FiveDaysForecast fiveDaysForecast;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        queue = Volley.newRequestQueue(this);
        query = new DatabaseQuery(WeatherActivity.this);
        sharedPreference = new CustomSharedPreference(WeatherActivity.this);
        isLocationSaved = sharedPreference.getLocationInPreference();

        cityCountry = (TextView)findViewById(R.id.city_country);
        currentDate = (TextView)findViewById(R.id.current_date);
        weatherImage = (ImageView)findViewById(R.id.weather_icon);
        circleTitle = (CircleView)findViewById(R.id.weather_result);
        windResult = (TextView)findViewById(R.id.wind_result);
        humidityResult = (TextView)findViewById(R.id.humidity_result);
        Button popularRestaurants = (Button) findViewById(R.id.popularRestaurants);

        popularRestaurants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherActivity.this, PopularRestaurants.class);
                startActivity(intent);
            }
        });

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WeatherActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            if(isLocationSaved.equals("")){
                // make API call with longitude and latitude//==
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, WeatherActivity.this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        Log.i("TAG", "GPS is on");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Toast.makeText(WeatherActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_LONG).show();
                    }
                    else{
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
                    }
                    //apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&APPID="+Helper.API_KEY+"&units=metric";
                    apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat=24.8570498&lon=67.0405134&APPID=62f6de3f7c0803216a3a13bbe4ea9914&units=metric";

                    makeJsonObject(apiUrl);
                }
            }else{
                // make API call with city name
                String storedCityName = sharedPreference.getLocationInPreference();
                //String storedCityName = "Enugu";
                System.out.println("Stored city " + storedCityName);
                String[] city = storedCityName.split(",");
                if(!TextUtils.isEmpty(city[0])){
                    System.out.println("Stored city " + city[0]);
                    //String url ="http://api.openweathermap.org/data/2.5/weather?q="+city[0]+"&APPID="+ Helper.API_KEY+"&units=metric";
                    String url ="http://api.openweathermap.org/data/2.5/weather?lat=24.8570498&lon=67.0405134&APPID=62f6de3f7c0803216a3a13bbe4ea9914&units=metric";
                    makeJsonObject(url);
                }
            }
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(WeatherActivity.this, 4);

        recyclerView = (RecyclerView)findViewById(R.id.weather_daily_list);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
    }

    private void makeJsonObject(final String apiUrl){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response " + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                locationMapObject = gson.fromJson(response, LocationMapObject.class);
                if (null == locationMapObject) {
                    Toast.makeText(getApplicationContext(), "Nothing was returned", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Response Good", Toast.LENGTH_LONG).show();

                    String city = locationMapObject.getName() + ", " + locationMapObject.getSys().getCountry();
                    String todayDate = getTodayDateInStringFormat();
                    Long tempVal = Math.round(Math.floor(Double.parseDouble(locationMapObject.getMain().getTemp())));
                    String weatherTemp = String.valueOf(tempVal) + "°";
                    String weatherDescription = Helper.capitalizeFirstLetter(locationMapObject.getWeather().get(0).getDescription());
                    String windSpeed = locationMapObject.getWind().getSpeed();
                    String humidityValue = locationMapObject.getMain().getHumudity();

                    //save location in database
                    if(apiUrl.contains("lat")){
                        query.insertNewLocation(locationMapObject.getName());
                    }
                    // populate View data
                    cityCountry.setText(Html.fromHtml(city));
                    currentDate.setText(Html.fromHtml(todayDate));
                    circleTitle.setTitleText(Html.fromHtml(weatherTemp).toString());
                    circleTitle.setSubtitleText(Html.fromHtml(weatherDescription).toString());
                    windResult.setText(Html.fromHtml(windSpeed) + " km/h");
                    humidityResult.setText(Html.fromHtml(humidityValue) + " %");

                    fiveDaysApiJsonObjectCall(locationMapObject.getName());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //make api call
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat=24.8570498&lon=67.0405134&APPID="+Helper.API_KEY+"&units=metric";
                        makeJsonObject(apiUrl);
                    }else{
                        apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat=24.8570498&lon=67.0405134&APPID="+Helper.API_KEY+"&units=metric";
                        makeJsonObject(apiUrl);
                    }
                }
            }else{
                Toast.makeText(WeatherActivity.this, getString(R.string.permission_notice), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private String getTodayDateInStringFormat(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("E, d MMMM", Locale.getDefault());
        return df.format(c.getTime());
    }

    private void fiveDaysApiJsonObjectCall(String city){
        String apiUrl = "http://api.openweathermap.org/data/2.5/forecast?q="+city+ "&APPID="+ Helper.API_KEY+"&units=metric";
        //String apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat=24.8570498&lon=67.0405134&APPID=62f6de3f7c0803216a3a13bbe4ea9914&units=metric";
        final List<WeatherObject> daysOfTheWeek = new ArrayList<WeatherObject>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response 5 days" + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                Forecast forecast = gson.fromJson(response, Forecast.class);
                if (null == forecast) {
                    Toast.makeText(getApplicationContext(), "Nothing was returned", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Response Good", Toast.LENGTH_LONG).show();

                    int[] everyday = new int[]{0,0,0,0,0,0,0};

                    List<FiveWeathers> weatherInfo = forecast.getList();
                    if(null != weatherInfo){
                        for(int i = 0; i < weatherInfo.size(); i++){
                            String time = weatherInfo.get(i).getDt_txt();
                            String shortDay = convertTimeToDay(time);
                            String temp = weatherInfo.get(i).getMain().getTemp();
                            String tempMin = weatherInfo.get(i).getMain().getTemp_min();

                            if(convertTimeToDay(time).equals("Mon") && everyday[0] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[0] = 1;
                            }
                            if(convertTimeToDay(time).equals("Tue") && everyday[1] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[1] = 1;
                            }
                            if(convertTimeToDay(time).equals("Wed") && everyday[2] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[2] = 1;
                            }
                            if(convertTimeToDay(time).equals("Thu") && everyday[3] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[3] = 1;
                            }
                            if(convertTimeToDay(time).equals("Fri") && everyday[4] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[4] = 1;
                            }
                            if(convertTimeToDay(time).equals("Sat") && everyday[5] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[5] = 1;
                            }
                            if(convertTimeToDay(time).equals("Sun") && everyday[6] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[6] = 1;
                            }
                            recyclerViewAdapter = new RecyclerViewAdapter(WeatherActivity.this, daysOfTheWeek);
                            recyclerView.setAdapter(recyclerViewAdapter);
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private String convertTimeToDay(String time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:SSSS", Locale.getDefault());
        String days = "";
        try {
            Date date = format.parse(time);
            System.out.println("Our time " + date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            days = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            System.out.println("Our time " + days);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return days;
    }
}
