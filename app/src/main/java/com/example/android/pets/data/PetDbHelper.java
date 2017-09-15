package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.data.PetContract.PetEntry;
/**
 * Created by nguyennguyen on 8/29/17.
 */

public class PetDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Pet.db";
    private static final String SQL_CREATE_ENTRY = "CREATE TABLE " + PetEntry.TABLE_NAME + " (" +
            PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PetEntry.PET_NAME + " TEXT NOT NULL, " +
            PetEntry.PET_BREED + " TEXT, " +
            PetEntry.PET_GENDER + " INTEGER NOT NULL, " +
            PetEntry.PET_WEIGHT + " INTEGER DEFAULT 0);";

    //private static final ;
    private static final String SQL_DELETE_ENTRY = "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME;

    public PetDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

        db.execSQL(SQL_CREATE_ENTRY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_ENTRY);
        onCreate(db);
    }

}
