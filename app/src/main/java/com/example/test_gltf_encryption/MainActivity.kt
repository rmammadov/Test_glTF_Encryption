package com.example.test_gltf_encryption

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private val FILE_NAME: String = "data.blob"
    private val CHARSET: Charset = Charsets.UTF_8

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
            binaryFromFileToJson(FILE_NAME)
        }
        btnEncode.setOnClickListener {
            jsonToFileAsBinary("Fox.gltf")
        }
        btnTest.setOnClickListener {
        }
    }

    /**
     * Present model
     */
    private fun showModel() {
        val sceneViewerIntent = Intent(Intent.ACTION_VIEW)
        val intentUri = Uri.parse("https://arvr.google.com/scene-viewer/1.0").buildUpon()
            .appendQueryParameter("file", "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Duck/glTF/Duck.gltf")
            .appendQueryParameter("mode", "ar_only")
            .appendQueryParameter("title", "Duck")
            .appendQueryParameter("resizable", "false")
            .build()
        sceneViewerIntent.setData(intentUri);
        sceneViewerIntent.setPackage("com.google.android.googlequicksearchbox")
        startActivity(sceneViewerIntent)
    }

    private fun jsonToFileAsBinary(nameOfAsset: String) {
        val jsonString = getJsonFromAsset(nameOfAsset)
        val byteArray = getBinaryFromString(jsonString, CHARSET)

        writeBytesToFile(FILE_NAME, byteArray)
    }

    private fun binaryFromFileToJson(nameOfFile: String) {
        val byteArray = readBytesFromFile(nameOfFile)
        val jsonString = getStringFromBinary(byteArray, CHARSET)

        val jsonObject = stringToJson(jsonString)

        tvDetails.text = jsonString
    }

    /**
     * Get Json from Asset
     */
    private fun getJsonFromAsset(nameOfAsset: String) : String {
        var jsonString: String? = ""

        try {
            val inputStream = assets.open(nameOfAsset)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            val charset: Charset = CHARSET
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, charset)
        }
        catch (ex: IOException) {
            ex.printStackTrace()
            return ""
        }

        return jsonString.toString()
    }

    /**
     * String to Json
     */
    private fun stringToJson(string: String) : JSONObject {
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
    private fun getStringFromBinary(byteArray: ByteArray, charset: Charset) : String {
        val charset = charset

        return byteArray.toString(charset)
    }

    /**
     * Write bytes to the file
     */
    private fun writeBytesToFile(name: String, byteArray: ByteArray) {
        val fileName = name

        var file = File(fileName)

        val isNewFileCreated :Boolean = file.createNewFile()

        if(isNewFileCreated){
            println("$fileName is created successfully.")
        } else{
            println("$fileName already exists.")
        }

        file.writeBytes(byteArray)
    }

    /**
     * Reading bytes from file
     */
    private fun readBytesFromFile(fileName: String) :ByteArray {
        var file = File(fileName)
        var fileExists = file.exists()

        if(fileExists){
            print("$fileName file does exist.")
        } else {
            print("$fileName file does not exist.")
        }

        return file.readBytes()
    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File = File(context.cacheDir, fileName)
        .also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    context.assets.open(fileName).use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
        }

//    private fun getJsonFromString() : JSONObject {
//
//    }
}