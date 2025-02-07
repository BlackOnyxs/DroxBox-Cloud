package com.example.droxbox.homeModule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.droxbox.Fragments.AboutUsFragment;
import com.example.droxbox.Fragments.AddFileFragment;
import com.example.droxbox.R;
import com.example.droxbox.Fragments.FileDetailFragment;
import com.example.droxbox.ScrollingActivity;
import com.example.droxbox.pojo.File;
import com.example.droxbox.pojo.User;
import com.example.droxbox.singletons.AuthAPI;
import com.example.droxbox.singletons.FirestoreAPI;
import com.example.droxbox.singletons.StorageAPI;
import com.example.droxbox.singletons.UserSingleton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements OnItemClickListener, View.OnClickListener{

    private static final String CAMERA_TYPE = "camera";
    private static final String GALLERY_TYPE = "gallery";

    private FirestoreAPI mFirestoreAPI;
    private UserSingleton mUserSingleton;
    private AuthAPI mAuthAPI;
    private StorageAPI mStorageAPI;
    private FilesAdapter mFilesAdapter;
    private RecyclerView mFilesRV;
    private Boolean isFabOpen = false;
    private FloatingActionButton fabAdd, fabCamera, fabGallery;
    private Animation fabOpen, fabClose, rotateForward, rotateBackward;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        configExtendedFab(getApplicationContext());

        mAuthAPI = AuthAPI.getInstance();
        mStorageAPI = StorageAPI.getInstance();

        TextView tvEmpty = findViewById(R.id.tv_empty);
        TextView tvTitle = findViewById(R.id.tv_title);

        pb = findViewById(R.id.pb_home);

        mFilesRV = findViewById(R.id.rv_homeFiles);
        mFirestoreAPI = FirestoreAPI.getInstance();

        configRV(getApplicationContext());
        mUserSingleton = UserSingleton.getInstance();

        if (mUserSingleton.getUser().getUid() != null) {
            pb.setVisibility(View.GONE);
            listenUser(mUserSingleton.getUser().getUid(), tvTitle, tvEmpty);
        }
    }

    private void configExtendedFab(Context context){
        fabAdd = findViewById(R.id.fab_add);
        fabCamera = findViewById(R.id.fab_camera);
        fabGallery = findViewById(R.id.fab_gallery);

        fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward);

        fabAdd.setOnClickListener(this);
        fabGallery.setOnClickListener(this);
        fabCamera.setOnClickListener(this);
    }

    private void configRV(Context context){
        mFilesAdapter = new FilesAdapter(new ArrayList<>(), context, this);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mFilesRV.addItemDecoration(itemDecoration);

        mFilesRV.setAdapter(mFilesAdapter);
        mFilesRV.setLayoutManager(manager);
    }

    private void addFiles(User user){
        for ( File f :
                user.getFiles()){
            if ( !f.getName().isEmpty() ) {
                mFilesAdapter.add(f);
                if ( mUserSingleton.getUser().getFiles() == null ){
                    mUserSingleton.getUser().setFiles(new ArrayList<>());
                }
                mUserSingleton.getUser().getFiles().add(f);
            }
        }
    }

    private void listenUser(String uid, TextView tvTitle, TextView tvEmpty){
        mFirestoreAPI.getUserByUid(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if ( error == null) {
                    if ( value != null ) {
                        try {
                            User currentUser = value.toObject(User.class);
                            if ( mUserSingleton.getUser().getFiles() != null ) {
                                mUserSingleton.getUser().getFiles().clear();
                            }
                            if ( currentUser.getFiles() != null ) {
                                if ( currentUser.getFiles().size() > 0 ) {
                                    addFiles(currentUser);
                                    tvTitle.setVisibility(View.VISIBLE);
                                    mFilesRV.setVisibility(View.VISIBLE);
                                    tvEmpty.setVisibility(View.GONE);
                                }
                            }
                        }catch ( Exception e){
                            e.printStackTrace();
                        }
                    }

                }else{
                    if ( error.getMessage() != null ){
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onClickListener(File file) {
        new FileDetailFragment(file).show(getSupportFragmentManager(), "");
    }

    @Override
    public void onLongClickListener(File file) {
        new AlertDialog.Builder(this)
                .setTitle("Delete File")
                .setMessage("Do you want to delete "+ file.getName())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFle(file);
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                    }
                }).setNegativeButton(getString(R.string.dialog_cancel_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    private void deleteFle(File file) {
        if ( mUserSingleton.getUser().getFiles().contains( file ) ) {
           // int index = mUserSingleton.getUser().getFiles().indexOf(file);
            mUserSingleton.getUser().getFiles().remove(file);
            mFirestoreAPI.getUserByUid( mUserSingleton.getUser().getUid() ).set(mUserSingleton.getUser())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), getString(R.string.firestore_success_deleted), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_aboutUs:
                new AboutUsFragment().show(getSupportFragmentManager(), "");
                break;
            case R.id.menu_logout:
                mAuthAPI.getFirebaseAuth().signOut();
                mUserSingleton.reset();
                Intent intent = new Intent(this, ScrollingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch ( v.getId() ){

            case R.id.fab_add:
                    animateFAB();
                break;
            case R.id.fab_camera:
                new AddFileFragment(CAMERA_TYPE).show(getSupportFragmentManager(), "Add File");
                break;
            case R.id.fab_gallery:
                new AddFileFragment(GALLERY_TYPE).show(getSupportFragmentManager(), "Add File");
            break;
        }
    }

    public void animateFAB(){

        if(isFabOpen){

            fabAdd.startAnimation(rotateBackward);
            fabGallery.startAnimation(fabClose);
            fabCamera.startAnimation(fabClose);
            fabGallery.setClickable(false);
            fabCamera.setClickable(false);
            isFabOpen = false;

        } else {

            fabAdd.startAnimation(rotateForward);
            fabGallery.startAnimation(fabOpen);
            fabCamera.startAnimation(fabOpen);
            fabGallery.setClickable(true);
            fabCamera.setClickable(true);
            isFabOpen = true;

        }
    }


}