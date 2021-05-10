package com.hllbr.travelbook.Fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hllbr.travelbook.Adapter.CustomAdapter;
import com.hllbr.travelbook.Model.Place;
import com.hllbr.travelbook.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView ;
    SQLiteDatabase database ;
    ArrayList<Place> placeList = new ArrayList<Place>();
    CustomAdapter customAdapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.ListView);
        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_place,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.add_place_item){
            Intent intent = new Intent(MainActivity.this,MapsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    public void getData(){
        customAdapter = new CustomAdapter(this,placeList);
        try {
            database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            //Oluşturduğum veritabanı üzerinde veriyi çekmek için bir imleç ihtiyacım var mapsActivityden farklı olarak

            Cursor cursor = database.rawQuery("SELECT * FROM places",null);
            //şimdi bu alanda id ve name verileri işimi görüyor.id ile old verilere geçiş sağlayabilirim.name ile verinin hangi değere tanımlı olduğunu kullanıcıya liste üzerinde gösterebilirim

                int nameIx = cursor.getColumnIndex("name");
                int latitudeIx = cursor.getColumnIndex("latitude");
                int longitudeIx = cursor.getColumnIndex("longitude");

                while(cursor.moveToNext()){
                    String nameFromDataBase = cursor.getString(nameIx);
                    String latitudeFromDataBase = cursor.getString(latitudeIx);
                    String longitudeFromDataBase = cursor.getString(longitudeIx);


                    //Place objesine verilerimi göndermeden önce verilerimden konum olanları double ifadeye dönüştürmem gerekiyor.
                    Double latidouble = Double.parseDouble(latitudeFromDataBase);
                    Double longidouble = Double.parseDouble(longitudeFromDataBase);

                    Place place = new Place(nameFromDataBase,latidouble,longidouble);

                    System.out.println("MainActivity name Test = "+place.name);
                    System.out.println("MainActivity latitude Test = "+place.latitude);
                    System.out.println("MainActivity longitude Test = "+place.longitude);
                    placeList.add(place);

                }
                customAdapter.notifyDataSetChanged();
                cursor.close();

        }catch (Exception ex){
            ex.printStackTrace();
        }


        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("place",placeList.get(position));
                startActivity(intent);
            }
        });

    }
}