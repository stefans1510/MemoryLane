package com.example.memorylane.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.memorylane.models.PlaceModel
import com.example.memorylane.models.UserModel

class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "MemoryLaneDatabase"
        private const val TABLE_PLACE = "PlaceTable"
        private const val TABLE_USER = "UserTable"

        //places table columns
        private const val ID = "id"
        private const val TITLE = "title"
        private const val IMAGE = "image"
        private const val DESCRIPTION = "description"
        private const val DATE = "date"
        private const val LOCATION = "location"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"

        //users table columns
        private const val USER_ID = "user_id"
        private const val NAME = "name"
        private const val EMAIL = "email"
        private const val PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createPlaceTable = ("CREATE TABLE " + TABLE_PLACE + " ("
                + ID + " INTEGER PRIMARY KEY,"
                + TITLE + " TEXT,"
                + IMAGE + " TEXT,"
                + DESCRIPTION + " TEXT,"
                + DATE + " TEXT,"
                + LOCATION + " TEXT,"
                + LATITUDE + " TEXT,"
                + LONGITUDE + " TEXT)")
        db?.execSQL(createPlaceTable)

        val createUserTable = ("CREATE TABLE " + TABLE_USER + " ("
                + USER_ID + " INTEGER PRIMARY KEY,"
                + NAME + " TEXT,"
                + EMAIL + " TEXT,"
                + PASSWORD + " TEXT)")
        db?.execSQL(createUserTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLACE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    fun createPlace(place: PlaceModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(TITLE, place.title)
        contentValues.put(IMAGE, place.image)
        contentValues.put(DESCRIPTION, place.description)
        contentValues.put(DATE, place.date)
        contentValues.put(LOCATION, place.location)
        contentValues.put(LATITUDE, place.latitude)
        contentValues.put(LONGITUDE, place.longitude)

        val result = db.insert(TABLE_PLACE, null, contentValues)

        db.close()
        return result
    }

    fun updatePlace(place: PlaceModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(TITLE, place.title)
        contentValues.put(IMAGE, place.image)
        contentValues.put(DESCRIPTION, place.description)
        contentValues.put(DATE, place.date)
        contentValues.put(LOCATION, place.location)
        contentValues.put(LATITUDE, place.latitude)
        contentValues.put(LONGITUDE, place.longitude)

        val result = db.update(TABLE_PLACE, contentValues, ID + "=" + place.id, null)

        db.close()
        return result
    }

    fun deletePlace(place: PlaceModel): Int {
        val db = this.writableDatabase

        val result = db.delete(TABLE_PLACE, ID + "=" + place.id, null)

        db.close()
        return result
    }

    @SuppressLint("Range")
    fun getPlacesList(): ArrayList<PlaceModel> {
        val placesList = ArrayList<PlaceModel>()
        val selectQuery = "SELECT * FROM $TABLE_PLACE"
        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, null)

            if (cursor.moveToFirst()) {
                do {
                    val place = PlaceModel(
                        cursor.getInt(cursor.getColumnIndex(ID)),
                        cursor.getString(cursor.getColumnIndex(TITLE)),
                        cursor.getString(cursor.getColumnIndex(IMAGE)),
                        cursor.getString(cursor.getColumnIndex(DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(DATE)),
                        cursor.getString(cursor.getColumnIndex(LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(LONGITUDE))
                    )
                    placesList.add(place)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        return placesList
    }

    @SuppressLint("Range")
    fun getPlaceById(placeId: Int): PlaceModel? {
        val db = this.readableDatabase
        var place: PlaceModel? = null
        val selectQuery = "SELECT * FROM $TABLE_PLACE WHERE $ID = $placeId"

        val cursor: Cursor = db.rawQuery(selectQuery, null)
        cursor.use { c ->
            if (c.moveToFirst()) {
                place = PlaceModel(
                    c.getInt(c.getColumnIndex(ID)),
                    c.getString(c.getColumnIndex(TITLE)),
                    c.getString(c.getColumnIndex(IMAGE)),
                    c.getString(c.getColumnIndex(DESCRIPTION)),
                    c.getString(c.getColumnIndex(DATE)),
                    c.getString(c.getColumnIndex(LOCATION)),
                    c.getDouble(c.getColumnIndex(LATITUDE)),
                    c.getDouble(c.getColumnIndex(LONGITUDE))
                )
            }
        }
        return place
    }

    /*fun addUser(user: UserModel): Long {
    val db = this.writableDatabase

    val contentValues = ContentValues()
    contentValues.put(NAME, user.name)
    contentValues.put(EMAIL, user.email)
    contentValues.put(PASSWORD, user.password)

    val result = db.insert(TABLE_USER, null, contentValues)

    db.close()
    return result
    }

    @SuppressLint("Range")
    fun getUsersList(): ArrayList<UserModel> {
        val usersList = ArrayList<UserModel>()
        val selectQuery = "SELECT * FROM $TABLE_USER"
        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, null)

            if (cursor.moveToFirst()) {
                do {
                    val user = UserModel(
                        cursor.getInt(cursor.getColumnIndex(USER_ID)),
                        cursor.getString(cursor.getColumnIndex(NAME)),
                        cursor.getString(cursor.getColumnIndex(EMAIL)),
                        cursor.getString(cursor.getColumnIndex(PASSWORD))
                    )
                    usersList.add(user)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        return usersList
    }*/
}