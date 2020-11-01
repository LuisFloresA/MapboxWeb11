package com.example.mapboxweb1;

import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

// classes needed to initialize map
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

// classes needed to add the location component
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;

// classes needed to add a marker
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

// classes to calculate a route
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

// classes needed to launch navigation UI
import android.view.View;
import android.widget.Button;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener, PopupMenu.OnMenuItemClickListener {
    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;
    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    private CardView card_view;
    private  ImageButton closeCard;
    private  ImageButton car;
    private LatLng puntoMarcador;
    private TextView texto1;
    private TextView texto3;
    private TextView texto5;
    private TextView texto7;
    // variables needed to initialize navigation



    List<MarkerOptions> markerOptions = new ArrayList<>();
    DatabaseReference mDatabase;
    String[] dias={"domingo","lunes","martes", "miércoles","jueves","viernes","sábado"};
    String[] categorias = {"Dentista","Farmacia","Policia","Salud","Veterinario"};
    BaseDatos db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        closeCard = findViewById(R.id.closeCard);
        closeCard.setOnClickListener(this);
        car = findViewById(R.id.car);
        car.setOnClickListener(this);
        texto1 = findViewById(R.id.texto1);
        texto3 = findViewById(R.id.texto3);
        texto5 = findViewById(R.id.texto5);
        texto7= findViewById(R.id.texto7);
        card_view = findViewById(R.id.card_view);
        card_view.setVisibility(View.GONE);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        db = new BaseDatos(getResources(),markerOptions,texto1,texto3,texto5,texto7);

    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
                //db.setStyle(style);
                db.setMapboxMap(mapboxMap);
                //Metodo al pinchar genera el icono ruta
                //addDestinationIconSymbolLayer(style);
                //mapboxMap.addOnMapClickListener(MainActivity.this);
                db.loadMarcadores(mDatabase, dias,categorias);
                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        String name = marker.getTitle();
                        puntoMarcador = marker.getPosition();
                        db.rellenarCard(name);
                        card_view.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(),name,Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

            }
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        Toast.makeText(getApplicationContext(),"CLICK ;)",Toast.LENGTH_SHORT).show();
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());
        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }
        getRoute(originPoint, destinationPoint);
        return true;
    }




    public void showPopup(View v){
        PopupMenu popup = new PopupMenu(this,v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_content);
        popup.show();
    }
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.Todo:
                try {
                    navigationMapRoute.removeRoute();
                }catch (Exception e){

                }
                db.removeMarker();
                db.setKey("Todo");
                db.loadMarcadores(mDatabase, dias,categorias);
                Toast.makeText(getApplicationContext(),"Click Todo",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Dentista:
                try {
                    navigationMapRoute.removeRoute();
                }catch (Exception e){

                }
                db.removeMarker();
                db.setKey("Dentista");
                db.loadMarcadores(mDatabase, dias,categorias);
                Toast.makeText(getApplicationContext(),"Click Dentista",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Farmacia:
                try {
                    navigationMapRoute.removeRoute();
                }catch (Exception e){

                }
                db.removeMarker();
                db.setKey("Farmacia");
                db.loadMarcadores(mDatabase, dias,categorias);
                Toast.makeText(getApplicationContext(),"Click Farmacia",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Policia:
                try {
                    navigationMapRoute.removeRoute();
                }catch (Exception e){

                }
                db.removeMarker();
                db.setKey("Policia");
                db.loadMarcadores(mDatabase, dias,categorias);
                Toast.makeText(getApplicationContext(),"Click Policia",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Salud:
                try {
                    navigationMapRoute.removeRoute();
                }catch (Exception e){

                }
                db.removeMarker();
                db.setKey("Salud");
                db.loadMarcadores(mDatabase, dias,categorias);
                Toast.makeText(getApplicationContext(),"Click Salud",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Veterinario:
                try {
                    navigationMapRoute.removeRoute();
                }catch (Exception e){

                }
                db.removeMarker();
                db.setKey("Veterinario");
                db.loadMarcadores(mDatabase, dias,categorias);
                Toast.makeText(getApplicationContext(),"Click Veterinario",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.closeCard:
                card_view.setVisibility(View.GONE);
                Toast.makeText(this, "Cerrado cardView ;)", Toast.LENGTH_SHORT).show();
                break;
            case R.id.car:
                onMapClick(puntoMarcador);
                Toast.makeText(this, "Ruta generada ;)", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.play:
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                dialogo1.setTitle("Ejecutar ruta");
                dialogo1.setMessage("Desea viajar a este punto?");
                dialogo1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean simulateRoute = true;
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(simulateRoute)
                                .build();
                        // Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(MainActivity.this, options);
                        //finish();
                    }
                });
                dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dialogo1.show();
                return true;
            case R.id.exit:
                AlertDialog.Builder dialogo2 = new AlertDialog.Builder(this);
                dialogo2.setTitle("Quitar ruta");
                dialogo2.setMessage("Estas seguro que deseas quitar la ruta?");
                dialogo2.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        navigationMapRoute.removeRoute();
                        Toast.makeText(getApplicationContext(),"Quitado ;)",Toast.LENGTH_SHORT).show();
                        //finish();
                    }
                });
                dialogo2.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dialogo2.show();
                return true;
            case R.id.filter:
                View menuView = findViewById(R.id.filter); // SAME ID AS MENU ID
                showPopup(menuView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings( {"MissingPermission"})



    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }
                        currentRoute = response.body().routes().get(0);
                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }
                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }


    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity, menu);
        return true;
    }


}