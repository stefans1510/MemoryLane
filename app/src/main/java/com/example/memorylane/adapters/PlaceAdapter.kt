package com.example.memorylane.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memorylane.R
import com.example.memorylane.activities.AddPlaceActivity
import com.example.memorylane.models.PlaceModel
import com.example.memorylane.utils.SwipeToEditCallback
import com.example.memorylane.viewmodels.PlaceViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.coroutines.coroutineContext

class PlaceAdapter(
    private val context: Context,
    private var placesList: ArrayList<PlaceModel>,
    private val placeViewModel: PlaceViewModel,
    private val placeItemClickListener: OnPlaceItemClickListener
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent, false)
        return PlaceViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newPlacesList: ArrayList<PlaceModel>) {
        placesList = newPlacesList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val currentItem = placesList[position]
        holder.bind(currentItem)

        holder.itemView.setOnClickListener {
            placeItemClickListener.onItemClick(currentItem)
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddPlaceActivity::class.java)
        intent.putExtra("PLACE_ID", placesList[position].id.toString())
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removePlace(position: Int) {
        val placeToDelete = placeViewModel.deletePlace(placesList[position])

        if (placeToDelete > 0) {
            placesList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_place_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_place_description)
        private val civPlaceImage: CircleImageView = itemView.findViewById(R.id.civ_place_image)

        fun bind(placeModel: PlaceModel) {
            tvTitle.text = placeModel.title
            tvDescription.text = placeModel.description

            // load the image using Glide into the CircleImageView
            Glide.with(itemView.context)
                .load(placeModel.image) // assuming placeModel.imageUri is the Uri for the image
                .placeholder(R.mipmap.ic_launcher_round) // placeholder image while loading
                .error(R.mipmap.ic_launcher_round) // image to show in case of error
                .into(civPlaceImage)
        }
    }

    interface OnPlaceItemClickListener {
        fun onItemClick(placeModel: PlaceModel)
    }
}