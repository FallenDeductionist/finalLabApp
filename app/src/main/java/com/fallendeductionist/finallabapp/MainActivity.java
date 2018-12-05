package com.fallendeductionist.finallabapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST = 100;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "user: " + firebaseUser);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "currentUser: " + currentUser);

        User user = new User();
        user.setUid(currentUser.getUid());
        user.setDisplayName(currentUser.getDisplayName());
        user.setEmail(currentUser.getEmail());
        user.setPhotoUrl((currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null));

        //location thing

        Log.d(TAG, "myLocation");

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                new AlertDialog.Builder(this)
                        .setMessage("Para verificar su ubicación se requiere activar el GPS.")
                        .setPositiveButton("Habilitar GPS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        }).create().show();
                return;
            }

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null)
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                Log.d(TAG, "getLastKnownLocation by " + location.getProvider());

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "getLastKnownLocation LatLng: " + latLng);

                user.setLatitude(location.getLatitude());
                user.setLongitude(location.getLongitude());
            }

        }

        // Lista de usuarios con RecyclerView
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final UserAdapter userAdapter = new UserAdapter();
        recyclerView.setAdapter(userAdapter);

        // Obteniendo lista de usuarios de Firebase (con realtime)
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        ChildEventListener childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded " + dataSnapshot.getKey());

                String postKey = dataSnapshot.getKey();
                final User addedUser = dataSnapshot.getValue(User.class);
                Log.d(TAG, "addedUser " + addedUser);

                // Actualizando adapter datasource
                List<User> users = userAdapter.getUsers();
                users.add(0, addedUser);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged " + dataSnapshot.getValue());

                String postKey = dataSnapshot.getKey();
                User changedUser = dataSnapshot.getValue(User.class);
                Log.d(TAG, "changedUser " + changedUser);
                // Actualizando adapter datasource
                List<User> users = userAdapter.getUsers();
                int index = users.indexOf(changedUser);
                if (index != -1) {
                    users.set(index, changedUser);
                }
                userAdapter.notifyDataSetChanged();

            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved " + dataSnapshot.getKey());

                String postKey = dataSnapshot.getKey();
                User removedUser = dataSnapshot.getValue(User.class);
                Log.d(TAG, "removedUser " + removedUser);

                // Actualizando adapter datasource
                List<User> users = userAdapter.getUsers();
                users.remove(removedUser);
                userAdapter.notifyDataSetChanged();

                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved " + dataSnapshot.getKey());
                User movedUser = dataSnapshot.getValue(User.class);
                String postKey = dataSnapshot.getKey();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled " + databaseError.getMessage(), databaseError.toException());
            }
        };

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

        userRef.addChildEventListener(childEventListener);

            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.child(user.getUid()).setValue(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onSuccess");
                            } else {
                                Log.e(TAG, "onFailure", task.getException());
                            }
                        }
                    });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                callLogout(null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private EditText usernameText;

    public void callChange(View view){

        usernameText = findViewById(R.id.username);
        String username = usernameText.getText().toString();

        FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username).build();
        user.updateProfile(profileUpdates);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(user.getUid()).child("displayName").setValue(user.getDisplayName());

        Toast.makeText(this,"por alguna razón se necesita presionar el lápiz 2 veces", Toast.LENGTH_LONG).show();
    }

    private void callLogout(View view){
        Log.d(TAG, "Sign out user");
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }



}

