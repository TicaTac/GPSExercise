package clm.gpsexercise;

import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LocationListener {
    TextView temp;
    double altitude;
    double longitude;
    LocationManager locationManager;

    ///////////////////////////////// ON CREATE ///////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        temp= (TextView) findViewById(R.id.tempTV);

        String Provider = "gps";

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(Provider);

        if (location != null) {
            altitude = location.getAltitude();
            longitude = location.getLongitude();
            temp.setText("alt= "+altitude+" long= "+longitude);
        }
        else {
            locationManager.requestLocationUpdates("gps", 5000, 1, this);
        }
        FragmentManager manager= getFragmentManager();
        MapFragment mapFragment=(MapFragment) manager.findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                LatLng position = new LatLng(32.0614121,34.8092533);
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                CameraUpdate update= CameraUpdateFactory.newLatLngZoom(position,15);
                googleMap.moveCamera(update);

            }

        });
    }
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ EOF ON CREATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    //////////////////////////////// OVERRIDE LOCATION LISTENER METHODS //////////////

    @Override
    public void onLocationChanged(Location location) {
            altitude=location.getAltitude();
            longitude = location.getLongitude();
            temp.setText("alt= "+altitude+" long= "+longitude);





    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


    //////////////////////////////////// AsyncTask - getFromWeb ///////////////////////////
    public class getFromWeb extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

          return  getResponse(params);
        }

        public String getResponse(String... params) {
            //start download....
            int lineConut = 0;

            BufferedReader input = null;
            HttpURLConnection connection = null;
            StringBuilder response = new StringBuilder();

            try {
                //create a url:
                URL url = new URL(params[0]);
                //create a connection and open it:
                connection = (HttpURLConnection) url.openConnection();

                //status check:
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    //connection not good - return.
                    return null;
                }

                //get a buffer reader to read the data stream as characters(letters)
                //in a buffered way.
                input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                //go over the input, line by line
                String line = "";
                while ((line = input.readLine()) != null) {
                    //append it to a StringBuilder to hold the
                    //resulting string
                    response.append(line + "\n");
                    lineConut++;

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        //must close the reader
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (connection != null) {
                    //must disconnect the connection
                    connection.disconnect();
                }
            }


            return response.toString();

            }


        @Override
        protected void onPostExecute(String resutFromWebsite) {
           parseJSON(resutFromWebsite);
        }

        protected String parseJSON(String response)
        {
            String currentWeather="current weather: ";

            try {

                //the main JSON object - initialize with string
                JSONObject mainObject= new JSONObject(response);

                //extract data with getString, getInt getJsonObject - for inner objects or JSONArray- for inner arrays

                JSONArray myArray= mainObject.getJSONArray("weather");


                for(int i=0; i<myArray.length(); i++)
                {
                    //inner objects inside the array
                    JSONObject innerObj= myArray.getJSONObject(i);
                    String description= innerObj.getString("description");
                    Log.d("json", description);
                    currentWeather=currentWeather+ description;
                }

                JSONObject tempObject=   mainObject.getJSONObject("main");
                double  tmeper=   tempObject.getDouble("temp");

                currentWeather=currentWeather+ " Temp: "+tmeper;
                Log.d("json", ""+tmeper);

            } catch (JSONException e) {
                e.printStackTrace();

            }

            // currentWaetherTV.setText(currentWeather);
            return currentWeather;
        }
    }

}