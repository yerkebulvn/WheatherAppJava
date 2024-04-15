package com.example.weatherapptutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {


    final String APP_ID = "dab3af44de7d24ae7ff86549334e45bd";   //API жұмыс жасауға арналған айнымалы  (кілт)
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";   //Ауа райы туралы мәліметтерді алатын сайт

    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 101;


    String Location_Provider = LocationManager.GPS_PROVIDER;    //GPS орналасу орынын алу провайдері

    TextView NameofCity, weatherState, Temperature; //Экранға текст шығаратын компоненттер
    ImageView mweatherIcon; //Ауа райының күйін көрсететін сурет

    RelativeLayout mCityFinder; //Қаланы іздеу экран бөлшегі


    LocationManager mLocationManager;   //Орналасу орнының менеджері
    LocationListener mLocationListner;  //Орналасу орынын тыңдаушы


    @Override
    protected void onCreate(Bundle savedInstanceState) {    //Активити құрылғанда орындалатын функция
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherState = findViewById(R.id.weatherCondition); //Экрандағы элементтердің инициализациясы
        Temperature = findViewById(R.id.temperature);
        mweatherIcon = findViewById(R.id.weatherIcon);
        mCityFinder = findViewById(R.id.cityFinder);
        NameofCity = findViewById(R.id.cityName);


        mCityFinder.setOnClickListener(new View.OnClickListener() { //Қаланы іздеу батырмасының тыңдаушысы
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, cityFinder.class);    //Батырма басылғанда жаңа активити экранға шығарылады
                startActivity(intent);
            }
        });

    }

 /*   @Override
   protected void onResume() {
       super.onResume();
       getWeatherForCurrentLocation();
    }*/

    @Override
    protected void onResume() { //Активити қайта ашқанда орындалатын функция. Мысалға қаланы таңдап болғаннан кейін оның аты осы активитиге беріледі
        super.onResume();
        Intent mIntent=getIntent();
        String city= mIntent.getStringExtra("City");
        if(city!=null)
        {
            getWeatherForNewCity(city);
        }
        else
        {
            getWeatherForCurrentLocation();
        }


    }


    private void getWeatherForNewCity(String city)  //Жаңадан таңдалған қаланың ауа райын көрсету
    {
        RequestParams params=new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        letsdoSomeNetworking(params);

    }




    private void getWeatherForCurrentLocation() {   //GPS арқылы ауа райын көрсету

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {  //Орналасқан орын өзгерген кезде іске қосылатын функция

                String Latitude = String.valueOf(location.getLatitude());
                String Longitude = String.valueOf(location.getLongitude());

                RequestParams params =new RequestParams();
                params.put("lat" ,Latitude);
                params.put("lon",Longitude);
                params.put("lang","ru");
                params.put("appid",APP_ID);
                letsdoSomeNetworking(params);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                //not able to get location
            }
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // мұнда жетіспейтін рұқсаттарды сұрау, содан кейін қайта анықтау
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // пайдаланушы рұқсат берген жағдайды шешу үшін.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListner);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if(requestCode==REQUEST_CODE)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.location_success),Toast.LENGTH_SHORT).show();
                getWeatherForCurrentLocation();
            }
            else
            {
                //пайдаланушы рұқсаттан беруден бас тартқан кезде
                Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.location_access_denied),Toast.LENGTH_SHORT).show();
            }
        }


    }



    private  void letsdoSomeNetworking(RequestParams params)    //Сайтқа запрос жіберу
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL,params,new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {  //Запрос сәтті орындалса

                Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.data_success),Toast.LENGTH_SHORT).show();

                weatherData weatherD=weatherData.fromJson(response);
                updateUI(weatherD);


               // super.onSuccess(statusCode, headers, response);
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });



    }

    private  void updateUI(weatherData weather){    //Экрандағы мәліметтерді жаңарту функциясы


        Temperature.setText(weather.getmTemperature());
        NameofCity.setText(weather.getMcity());
        weatherState.setText(weather.getmWeatherType());
        int resourceID=getResources().getIdentifier(weather.getMicon(),"drawable",getPackageName());
        mweatherIcon.setImageResource(resourceID);


    }

    @Override
    protected void onPause() {  //Активити пауза күйіне түскенде орындалады
        super.onPause();
        if(mLocationManager!=null)
        {
            mLocationManager.removeUpdates(mLocationListner);
        }
    }
}