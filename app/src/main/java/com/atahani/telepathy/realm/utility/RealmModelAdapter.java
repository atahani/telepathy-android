package com.atahani.telepathy.realm.utility;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import io.realm.RealmBaseAdapter;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * create custom RealmModelAdapter base on RealmBaseAdapter
 */
public class RealmModelAdapter<T extends RealmObject> extends RealmBaseAdapter<T> {

    public RealmModelAdapter(Context context, RealmResults<T> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    // I'm not sorry.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}