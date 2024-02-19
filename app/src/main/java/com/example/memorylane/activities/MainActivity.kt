package com.example.memorylane.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorylane.adapters.PlaceAdapter
import com.example.memorylane.databinding.ActivityMainBinding
import com.example.memorylane.models.PlaceModel
import com.example.memorylane.utils.SwipeToDeleteCallback
import com.example.memorylane.utils.SwipeToEditCallback
import com.example.memorylane.viewmodels.PlaceViewModel

class MainActivity : AppCompatActivity(), PlaceAdapter.OnPlaceItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var addPlaceLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        placeAdapter = PlaceAdapter(this, ArrayList(), placeViewModel = PlaceViewModel(application), this)

        binding.rvPlacesList.layoutManager = LinearLayoutManager(this)
        binding.rvPlacesList.adapter = placeAdapter

        addPlaceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                displayPlaces()
            }
        }

        binding.ivProfile.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            startActivity(intent)
        }

        binding.fabAddPlace.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)
            addPlaceLauncher.launch(intent)
        }

        binding.btnHelp.setOnClickListener {
            Toast.makeText(
                this, "Swipe right on the place card to edit it, or swipe left to delete it", Toast.LENGTH_LONG
            ).show()
        }

        displayPlaces()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayPlaces() {
        val placeViewModel = PlaceViewModel(application)
        val placesList: ArrayList<PlaceModel> = placeViewModel.getPLacesList()

        Log.d("MainActivity", "Places List: $placesList")

        if (placesList.isNotEmpty()) {
            binding.rvPlacesList.visibility = View.VISIBLE
            binding.tvNoRecordsFound.visibility = View.GONE
            binding.btnHelp.visibility = View.VISIBLE
            placeAdapter.setData(placesList)
            placeAdapter.notifyDataSetChanged()
        } else {
            binding.rvPlacesList.visibility = View.GONE
            binding.tvNoRecordsFound.visibility = View.VISIBLE
            binding.btnHelp.visibility = View.GONE
        }

        val editSwipeHelper = object: SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val intent = Intent(binding.rvPlacesList.context, MainActivity::class.java)
                addPlaceLauncher.launch(intent)
                placeAdapter = binding.rvPlacesList.adapter as PlaceAdapter
                placeAdapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHelper)
        editItemTouchHelper.attachToRecyclerView(binding.rvPlacesList)

        val deleteSwipeHelper = object: SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                placeAdapter = binding.rvPlacesList.adapter as PlaceAdapter
                placeAdapter.removePlace(viewHolder.adapterPosition)

                displayPlaces()
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHelper)
        deleteItemTouchHelper.attachToRecyclerView(binding.rvPlacesList)
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
