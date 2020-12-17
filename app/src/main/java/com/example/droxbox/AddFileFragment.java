package com.example.droxbox;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.droxbox.homeModule.HomeActivity;
import com.example.droxbox.pojo.File;
import com.example.droxbox.singletons.FirestoreAPI;
import com.example.droxbox.singletons.StorageAPI;
import com.example.droxbox.singletons.UserSingleton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


public class AddFileFragment extends DialogFragment implements DialogInterface.OnShowListener {

    private static final int RC_GALLERY = 21;
    private static final int RC_CAMERA = 22;

    private static final int RP_CAMERA = 121;
    private static final int RP_STORAGE = 122;

    private static final String IMAGE_DIRECTORY = "/DroxBox";
    private static final String MY_PHOTO = "my_file";

    private static final String PATH_FILE = "files";

    private static final String CAMERA_TYPE = "camera";
    private static final String GALLERY_TYPE = "gallery";

    private StorageAPI mStorageAPI;
    private FirestoreAPI mFirestoreAPI;
    private UserSingleton mUserSingleton;

    private String mCurrentPhotoPATH;
    private Uri mPhotoSelectedUri;

    private ImageView ivChoose, ivPhoto;
    private ConstraintLayout mView;
    private EditText etFileName;
    private ProgressBar uploadPB;
    private String fileName;
    private String type;

    public AddFileFragment(String type) {
        this.type = type;
        Log.i("type", type);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setPositiveButton(getContext().getString(R.string.dialog_upload_btn), null)
                .setNegativeButton(getContext().getString(R.string.dialog_cancel_btn), null);

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_file, null);
        builder.setView(view);

        mView = view.findViewById(R.id.add_view);

        ivChoose = view.findViewById(R.id.iv_choose_action);
        ivPhoto = view.findViewById(R.id.iv_file);

        etFileName = view.findViewById(R.id.et_fileName);

        uploadPB = view.findViewById(R.id.pb_uploadFile);

        mStorageAPI = StorageAPI.getInstance();
        mFirestoreAPI = FirestoreAPI.getInstance();
        mUserSingleton = UserSingleton.getInstance();

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(this);
        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        final AlertDialog dialog = (AlertDialog)getDialog();

        if ( dialog != null ) {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

            ivChoose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( type.equals(GALLERY_TYPE) ) {
                        fromGallery();
                    }else if ( type.equals(CAMERA_TYPE) ) {
                        checkPermissionToApp(Manifest.permission.CAMERA, RP_CAMERA);
                    }
                }
            });

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( validateName(etFileName) ) {
                        uploadPB.setVisibility(View.VISIBLE);
                        uploadFile();
                    }
                }
            });

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    private void checkPermissionToApp(String permissionStr, int requestPermission) {
        if (ContextCompat.checkSelfPermission(getContext(), permissionStr) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{permissionStr}, requestPermission);
            return;
        }

        switch (requestPermission){
            case RP_STORAGE:
                fromGallery();
                break;
            case RP_CAMERA:
                fromCamera();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            switch (requestCode){
                case RP_STORAGE:
                    fromGallery();
                    break;
                case RP_CAMERA:
                    fromCamera();
                    break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void fromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_GALLERY);
    }

    private void fromCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if ( intent.resolveActivity(getActivity().getPackageManager()) != null) {
            java.io.File photoFile;
            photoFile = createImageFile();

            if ( photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(getContext(),
                        getString(R.string.package_name), photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, RC_CAMERA);
            }
        }
    }

    private java.io.File createImageFile(){
        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date());
        final String imageFileName = fileName + timeStamp + "_";
        java.io.File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        java.io.File image = null;
        try {
            image = java.io.File.createTempFile(imageFileName, ".jpg", storageDir);
            mCurrentPhotoPATH = image.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    private void uploadFile(){
        if ( mStorageAPI != null ) {
            if ( mPhotoSelectedUri != null ){
                mStorageAPI.getUserCarpet(mUserSingleton.getUser().getUid()).child(fileName).putFile(mPhotoSelectedUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                 taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                     @Override
                                     public void onSuccess(Uri uri) {
                                         if ( uri != null ) {
                                             savePhotoUri(uri);
                                         }
                                     }
                                 });

                            }
                        });
            }else {
                uploadPB.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getContext().getString(R.string.picture_required_message), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateName( EditText etFileName ) {
        boolean isValid = true;

        if ( etFileName.getText().toString().trim().isEmpty() ) {
            etFileName.setError(getActivity().getString(R.string.name_required_message));
        }else{
            fileName = etFileName.getText().toString().trim();
            if ( mUserSingleton.getUser().getFiles() != null ) {
                if ( mUserSingleton.getUser().getFiles().size() > 0 ) {
                    for ( File file :
                            mUserSingleton.getUser().getFiles()) {

                        if ( file.getName().equals(fileName) ) {
                            etFileName.setError(getString(R.string.file_exist_message));
                            isValid = false;
                        }
                    }
                }
            }
        }

        return isValid;
    }

    private void savePhotoUri( Uri uri ) {
        File file = new File();
        file.setName(fileName);
        file.setUrl(uri.toString());

        if ( file.getHistory() == null ){
            file.setHistory(new ArrayList<>());
        }
        file.getHistory().add(getDate());

        if ( mUserSingleton.getUser().getFiles() == null ) {
            mUserSingleton.getUser().setFiles(new ArrayList<>());
        }

        if ( mUserSingleton.getUser().getFiles().size()  == 0 ) {
            mUserSingleton.getUser().getFiles().add(file);
        }else{
            if ( !containsFile(file) ) {
                mUserSingleton.getUser().getFiles().add(file);
            }
        }

        Log.i("UserData", mUserSingleton.getUser().toString());


        mFirestoreAPI.getUserByUid(mUserSingleton.getUser().getUid()).set(mUserSingleton.getUser())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if ( task.isSuccessful() ) {
                            Toast.makeText(getActivity(), R.string.upload_success_message, Toast.LENGTH_SHORT).show();
                            uploadPB.setVisibility(View.INVISIBLE);
                            dismiss();
                        }else{
                            Toast.makeText(getActivity(), R.string.upload_error_message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean containsFile(File f){
        boolean contains = false;
        for ( File file :
                mUserSingleton.getUser().getFiles() ) {
            if ( file.getName().equals(f.getName()) ) {
                contains = true;
                break;
            }
        }
        return contains;
    }
    private String getDate(){
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") String tf = new SimpleDateFormat("yyyy-MM-dd").format(date);
        return  tf;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK ) {
            switch (requestCode){
                case RC_GALLERY:
                        if ( data != null ) {
                            mPhotoSelectedUri = data.getData();
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                                        mPhotoSelectedUri);
                                ivPhoto.setImageBitmap(bitmap);
                                ivPhoto.setVisibility(View.VISIBLE);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    break;
                case  RC_CAMERA:
                        mPhotoSelectedUri = addPictGallery();
                    try {
                    Bitmap bitmap;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        bitmap = ImageDecoder.decodeBitmap(ImageDecoder
                                .createSource(getActivity().getContentResolver(), mPhotoSelectedUri));
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                                mPhotoSelectedUri);
                    }
                        ivPhoto.setImageBitmap(bitmap);
                        ivPhoto.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    break;
            }
        }
    }

    private Uri addPictGallery(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        java.io.File file = new java.io.File(mCurrentPhotoPATH);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        mCurrentPhotoPATH = null;
        return contentUri;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(getContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}