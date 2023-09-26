package com.seotepa.infobotApp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.robotemi.sdk.Robot
import com.robotemi.sdk.map.MapDataModel
import com.robotemi.sdk.navigation.model.Position
import com.seotepa.infobotApp.databinding.ActivityMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt


class MapActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_FILE_PICKER = 1

        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"
    }

    @Volatile
    private var bitmap: Bitmap? = null

    @Volatile
    private var mapDataModel: MapDataModel? = null

    private val singleThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var bindingMap: ActivityMapBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMap = ActivityMapBinding.inflate(layoutInflater)
        setContentView(bindingMap.root)
        bindingMap.ibBack.setOnClickListener { finish() }
        bindingMap.textViewMapElements.setOnClickListener { refreshMap() }
        bindingMap.textViewMapElements.movementMethod = ScrollingMovementMethod()

        bindingMap.buttonBackupMap.setOnClickListener {
            // This code block will take current map from temi and create a backup file as ParcelFileDescriptor
            // Write the file to a desired location to finish the backup
            val withoutUI = bindingMap.checkBoxLoadMapWithoutUI.isChecked
            val parcelFileDescriptor =
                Robot.getInstance().getCurrentMapBackupFile(withoutUI) ?: return@setOnClickListener
            lifecycleScope.launch(Dispatchers.IO) {
                val dir = File(applicationContext.getExternalFilesDir(null), "maps")
                if (!dir.exists()) {
                    dir.mkdir()
                }

                val file = File(dir, "map-${System.currentTimeMillis()}.tar.gz")
                file.createNewFile()
                val inputStream = ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor)
                Log.d("Map-SDK", "Loading map 1")

                inputStream.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                if (file.length() > 0) {
                    Log.d("Map-SDK", "Loading map 2")

                    launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "File generated", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        bindingMap.buttonLoadMapFromPrivateFile.setOnClickListener {
            // This code block will load a map backup to temi.
            // The backup files are taken from either application's internal storage or external storage.
            // These files are securely store this way and transferred by content provider that only temi launcher can read.

            // The folder needs to be declared in res/xml/provider_paths.xml
            // <files-path name="map_internal_file" path="maps/" />
            val internalMapDirectory = File(filesDir, "maps")

            // The folder needs to be declared in res/xml/provider_paths.xml
            // <external-files-path name="map_external_file" path="maps/"/>
            val externalMapDirectory = File(getExternalFilesDir(null), "maps")

            Log.d("SDK-Path", "externalMapDirectory $externalMapDirectory")

            lifecycleScope.launch(Dispatchers.IO) {
                val internalFiles = internalMapDirectory.listFiles()?.toList() ?: listOf()
                val externalFiles = externalMapDirectory.listFiles()?.toList() ?: listOf()
                val files = (internalFiles + externalFiles).filter {
                    it.isFile && it.path.endsWith("tar.gz", true)
                }

                val builder = AlertDialog.Builder(this@MapActivity)

                if (files.isNotEmpty()) {
                    builder.setItems(files.map { it.path }.toTypedArray()) { _, which ->
                        val fileSelected = files[which]
                        Log.d("SDK-Sample", "Map file selected ${fileSelected.path}")
                        val uri =
                            FileProvider.getUriForFile(this@MapActivity, AUTHORITY, fileSelected)
                        loadMap(uri)

                        Log.d("SDK-Sample", "Map file loaded")

                    }.setTitle("Select one map file to load")
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                } else {
                    builder.setTitle("No map backup files found")
                        .setMessage("This sample takes map files from\n/sdcard/Android/data/com.seotepa.infobotApp/files/maps/\nand /data/data/com.robotemi.sdk.sample/files/maps/")
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                }

                launch(Dispatchers.Main) {
                    builder.show()
                }
            }
        }

        bindingMap.buttonLoadMapFromFileSelector.setOnClickListener {
            // This code block is launching a file picker to select a public accessible backup file.
            // So if you app is loaded in the USB drive on V3 robot, this could be an easy way to load it.

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            val mimeTypes = arrayOf("application/gzip", "application/zip")
            intent.setType("*/*")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(intent, REQUEST_FILE_PICKER)
        }

        bindingMap.buttonLoadMapFromPublicFile.setOnClickListener {
            // This is possible but not recommended.
            // As Android doesn't recommend to use file:// scheme to send files.
            val file = File("/sdcard/map-1690428181150.tar.gz")
            if (file.exists()) {
                loadMap(Uri.fromFile(file))
            } else {
                Toast.makeText(this, "Please place a map file at public storage", Toast.LENGTH_SHORT).show()
            }
        }

        refreshMap()
    }

    private fun refreshMap() {
        bindingMap.progressBar.visibility = View.VISIBLE
        singleThreadExecutor.execute {
            mapDataModel = Robot.getInstance().getMapData() ?: return@execute
            val mapImage = mapDataModel!!.mapImage
            Log.i("Map-mapImage", mapDataModel!!.mapImage.typeId)

            bitmap = Bitmap.createBitmap(
                mapImage.data.map { Color.argb((it * 2.55).roundToInt(), 0, 0, 0) }.toIntArray(),
                mapImage.cols,
                mapImage.rows,
                Bitmap.Config.ARGB_8888
            )
            runOnUiThread {
                bindingMap.progressBar.visibility = View.GONE
                bindingMap.buttonBackupMap.visibility = View.VISIBLE
                bindingMap.buttonLoadMapFromPrivateFile.visibility = View.VISIBLE
                bindingMap.buttonLoadMapFromFileSelector.visibility = View.VISIBLE
                bindingMap.textViewMapElements.text = ""
                Log.i("Map-mapId", mapDataModel!!.mapId)
                bindingMap.textViewMapElements.append("[map_id]: ${mapDataModel!!.mapId} \n")
                Log.i("Map-mapInfo", mapDataModel!!.mapInfo.toString())
                bindingMap.textViewMapElements.append("[map_info]: ${mapDataModel!!.mapInfo} \n")
                Log.i("Map-greenPaths", mapDataModel!!.greenPaths.toString())
                bindingMap.textViewMapElements.append("[map_green_path]: ${mapDataModel!!.greenPaths} \n")
                Log.i("Map-virtualWalls", mapDataModel!!.virtualWalls.toString())
                bindingMap.textViewMapElements.append("[map_virtual_walls]: ${mapDataModel!!.virtualWalls} \n")
                Log.i("Map-locations", mapDataModel!!.locations.toString())
                bindingMap.textViewMapElements.append("[map_locations]: ${mapDataModel!!.locations} \n")
                bindingMap.textViewMapElements.append("[map_name]: ${mapDataModel!!.mapName} \n")
                bindingMap.imageViewMap.setImageBitmap(bitmap)
            }
        }
    }

    override fun onDestroy() {
        bitmap?.recycle()
        singleThreadExecutor.shutdownNow()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FILE_PICKER && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedFileUri = data.data
                Log.d("SDK-Sample", "Map file selected from file selector, uri $selectedFileUri")

                if (selectedFileUri != null) {
                    loadMap(selectedFileUri)
                    Log.d("SDK-Sample", "Map file loaded")
                    // It is safe to delete the file here if needed.
                }
            }
        }
    }

    /**
     * We can set a few arguments when loading map.
     * They function the same as [Robot.loadMap]
     */
    private fun loadMap(uri: Uri) {
        val reposeRequired = bindingMap.checkBoxLoadMapWithRepose.isChecked
        val withoutUI = bindingMap.checkBoxLoadMapWithoutUI.isChecked
        val position: Position? = if (bindingMap.checkBoxLoadMapFromPose.isChecked) {
            Position(1f, 1f, 1f)
        } else {
            null
        }
        Robot.getInstance().loadMapWithBackupFile(
            uri,
            reposeRequired = reposeRequired,
            withoutUI = withoutUI,
            position = position
        )
    }
}