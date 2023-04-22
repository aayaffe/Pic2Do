package `in`.avimarine.pic2do

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import `in`.avimarine.androidutils.TAG
import `in`.avimarine.pic2do.databinding.ActivityMainBinding
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploadToFirebase(uri)
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT)
                .show()
        }
    }
    private val storageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
//            addImage("")
            takeImage()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun addImage(url: String) {
        getContent.launch("image/*")
    }

    private fun uploadToFirestore(uri: Uri, caption:String) {
        val db = Firebase.firestore
        val image = GridImage("caption", uri.toString())
        db.collection("todo")
            .add(image)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun uploadToFirebase(uri: Uri) {
        val imageReference: StorageReference = storageReference.child(
            System.currentTimeMillis().toString() + "." + getFileExtension(uri)
        )
        imageReference.putFile(uri).addOnSuccessListener {
            imageReference.downloadUrl.addOnSuccessListener { uri ->
                uploadToFirestore(uri, "")
                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
            }
        }.addOnProgressListener { (bytesTransferred, totalByteCount) ->
            val progress = (100.0 * bytesTransferred) / totalByteCount
            Log.d(TAG, "Upload is $progress% done")
        }.addOnPausedListener {
            Log.d(TAG, "Upload is paused")
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileExtension(fileUri: Uri): String? {
        val contentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri))
    }


    private val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                uploadToFirebase(uri)
            }
        }
    }

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadToFirebase(uri) }
    }

    private var latestTmpUri: Uri? = null



    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }
}