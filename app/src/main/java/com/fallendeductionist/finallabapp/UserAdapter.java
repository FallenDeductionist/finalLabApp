package com.fallendeductionist.finallabapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private static final String TAG = UserAdapter.class.getSimpleName();

    private List<User> users;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> posts) {
        this.users = posts;
    }

    public UserAdapter(){
        this.users = new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImage;
        TextView displaynameText;
        TextView locationText;
        TextView emailText;

        ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_picture);
            displaynameText = itemView.findViewById(R.id.user_displayname);
            locationText = itemView.findViewById(R.id.distance);
            emailText = itemView.findViewById(R.id.user_email);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_example, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        final User user = users.get(position);

        viewHolder.locationText.setText("Lat: " + user.getLatitude() + " \nLong: " + user.getLongitude());

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange " + dataSnapshot.getKey());
                User user = dataSnapshot.getValue(User.class);
                Picasso.with(viewHolder.itemView.getContext()).load(user.getPhotoUrl()).placeholder(R.drawable.ic_profile).into(viewHolder.userImage);
                viewHolder.displaynameText.setText(user.getDisplayName());
                viewHolder.emailText.setText(user.getEmail());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled " + databaseError.getMessage(), databaseError.toException());
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

}
