package com.example.droxbox.singletons;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StorageAPI {
    private FirebaseStorage mFirebaseStorage;

    private static class Singleton{
        private static final StorageAPI INSTANCE = new StorageAPI();
    }

    public static  StorageAPI getInstance(){
        return Singleton.INSTANCE;
    }

    private StorageAPI(){
        this.mFirebaseStorage = FirebaseStorage.getInstance();
    }

    public FirebaseStorage getFirebaseStorage() {
        return mFirebaseStorage;
    }

    public StorageReference getUserCarpet( String uid ){
        return mFirebaseStorage.getReference().child(uid);
    }
}
