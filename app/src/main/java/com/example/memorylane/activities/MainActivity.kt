package com.example.memorylane.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorylane.R
import com.example.memorylane.adapters.PlaceAdapter
import com.example.memorylane.models.PlaceModel
import com.example.memorylane.utils.SwipeToDeleteCallback
import com.example.memorylane.utils.SwipeToEditCallback
import com.example.memorylane.viewmodels.PlaceViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), PlaceAdapter.OnPlaceItemClickListener {
    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var rvPlacesList: RecyclerView
    private lateinit var tvNoRecordsFound: TextView
    private lateinit var fabAddPLace: FloatingActionButton
    private lateinit var btnHelp: Button
    private lateinit var addPlaceLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        placeAdapter = PlaceAdapter(this, ArrayList(), placeViewModel = PlaceViewModel(application), this)
        rvPlacesList = findViewById(R.id.rv_places_list)
        rvPlacesList.layoutManager = LinearLayoutManager(this)
        rvPlacesList.adapter = placeAdapter
        tvNoRecordsFound = findViewById(R.id.tv_no_records_found)
        fabAddPLace = findViewById(R.id.fab_add_place)
        btnHelp = findViewById(R.id.btn_help)

        addPlaceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if (result.resultCode == RESULT_OK) {
                displayPlaces()
            }
        }

        fabAddPLace.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)  //explicit intent
            addPlaceLauncher.launch(intent) // 1. invoke the activity to add a place -> handle the res. in activity
        }

        btnHelp.setOnClickListener {
            Toast.makeText(
                this, "Swipe right on the place card to edit it, or swipe left to delete it", Toast.LENGTH_LONG
            ).show()
        }

        displayPlaces()
    }

    private fun displayPlaces() {
        val placeViewModel = PlaceViewModel(application)
        val placesList: ArrayList<PlaceModel> = placeViewModel.getPLacesList()

        if (placesList.isNotEmpty()) {
            rvPlacesList.visibility = View.VISIBLE
            tvNoRecordsFound.visibility = View.GONE
            btnHelp.visibility = View.VISIBLE
            placeAdapter.setData(placesList)
        } else {
            rvPlacesList.visibility = View.GONE
            tvNoRecordsFound.visibility = View.VISIBLE
            btnHelp.visibility = View.GONE
        }

        val editSwipeHelper = object: SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val intent = Intent(rvPlacesList.context, MainActivity::class.java)
                addPlaceLauncher.launch(intent)
                placeAdapter = rvPlacesList.adapter as PlaceAdapter
                placeAdapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHelper)
        editItemTouchHelper.attachToRecyclerView(rvPlacesList)

        val deleteSwipeHelper = object: SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                placeAdapter = rvPlacesList.adapter as PlaceAdapter
                placeAdapter.removePlace(viewHolder.adapterPosition)
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHelper)
        deleteItemTouchHelper.attachToRecyclerView(rvPlacesList)
    }

    override fun onItemClick(placeModel: PlaceModel) {
        val intent = Intent(this, PlaceDetailsActivity::class.java)
        intent.putExtra("PLACE_ID", placeModel.id.toString())
        startActivity(intent)
    }

    companion object {
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
    }
}