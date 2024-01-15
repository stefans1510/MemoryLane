package com.example.memorylane.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Geocoder
import android.location.LocationManager
import android.location.LocationRequest
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.memorylane.R
import com.example.memorylane.models.PlaceModel
import com.example.memorylane.viewmodels.PlaceViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var calendar = Calendar.getInstance()
    private lateinit var dateListener: DatePickerDialog.OnDateSetListener
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etLocation: EditText
    private lateinit var etDate: EditText
    private lateinit var ivPlaceImage: AppCompatImageView
    private lateinit var tvAddImageButton: TextView
    private lateinit var tvSelectCurrentLocation: TextView
    private lateinit var btnSave: Button
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var currentPhotoPath: String
    private var savedImage: Uri? = null
    private lateinit var placeActivityResultLauncher: ActivityResultLauncher<Intent>
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var placeViewModel: PlaceViewModel
    private var placeId: Int = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)

        val toolbar: Toolbar = findViewById(R.id.toolbar_add_place)
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        placeViewModel = PlaceViewModel(application)

        if (!Places.isInitialized()) {
            Places.initialize(
                this@AddPlaceActivity, resources.getString(R.string.google_maps_api_key)
            )
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImage: Uri? = result.data!!.data
                try {
                    // Set the selected image to the ImageView using ImageDecoder
                    val source = ImageDecoder.createSource(contentResolver, selectedImage!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    saveImageToInternalStorage(bitmap)
                    savedImage = saveImageToInternalStorage(bitmap)
                    findViewById<ImageView>(R.id.iv_place_image).setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                // Photo taken successfully, update the ImageView
                val photoFile = File(currentPhotoPath)
                val photoUri = Uri.fromFile(photoFile)
                try {
                    val source = ImageDecoder.createSource(contentResolver, photoUri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    saveImageToInternalStorage(bitmap)
                    savedImage = saveImageToInternalStorage(bitmap)
                    findViewById<ImageView>(R.id.iv_place_image).setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "Failed to take photo", Toast.LENGTH_SHORT).show()
            }
        }

        placeActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        etLocation.setText(place.name)
                        // Handle the selected place data as needed (e.g., use it for your PlaceModel)
                        // place.id, place.name, place.address, etc.
                        latitude = place.latLng?.latitude ?: 0.0
                        longitude = place.latLng?.longitude ?: 0.0
                    }
                } /*else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {

                }
                val status = Autocomplete.getStatusFromIntent(data!!)
                // Handle the error
                } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation
                } */
            }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        dateListener = DatePickerDialog.OnDateSetListener {
                view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        etTitle = findViewById(R.id.et_title)
        etDescription = findViewById(R.id.et_description)
        etLocation = findViewById(R.id.et_location)
        etLocation.setOnClickListener(this)
        etDate = findViewById(R.id.et_date)
        etDate.setOnClickListener(this)
        ivPlaceImage = findViewById(R.id.iv_place_image)
        tvAddImageButton = findViewById(R.id.tv_add_image)
        tvAddImageButton.setOnClickListener(this)
        tvSelectCurrentLocation = findViewById(R.id.tv_select_current_location)
        tvSelectCurrentLocation.setOnClickListener(this)
        btnSave = findViewById(R.id.btn_save)
        btnSave.setOnClickListener(this)

        if (intent.hasExtra("PLACE_ID")) {
            placeId = intent.getStringExtra("PLACE_ID")!!.toInt()
        }

        if (placeId > 0) {
            supportActionBar?.title = "Edit Place"

            val placeModel = placeViewModel.getPlaceById(placeId)

            etTitle.setText(placeModel!!.title)
            etDescription.setText(placeModel.description)
            etDate.setText(placeModel.date)
            savedImage = Uri.parse(placeModel.image)
            ivPlaceImage.setImageURI(savedImage)
            etLocation.setText(placeModel.location)

            btnSave.text = "UPDATE PLACE"
        }
        
        updateDateInView() // auto populate field with current date
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(this@AddPlaceActivity,
                    R.style.CustomDatePickerTheme,
                    dateListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_add_image -> {
                val imageDialog = AlertDialog.Builder(this)
                imageDialog.setTitle("Select Action")
                val imageDialogItems = arrayOf("Select photo from Gallery","Take a photo")
                imageDialog.setItems(imageDialogItems) {
                    dialogOption, which ->
                    when(which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }.show()
            }
            R.id.et_location -> {
                try {
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this@AddPlaceActivity)
                    placeActivityResultLauncher.launch(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.tv_select_current_location -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this, "Your location provider is turned off. Please turn it on",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    // Request location permission if not granted
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission already granted, get current location
                        getCurrentLocation()
                    } else {
                        // Request location permission
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ),
                            LOCATION_PERMISSION_CODE
                        )
                    }
                }
            }
            R.id.btn_save -> {
                when {
                    etTitle.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this, "Please enter the title", Toast.LENGTH_SHORT
                        ).show()
                    }
                    etDescription.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this, "Please enter description", Toast.LENGTH_SHORT
                        ).show()
                    }
                   etLocation.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this, "Please enter location", Toast.LENGTH_SHORT
                        ).show()
                    }
                    savedImage == null -> {
                        Toast.makeText(
                            this, "Please select an image", Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        val placeModel = PlaceModel(
                            if (placeId > 0) placeId else 0,
                            etTitle.text.toString(),
                            savedImage.toString(),
                            etDescription.text.toString(),
                            etDate.text.toString(),
                            etLocation.text.toString(),
                            latitude,
                            longitude
                        )

                        if (placeId > 0) {
                            val updatePlace = placeViewModel.updatePlace(placeModel)

                            if (updatePlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            val addPlace = placeViewModel.addPlace(placeModel)

                            if (addPlace > 0) {
                                setResult(Activity.RESULT_OK) // 2. handle the result
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, launch camera
                takePhotoFromCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, launch camera
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.memorylane.fileprovider",
                    it
                )

                cameraLauncher.launch(photoURI)
            }
        } else {
            // request the camera permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        galleryIntent.type = "image/*" // specify the MIME type to restrict to certain types of files
        galleryLauncher.launch(galleryIntent)
    }

    private fun updateDateInView() {
        val format = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        etDate.setText(sdf.format(calendar.time))
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return  Uri.parse(file.absolutePath)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude

                    // use a Geocoder to get a human-readable address
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                    if (addresses!!.isNotEmpty()) {
                        val address = addresses[0]
                        etLocation.setText(address.getAddressLine(0))
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this, "Failed to get current location. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    companion object {
        private const val IMAGE_DIRECTORY = "MemoryLaneImages"
        private const val CAMERA_PERMISSION_CODE = 101
        private const val LOCATION_PERMISSION_CODE = 1001
    }
}