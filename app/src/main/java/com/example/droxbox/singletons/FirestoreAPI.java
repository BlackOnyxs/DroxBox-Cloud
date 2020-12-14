package com.example.droxbox.singletons;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreAPI {

    private static final String USER_COLLECTION = "users";

    private FirebaseFirestore mFirestore;

    private static FirestoreAPI INSTANCE = null;

    private FirestoreAPI(){
        this.mFirestore = FirebaseFirestore.getInstance();
    }

    public static FirestoreAPI getInstance(){
        if ( INSTANCE == null ){
            INSTANCE = new FirestoreAPI();
        }

        return INSTANCE;
    }

    public FirebaseFirestore getFirestore(){
        return mFirestore;
    }

    //References

    public CollectionReference getUserCollection(){
        return mFirestore.collection(USER_COLLECTION);
    }

    public DocumentReference getUserByUid(String uid){
        return getUserCollection().document(uid);
    }

}
