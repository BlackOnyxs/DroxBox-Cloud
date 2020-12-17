package com.example.droxbox.fileDetailModule;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.droxbox.R;
import com.example.droxbox.homeModule.HomeActivity;
import com.example.droxbox.pojo.File;
import com.example.droxbox.singletons.FirestoreAPI;
import com.example.droxbox.singletons.UserSingleton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class FileDetailFragment  extends DialogFragment implements DialogInterface.OnShowListener {

    private File currentFile;
    private TextView  tvCreatedDate, tvLastModified;
    EditText etFileName;
    private ImageView ivFile;

    private UserSingleton mUserSingleton;
    private FirestoreAPI mFirestoreAPI;
    public FileDetailFragment(File file) {
        this.currentFile = file;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.dialog_ok_btn, null)
                .setNegativeButton(R.string.dialog_cancel_btn, null);

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_file_detail, null);
        builder.setView(view);

        mUserSingleton = UserSingleton.getInstance();
        mFirestoreAPI = FirestoreAPI.getInstance();

        etFileName = view.findViewById(R.id.etFileNameHolder);
        tvCreatedDate = view.findViewById(R.id.tvCreatedDateHolder);
        tvLastModified = view.findViewById(R.id.tvLastModifiedHolder);

        etFileName.setText(currentFile.getName());
        tvCreatedDate.setText(currentFile.getHistory().get(0));
        tvLastModified.setText(currentFile.getHistory().get( currentFile.getHistory().size() - 1));

        ivFile = view.findViewById(R.id.iv_holder);

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();

        Glide.with(getActivity())
                .asBitmap()
                .load(currentFile.getUrl())
                .apply(options)
                .into(ivFile);

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

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    validateUpload(etFileName);
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

    private void validateUpload(EditText etFileName) {
        String fileName;
        if ( etFileName.getText().toString().trim().isEmpty() ) {
            etFileName.setError(getString(R.string.name_required_message));
        }else{
            fileName = etFileName.getText().toString().trim();
            currentFile.setName(fileName);
            if ( mUserSingleton.getUser().getFiles().contains( currentFile ) ) {
                int index = mUserSingleton.getUser().getFiles().indexOf(currentFile);
                mUserSingleton.getUser().getFiles().set(index, currentFile);
                mFirestoreAPI.getUserByUid(mUserSingleton.getUser().getUid())
                        .set(mUserSingleton.getUser()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), getContext().getString(R.string.firestore_succes_update), Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),  getContext().getString(R.string.firestore_error_update), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
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