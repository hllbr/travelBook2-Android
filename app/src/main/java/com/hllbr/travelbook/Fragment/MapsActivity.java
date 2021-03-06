package com.hllbr.travelbook.Fragment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hllbr.travelbook.Model.Place;
import com.hllbr.travelbook.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager ;
    LocationListener locationListener ;
    SQLiteDatabase database ;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intentToMain = new Intent(MapsActivity.this,MainActivity.class);
        startActivity(intentToMain);
        finish();
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if(info.matches("new")){
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    //  Konum de??i??ti??inde yap??lacak i??lemler

                    //i??leimi bir kez ??al????t??r??p ard??ndan kullan??c??y?? serbest b??rakmak istiyorum bunun i??in sharedpreferansces kullansam yeter

                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.hllbr.travelbook",MODE_PRIVATE);
                    //burada bir integer yada boolean kaydedece??im
                    boolean trackBoolean = sharedPreferences.getBoolean("tackBoolean",false);//e??er daha ??nceden d??nd??r??lm???? ifade yoksa false d??nd??rmesini istiyorum bir ??ey kaydetmeden veri alm???? gibi g??r??n??yor .
                    if(trackBoolean == false){
                        LatLng userLocation = new LatLng(location.getAltitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply();//e??er bu d??ng??deki i??lemi yapt??ysan art??k bir shared var demektir ve bu false ifadeyi true d??n????t??r ve uygula demi?? oldum
                        //bir sonraki ??al????mada bu alan pas ge??ilecek

                    }


                }
            };
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else{
                //izin verildi??inde yap??lacaklar =
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                //devaml?? ??al????t??rmak istemiyorsak veya ilk a????ld????nda bu alan??n ??al????t??r??lamamas?? ihtimaline kar???? son bilinen konumu al cameray?? oraya ??evir gibi bir i??lem yapabilirim fakat ben bu uygulamada standart bir noktadan ba??lamas??n?? istiyorum
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastLocation != null){
                    LatLng lastUserLocation = new LatLng(lastLocation.getAltitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));

                }

            }
        }else{
            //SQLiteDatabase operations && Intent data operations
            mMap.clear();
            Place place =(Place) intent.getSerializableExtra("place");
            LatLng latLng = new LatLng(place.latitude,place.longitude);
            String placeName = place.name;
            mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length >0 ){
            if(requestCode == 1){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                    Intent intent = getIntent();
                    String info = intent.getStringExtra("info");
                    if(info.matches("new")){
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(lastLocation != null){
                            LatLng lastUserLocation = new LatLng(lastLocation.getAltitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));

                        }
                    }else{
                        //SQLiteData operations && Intent data operations like else in onMapReady

                        mMap.clear();
                        Place place =(Place) intent.getSerializableExtra("place");
                        LatLng latLng = new LatLng(place.latitude,place.longitude);
                        String placeName = place.name;
                        mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


                    }


                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        //context get app ve local ise kullan??c??n??n locali olsun olarak ifade ettim

        String address = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);//enlem,boylam,en fazla ka?? adet adres d??nd??relece??i soruluyor buy yap?? ile
            //bu alanda adresslist i??lemlerini uygulamaya devam ediyorum.
            if(addressList != null && addressList.size() >0){
                if (addressList.get(0).getThoroughfare()  != null){
                    address += addressList.get(0).getThoroughfare();
                    if(addressList.get(0).getSubThoroughfare() != null){
                        address+= " ";
                        address += addressList.get(0).getSubThoroughfare();//cadde ad??

                    }
                }

            }else{
                address = "Null Place";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions().title(address).position(latLng));


        //Place objesine vermem gereken t??m verilere ??uana kadar gerekli i??lemleri yaprak elde ettim gerekli de??erleri vererek bir Place objesi olu??turmak ve bu objeleri veri taban??na kaydetmek i??in haz??r??m

        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;
        //Place farkl?? bir paket i??erisinde oldu??u i??in hemen alg??lanamaya bilir.

        //place objesi i??erisine olu??turdu??um address objesini g??ndererek veri ??zerinde do??ru adresin kay??tl?? oldu??undan emin oluyorum

        final Place place = new Place(address,latitude,longitude);
        //alt sat??rda bulunan yazd??rma i??lemlerinde sorun ????kabilir e??er verilerinizi public tan??mlamassan??z
        //public ifadesi ile tan??mlanmad??????nda normal oalrak tan??mland??????nda da ifade default public oalrak ??al??????r fakat sa??l??kl?? bir yaz??l??m ??rne??i olmaz

        System.out.println("place name : "+place.name);
        System.out.println("Place latitude(enlem) : "+place.latitude);
        System.out.println("Place Longitude(boylam) : "+place.longitude);


        //Burada elde etti??mi adres enlem ve boylarlar?? bir otomatik id ile birlikte SQLiteDatabase olu??turup kaydetmek ...

         //??imdi bir dialog ile ki??inin kay??t i??lemi ??zerinde de??i??ikliklerini onay veya red i??lemleri i??in bir yol a??mak istiyorum

        AlertDialog.Builder  alertDialog = new AlertDialog.Builder(MapsActivity.this);

        //onClick i??erisinde oldu??um herhangi bir interface referans vermek gibi bir sorunla kar????la??mayaca????m i??in this olarak context ifadeyi verebilirim.
        //??ptal edilemez bir dialog olu??turmak i??in cancelable methodundan faydalanmak istiyorum

        alertDialog.setCancelable(false);//??ptal edilebilri olsun mu ??eklinde bir soruyu cevaplar gibi d??????n false olarak hay??r iptal edilemesin olarak cevaplad??m
        alertDialog.setTitle("Are you sure you want to save the location change?");
        alertDialog.setMessage("ADDRESS YOU WANT TO SAVE "+place.name);
        alertDialog.setPositiveButton("approve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Bu alanda kay??t i??lemini ger??ekle??tirmeme gerekiyor burada olu??turaca????m database ba??ka alanlardanda eri??im gerektirece??i i??in bu de??i??keni global alanda tan??mlamak istiyorum
                try {
                    database = MapsActivity.this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
                    database.execSQL("CREATE TABLE IF NOT EXISTS places(id INTEGER PRIMARY KEY,name VARCHAR,latitude VARCHAR,longitude VARCHAR)");
                    String toCompile = "INSERT INTO places(name,latitude,longitude) VALUES (?,?,?)";
                    SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
                    //soru i??aretlerini istedi??im de??erlerle ba??layabilmek i??in bir ??st sat??rdaki i??lemi yap??yorum

                    //Double olarak tan??mlad??????m latitude ve longitude burada String olarak d??n????t??rmem gerekiyor.


                    sqLiteStatement.bindString(1,place.name);
                    sqLiteStatement.bindString(2,String.valueOf(place.latitude));
                    sqLiteStatement.bindString(3,String.valueOf(place.longitude));

                    sqLiteStatement.execute();




                }catch (Exception ex){
                    ex.printStackTrace();
                }

                Toast.makeText(getApplicationContext(),"Saved!",Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton("reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"Cancelled",Toast.LENGTH_SHORT).show();
                //bir interface'e referans verdi??im i??in bu alanda this anahtar kelimesi kullanamam bu sebeple getApplicationContext Kullanmama gerkeiyor.
                //First DATABASE operations (save operations)

            }
        });
        alertDialog.show();

    }
}