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

                    //  Konum değiştiğinde yapılacak işlemler

                    //işleimi bir kez çalıştırıp ardından kullanıcıyı serbest bırakmak istiyorum bunun için sharedpreferansces kullansam yeter

                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.hllbr.travelbook",MODE_PRIVATE);
                    //burada bir integer yada boolean kaydedeceğim
                    boolean trackBoolean = sharedPreferences.getBoolean("tackBoolean",false);//eğer daha önceden döndürülmüş ifade yoksa false döndürmesini istiyorum bir şey kaydetmeden veri almış gibi görünüyor .
                    if(trackBoolean == false){
                        LatLng userLocation = new LatLng(location.getAltitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply();//eğer bu döngüdeki işlemi yaptıysan artık bir shared var demektir ve bu false ifadeyi true dönüştür ve uygula demiş oldum
                        //bir sonraki çalışmada bu alan pas geçilecek

                    }


                }
            };
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else{
                //izin verildiğinde yapılacaklar =
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                //devamlı çalıştırmak istemiyorsak veya ilk açıldğında bu alanın çalıştırılamaması ihtimaline karşı son bilinen konumu al camerayı oraya çevir gibi bir işlem yapabilirim fakat ben bu uygulamada standart bir noktadan başlamasını istiyorum
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
        //context get app ve local ise kullanıcının locali olsun olarak ifade ettim

        String address = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);//enlem,boylam,en fazla kaç adet adres döndüreleceği soruluyor buy yapı ile
            //bu alanda adresslist işlemlerini uygulamaya devam ediyorum.
            if(addressList != null && addressList.size() >0){
                if (addressList.get(0).getThoroughfare()  != null){
                    address += addressList.get(0).getThoroughfare();
                    if(addressList.get(0).getSubThoroughfare() != null){
                        address+= " ";
                        address += addressList.get(0).getSubThoroughfare();//cadde adı

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


        //Place objesine vermem gereken tüm verilere şuana kadar gerekli işlemleri yaprak elde ettim gerekli değerleri vererek bir Place objesi oluşturmak ve bu objeleri veri tabanına kaydetmek için hazırım

        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;
        //Place farklı bir paket içerisinde olduğu için hemen algılanamaya bilir.

        //place objesi içerisine oluşturduğum address objesini göndererek veri üzerinde doğru adresin kayıtlı olduğundan emin oluyorum

        final Place place = new Place(address,latitude,longitude);
        //alt satırda bulunan yazdırma işlemlerinde sorun çıkabilir eğer verilerinizi public tanımlamassanız
        //public ifadesi ile tanımlanmadığında normal oalrak tanımlandığında da ifade default public oalrak çalışır fakat sağlıklı bir yazılım örneği olmaz

        System.out.println("place name : "+place.name);
        System.out.println("Place latitude(enlem) : "+place.latitude);
        System.out.println("Place Longitude(boylam) : "+place.longitude);


        //Burada elde ettiğmi adres enlem ve boylarları bir otomatik id ile birlikte SQLiteDatabase oluşturup kaydetmek ...

         //Şimdi bir dialog ile kişinin kayıt işlemi üzerinde değişikliklerini onay veya red işlemleri için bir yol açmak istiyorum

        AlertDialog.Builder  alertDialog = new AlertDialog.Builder(MapsActivity.this);

        //onClick içerisinde olduğum herhangi bir interface referans vermek gibi bir sorunla karşılaşmayacağım için this olarak context ifadeyi verebilirim.
        //İptal edilemez bir dialog oluşturmak için cancelable methodundan faydalanmak istiyorum

        alertDialog.setCancelable(false);//İptal edilebilri olsun mu şeklinde bir soruyu cevaplar gibi düşün false olarak hayır iptal edilemesin olarak cevapladım
        alertDialog.setTitle("Are you sure you want to save the location change?");
        alertDialog.setMessage("ADDRESS YOU WANT TO SAVE "+place.name);
        alertDialog.setPositiveButton("approve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Bu alanda kayıt işlemini gerçekleştirmeme gerekiyor burada oluşturacağım database başka alanlardanda erişim gerektireceği için bu değişkeni global alanda tanımlamak istiyorum
                try {
                    database = MapsActivity.this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
                    database.execSQL("CREATE TABLE IF NOT EXISTS places(id INTEGER PRIMARY KEY,name VARCHAR,latitude VARCHAR,longitude VARCHAR)");
                    String toCompile = "INSERT INTO places(name,latitude,longitude) VALUES (?,?,?)";
                    SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
                    //soru işaretlerini istediğim değerlerle bağlayabilmek için bir üst satırdaki işlemi yapıyorum

                    //Double olarak tanımladığım latitude ve longitude burada String olarak dönüştürmem gerekiyor.


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
                //bir interface'e referans verdiğim için bu alanda this anahtar kelimesi kullanamam bu sebeple getApplicationContext Kullanmama gerkeiyor.
                //First DATABASE operations (save operations)

            }
        });
        alertDialog.show();

    }
}