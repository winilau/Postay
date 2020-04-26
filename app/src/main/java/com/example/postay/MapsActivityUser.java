package com.example.postay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class MapsActivityUser extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    GoogleApiClient mApiClient;
    private Button mRequestButton, mlogOut, mShopping;
    Location mLocation;
    LocationRequest mRequest;
    private LatLng dropOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_user);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mlogOut = (Button) findViewById(R.id.logout);
        mlogOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MapsActivityUser.this, main.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mRequestButton = (Button) findViewById(R.id.request);
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");

                GeoFire geo = new GeoFire(ref);
                geo.setLocation(userId,new GeoLocation(mLocation.getLatitude(),mLocation.getLongitude()));

                dropOff = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(dropOff).title("Drop Off Here"));

                mRequestButton.setText("Finding a Volunteer...");

                getClosestVolunteer();

            }
        });

        mShopping = (Button) findViewById(R.id.shop);
        mShopping.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                Intent intent = new Intent(MapsActivityUser.this, UserShopping.class);
                startActivity(intent);
                finish();
                return;
            }
        });

    }

    private int radius = 1;
    private Boolean volunteerFound = false;
    private String volunteerFoundID;
    private void getClosestVolunteer(){
        DatabaseReference volunteerLoc = FirebaseDatabase.getInstance().getReference().child("volunteersAvailable");

        GeoFire geo = new GeoFire(volunteerLoc);
        GeoQuery query = geo.queryAtLocation(new GeoLocation(dropOff.latitude, dropOff.longitude),radius);
        query.removeAllListeners();
        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!volunteerFound) {
                    volunteerFound = true;
                    volunteerFoundID = key;
                    mRequestButton.setText("Looking for Volunteer Location...");
                    DatabaseReference volunteerRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child("Volunteer").child(volunteerFoundID);
                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("UserShoppingId", userID);
                    volunteerRef.updateChildren(map);
                    getVolunteerLocation();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!volunteerFound){
                    radius += 1;
                    getClosestVolunteer();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    Marker mVolunteerMarker;
    private void getVolunteerLocation(){
        DatabaseReference volunteerLocRef = FirebaseDatabase.getInstance().getReference()
                .child("volunteerWorking").child(volunteerFoundID).child("l");
        volunteerLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLon = 0;
                    mRequestButton.setText("Volunteer Found!");
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString()) ;
                    }
                    if(map.get(1) != null){
                        locationLon = Double.parseDouble(map.get(1).toString()) ;
                    }
                    LatLng volunteerLatLng = new LatLng(locationLat,locationLon);
                    if(mVolunteerMarker != null){
                        mVolunteerMarker.remove();
                    }
                    mVolunteerMarker = mMap.addMarker(new MarkerOptions()
                            .position(volunteerLatLng).title("Your volunteer!"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mRequest = new LocationRequest();
        mRequest.setInterval(1000); //One second in millisecond
        mRequest.setFastestInterval(1000);
        mRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //Drawback is that it drains battery
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient,mRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
