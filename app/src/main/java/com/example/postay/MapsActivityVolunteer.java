package com.example.postay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;


public class MapsActivityVolunteer extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    GoogleApiClient mApiClient;
    Location mLocation;
    LocationRequest mRequest;
    private Button mlogOut;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_volunteer);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mlogOut = (Button) findViewById(R.id.logout);
        mlogOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MapsActivityVolunteer.this, main.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        getAssignedUser();
    }

    private void getAssignedUser(){
        String volunteerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedUserRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child("Volunteer").child(volunteerID).child("UserShoppingId");
        assignedUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                        mUserId = dataSnapshot.getValue().toString();
                        getAssignedUserDropOffLocation();
                        getAssignedUserShoppingList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getAssignedUserDropOffLocation(){
        DatabaseReference assignedUserDropOffLocation = FirebaseDatabase.getInstance().getReference()
                .child("customerRequest").child(mUserId).child("l");
        assignedUserDropOffLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLon = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString()) ;
                    }
                    if(map.get(1) != null){
                        locationLon = Double.parseDouble(map.get(1).toString()) ;
                    }
                    LatLng userLatLng = new LatLng(locationLat,locationLon);
                    mMap.addMarker(new MarkerOptions()
                            .position(userLatLng).title("Drop Off Here"));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getAssignedUserShoppingList(){
        DatabaseReference assignedUserList = FirebaseDatabase.getInstance().getReference()
                .child("users").child("User").child(mUserId).child("Shopping List");
        assignedUserList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
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

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("volunteersAvailable");

        GeoFire geo = new GeoFire(ref);
        geo.setLocation(userId, new GeoLocation(mLocation.getLatitude(), mLocation.getLongitude()));
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

        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient,this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("volunteersAvailable");

        GeoFire geo = new GeoFire(ref);
        geo.removeLocation(userId);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
