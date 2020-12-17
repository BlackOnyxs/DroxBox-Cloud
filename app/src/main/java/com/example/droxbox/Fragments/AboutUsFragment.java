package com.example.droxbox.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.example.droxbox.R;
import com.example.droxbox.singletons.StorageAPI;
import com.google.android.gms.tasks.OnSuccessListener;


public class AboutUsFragment extends DialogFragment implements DialogInterface.OnShowListener {

    private StorageAPI mStorageAPI;
    private VideoView mVideoView;


    public AboutUsFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setPositiveButton(getContext().getString(R.string.dialog_ok_btn), null);

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_about_us, null);
        builder.setView(view);

        mStorageAPI = StorageAPI.getInstance();
        mVideoView = view.findViewById(R.id.vvCredits);
        ProgressBar pb = view.findViewById(R.id.pb_video);

        mStorageAPI.getFirebaseStorage().getReference().child("Credits.mp4").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if ( uri != null ) {
                            pb.setVisibility(View.GONE);
                            mVideoView.setVideoPath(uri.toString());
                            mVideoView.start();
                        }
                    }
                });


        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(this);
        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        final AlertDialog dialog = (AlertDialog)getDialog();

        if ( dialog != null ) {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        }
    }
}