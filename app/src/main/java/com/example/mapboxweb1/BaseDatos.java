package com.example.mapboxweb1;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class BaseDatos {
    //int x = 0;
    private Resources res;
    //private Style style;
    private List<MarkerOptions> mOptions;
    private MapboxMap mapboxMap;
    private String key;
    List<Coordenada> marcadores;
    private TextView texto1;
    private TextView texto3;
    private TextView texto5;
    private TextView texto7;

    public BaseDatos(Resources res, List<MarkerOptions> mOptions, TextView texto1,TextView texto3,TextView texto5,TextView texto7) {
        this.res = res;
        this.mOptions = mOptions;
        this.key = "Todo";
        this.texto1 = texto1;
        this.texto3 = texto3;
        this.texto5 = texto5;
        this.texto7 = texto7;
    }

    public void setMapboxMap(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void loadMarcadores(DatabaseReference mDatabase, final String[] diass, final String[] categorias){
        marcadores = new ArrayList<>();
        if(!key.equals("Todo")) {
            mDatabase.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String nombre = ds.getKey();
                            String desc = ds.child("descripcion").getValue().toString();
                            String dias = ds.child("semana").getValue().toString();
                            float lat = Float.parseFloat(ds.child("lat").getValue().toString());
                            float lon = Float.parseFloat(ds.child("lon").getValue().toString());
                            Coordenada co = new Coordenada(nombre,desc, lat, lon, dias, key);
                            //if(validarDia(co.getDias(),diass)){
                            marcadores.add(co);
                            Toast.makeText(getApplicationContext(), "Agrego " + nombre, Toast.LENGTH_SHORT).show();
                            //}
                        }
                        addMarker(marcadores);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            for(int x = 0;x<categorias.length;x++) {
                String pass = categorias[x];
                setKey(pass);
                //x++;
                loadMarcadores(mDatabase,diass,categorias);
            }

        }
        //addMarker(marcadores,markerOptions,mapboxMap);

    }
    public void rellenarCard(String nombre){
        Iterator<Coordenada> iterator = marcadores.iterator();
        while(iterator.hasNext()){
            Coordenada coordenada = iterator.next();
            if(coordenada.getNombre().equalsIgnoreCase(nombre)){
                texto1.setText(coordenada.getNombre());
                texto3.setText(coordenada.getCategoria());
                texto5.setText(coordenada.getDesc());
                texto7.setText(coordenada.getDiasSema());
            }
        }
    }
    private boolean validarDia(String[] lista, String[] dias){
        boolean flag = false;
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/Santiago"));
        int dateString = c.get(Calendar.DAY_OF_WEEK);
        String dia = dias[dateString-1];
        Toast.makeText(getApplicationContext(),dia + ", " + c.get(Calendar.HOUR)+ ":" + c.get(Calendar.MINUTE),Toast.LENGTH_SHORT).show();
        for(int x=0;x<lista.length;x++){
            if(lista[x].equalsIgnoreCase(dia)){
                flag = true;
                break;
            }
        }
        return flag;
    }
    private void addMarker(List<Coordenada> coor){

        Iterator<Coordenada> iterator = coor.iterator();
        while(iterator.hasNext()){
            Coordenada coordenada = iterator.next();
            mOptions.add(new MarkerOptions().position(new LatLng(coordenada.getLatitud(),coordenada.getLongitud())).setSnippet(coordenada.getDesc()).setTitle(coordenada.getNombre()));
        }
        mapboxMap.addMarkers(mOptions);
    }

    public boolean removeMarker(){
        mOptions.clear();
        mapboxMap.clear();
        return true;
    }


    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {

        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(res, R.drawable.geo));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }
}
