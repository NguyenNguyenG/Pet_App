package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;
/**
 * Created by nguyennguyen on 9/1/17.
 */

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDbHelper helper;

    public static final int PET_CODE = 1;
    public static final int PET_ID_CODE = 2;

    //will used to link the code number to URI Patterns;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Run only once before constructor -> executed 1st time an instance is created
    // but not the 2nd,3rd,... objects of class are created
    static
    {
        // Mapping PET_CODE to URI Pattern: Content://com.example.android.pets/pet
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PET_CODE);

        //Mapping PET_ID_CODE to URI Pattern: Content://com.example.android.pets/pet/#
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID_CODE);
    }

    @Override
    public boolean onCreate() {
        helper = new PetDbHelper(getContext());

        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = helper.getReadableDatabase();

        int match = sUriMatcher.match(uri);

        switch (match)
        {
            case PET_CODE:
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID_CODE:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String [] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PET_CODE:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID_CODE:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("getType is not supported with" + uri);
        }
        
    }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        String name = contentValues.getAsString(PetEntry.PET_NAME);
        if(name == null)
            throw new IllegalArgumentException("Pet requires a name");

        Integer weight = contentValues.getAsInteger(PetEntry.PET_WEIGHT);
        if(weight != null && weight < 0)
            throw new IllegalArgumentException("Pet cannot have negative weight");

        Integer gender = contentValues.getAsInteger(PetEntry.PET_GENDER);
        if(gender == null || gender != PetEntry.GENDER_FEMALE && gender != PetEntry.GENDER_MALE && gender != PetEntry.GENDER_UNKOWN)
            throw new IllegalArgumentException("Pet has an invalid gender choice");

        int match = sUriMatcher.match(uri);

        switch (match)
        {
            case PET_CODE:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not support for" + uri);
        }


    }

    private Uri insertPet(Uri uri, ContentValues contentValues)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(PetEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);

        int deletedRow = 0;
        SQLiteDatabase db = helper.getWritableDatabase();

        switch (match)
        {
            case PET_CODE:
                deletedRow = db.delete(PetEntry.TABLE_NAME,selection, selectionArgs);
                break;
            case PET_ID_CODE:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String [] {String.valueOf(ContentUris.parseId(uri))};
                deletedRow = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete not support for" + uri);
        }
        if(deletedRow != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return deletedRow;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if( contentValues.size() == 0)
            return 0;

        if(contentValues.containsKey(PetEntry.PET_NAME)) {
            String name = contentValues.getAsString(PetEntry.PET_NAME);
            if (name == null)
                throw new IllegalArgumentException("Pet requires a name");
        }

        if(contentValues.containsKey(PetEntry.PET_WEIGHT)) {
            Integer weight = contentValues.getAsInteger(PetEntry.PET_WEIGHT);
            if (weight != null && weight < 0)
                throw new IllegalArgumentException("Pet cannot have negative weight");
        }

        if(contentValues.containsKey(PetEntry.PET_GENDER)) {
            Integer gender = contentValues.getAsInteger(PetEntry.PET_GENDER);
            if (gender == null || gender != PetEntry.GENDER_FEMALE && gender != PetEntry.GENDER_MALE && gender != PetEntry.GENDER_UNKOWN)
                throw new IllegalArgumentException("Pet has an invalid gender choice");
        }

        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = helper.getWritableDatabase();
        int rowUpdated = 0;

        switch (match){
            case PET_CODE:
                rowUpdated = db.update(PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case PET_ID_CODE:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String []{String.valueOf(ContentUris.parseId(uri))};
                rowUpdated = db.update(PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update is not available for" + uri);
        }
        if(rowUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowUpdated;
    }
}
