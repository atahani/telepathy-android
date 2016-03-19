package com.atahani.telepathy.realm.adapter;

import android.content.Context;

import com.atahani.telepathy.realm.utility.RealmModelAdapter;

import io.realm.RealmResults;
import com.atahani.telepathy.realm.UserModelRealm;

/**
 * Realm Friend adapter
 */
public class RealmFriendAdapter extends RealmModelAdapter<UserModelRealm> {
    public RealmFriendAdapter(Context context, RealmResults<UserModelRealm> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }
}
