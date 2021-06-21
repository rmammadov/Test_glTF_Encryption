package com.example.test_gltf_encryption

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {

    private val ASSET_NAME: String = "Fox.gltf"
    private val FILE_NAME: String = "data.blob"
    private val FILE_PATH = "TestTextEncryptionExternalStorage"

    private val CHARSET: Charset = Charsets.UTF_8

    private val isExternalStorageReadOnly: Boolean
        get() {
            val extStorageState = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)
        }
    private val isExternalStorageAvailable: Boolean
        get() {
            val extStorageState = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED.equals(extStorageState)
        }

    lateinit var tvDetails: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUi()
    }

    private fun setUi() {
        val btnDisplay = findViewById<Button>(R.id.btn_display)
        val btnDecode = findViewById<Button>(R.id.btn_decode)
        val btnEncode = findViewById<Button>(R.id.btn_encode)
        val btnTest = findViewById<Button>(R.id.btn_test)
        tvDetails = findViewById<TextView>(R.id.tv_details)

        btnDisplay.setOnClickListener {
            showModel()
        }
        btnDecode.setOnClickListener {
            dataFromFileToJson(FILE_NAME)
        }
        btnEncode.setOnClickListener {
            jsonToFile(ASSET_NAME)
        }
        btnTest.setOnClickListener {
        }

        checkPermission()
    }

    /**
     * Present model
     */
    private fun showModel() {
        val sceneViewerIntent = Intent(Intent.ACTION_VIEW)
        val intentUri = Uri.parse("https://arvr.google.com/scene-viewer/1.0").buildUpon()
            .appendQueryParameter(
                "file",
                "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Duck/glTF/Duck.gltf"
            )
            .appendQueryParameter("mode", "ar_only")
            .appendQueryParameter("title", "Duck")
            .appendQueryParameter("resizable", "false")
            .build()
        sceneViewerIntent.setData(intentUri);
        sceneViewerIntent.setPackage("com.google.android.googlequicksearchbox")
        startActivity(sceneViewerIntent)
    }

    private fun jsonToFile(nameOfAsset: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val jsonString = getJsonFromAsset(nameOfAsset)
            val dataEncoded = jsonString.encode()
            writeDataToFile(
                FILE_NAME,
                dataEncoded
            )
            tvDetails.text = dataEncoded
            Log.d("LOGIN", jsonString.encode())
        } else {
            checkPermission()
        }
    }

    private fun dataFromFileToJson(nameOfFile: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val dataEncoded = readDataFromFile(nameOfFile)
            if(!dataEncoded.isNullOrEmpty()) {
                val dataDecoded = dataEncoded.decode()
                val jsonObject = stringToJson(dataDecoded)
                tvDetails.text = dataDecoded
            }
        } else {
            checkPermission()
        }
    }

    /**
     * Get Json from Asset
     */
    private fun getJsonFromAsset(nameOfAsset: String): String {
        var jsonString: String? = ""

        try {
            val inputStream = assets.open(nameOfAsset)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            val charset: Charset = CHARSET
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, charset)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ""
        }

        return jsonString.toString()
    }

    private fun String.decode(): String {
        return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
    }

    private fun String.encode(): String {
        return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.DEFAULT)
    }

    /**
     * String to Json
     */
    private fun stringToJson(string: String): JSONObject {
        return JSONObject(string)
    }

    /**
     * Get Binary from string
     */
    private fun getBinaryFromString(string: String, charset: Charset): ByteArray {
        val charset = charset

        return string.toByteArray(charset)
    }

    /**
     * Get string from Binary
     */
    private fun getStringFromBinary(byteArray: ByteArray, charset: Charset): String {
        val charset = charset

        return byteArray.toString(charset)
    }

    /**
     * Write data to the file
     */
    private fun writeDataToFile(fileName: String, data: String) {
        savePrivately(fileName, data)
    }

    /**
     * Reading data from file
     */
    private fun readDataFromFile(fileName: String): String? {
        return showPrivateData(fileName)
    }

    private fun checkPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                }
            }).check()
    }

    private fun savePrivately(fileName: String, data: String) {
        // Creating folder with name #FILE_PATH
        val folder = getExternalFilesDir(FILE_PATH)

        // Creating file with name FILE_NAME
        val file = File(folder, fileName)
        writeTextData(file, data)
    }

    private fun writeTextData(file: File, data: String) {
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data.toByteArray())
            Toast.makeText(this, "Done" + file.absolutePath, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showPrivateData(fileName: String): String? {
        val folder = getExternalFilesDir(FILE_PATH)

        // file is saved privately
        val file = File(folder, fileName)

        return getData(file)
    }

    private fun getData(file: File): String? {
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream(file)
            var i = -1
            val buffer = StringBuffer()
            while (fileInputStream.read().also { i = it } != -1) {
                buffer.append(i.toChar())
            }
            return buffer.toString()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }
}