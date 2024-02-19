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
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "MemoryLaneDatabase"
        private const val TABLE_PLACE = "PlaceTable"
        private const val TABLE_USER = "UserTable"

        //places table columns
        private const val ID = "id"
        private const val CREATOR_ID = "creator_id"
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
                + CREATOR_ID + " INTEGER,"
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
                + EMAIL + " TEXT UNIQUE,"
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
        contentValues.put(CREATOR_ID, place.creatorId)
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
    fun getPlacesList(userId: Int): ArrayList<PlaceModel> {
        val placesList = ArrayList<PlaceModel>()
        val selectQuery = "SELECT * FROM $TABLE_PLACE WHERE $CREATOR_ID = ?"
        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, arrayOf(userId.toString()))
            cursor.use { c ->
                if (cursor.moveToFirst()) {
                    do {
                        val place = PlaceModel(
                            c.getInt(c.getColumnIndex(ID)),
                            c.getInt(c.getColumnIndex(CREATOR_ID)),
                            c.getString(c.getColumnIndex(TITLE)),
                            c.getString(c.getColumnIndex(IMAGE)),
                            c.getString(c.getColumnIndex(DESCRIPTION)),
                            c.getString(c.getColumnIndex(DATE)),
                            c.getString(c.getColumnIndex(LOCATION)),
                            c.getDouble(c.getColumnIndex(LATITUDE)),
                            c.getDouble(c.getColumnIndex(LONGITUDE))
                        )
                        placesList.add(place)
                    } while (c.moveToNext())
                }
            }
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
                    c.getInt(c.getColumnIndex(CREATOR_ID)),
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

    fun createUser(user: UserModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(NAME, user.name)
        contentValues.put(EMAIL, user.email)
        contentValues.put(PASSWORD, user.password)

        val result = db.insert(TABLE_USER, null, contentValues)

        db.close()
        return result
    }

    // Inside DatabaseHandler class
    @SuppressLint("Range")
    fun getUserById(userId: Int): UserModel? {
        val db = this.readableDatabase
        var user: UserModel? = null
        val selectQuery = "SELECT * FROM $TABLE_USER WHERE $USER_ID = $userId"

        val cursor: Cursor = db.rawQuery(selectQuery, null)
        cursor.use { c ->
            if (c.moveToFirst()) {
                user = UserModel(
                    c.getInt(c.getColumnIndex(USER_ID)),
                    c.getString(c.getColumnIndex(NAME)),
                    c.getString(c.getColumnIndex(EMAIL)),
                    c.getString(c.getColumnIndex(PASSWORD))
                )
            }
        }

        return user
    }


    @SuppressLint("Range")
    fun getUserByEmail(email: String): UserModel? {
        val db = this.readableDatabase
        var user: UserModel? = null
        val selectQuery = "SELECT * FROM $TABLE_USER WHERE $EMAIL = ?"

        val cursor: Cursor = db.rawQuery(selectQuery, arrayOf(email))
        cursor.use { c ->
            if (c.moveToFirst()) {
                user = UserModel(
                    c.getInt(c.getColumnIndex(USER_ID)),
                    c.getString(c.getColumnIndex(NAME)),
                    c.getString(c.getColumnIndex(EMAIL)),
                    c.getString(c.getColumnIndex(PASSWORD))
                )
            }
        }

        return user
    }
}