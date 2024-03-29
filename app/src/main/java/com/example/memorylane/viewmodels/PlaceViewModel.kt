package com.example.memorylane.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.memorylane.database.DatabaseHandler
import com.example.memorylane.models.PlaceModel

class PlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHandler: DatabaseHandler = DatabaseHandler(application)
    private val userViewModel: UserViewModel = UserViewModel(application)

    fun getPLacesList(): ArrayList<PlaceModel> {
        val placesList = dbHandler.getPlacesList(userViewModel.getLoggedInUserId())
        Log.d("PlaceViewModel", "Places List: $placesList")
        return placesList
    }

    fun getPlaceById(placeId: Int): PlaceModel? {
        return dbHandler.getPlaceById(placeId)
    }

    fun addPlace(placeModel: PlaceModel): Long {
        Log.d("Database", "Added place: $placeModel")
        return dbHandler.createPlace(placeModel)
    }

    fun updatePlace(placeModel: PlaceModel): Int {
        return dbHandler.updatePlace(placeModel)
    }

    fun deletePlace(placeModel: PlaceModel): Int {
        return dbHandler.deletePlace(placeModel)
    }
}