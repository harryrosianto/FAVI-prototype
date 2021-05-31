package com.thelatenightstudio.favi.core.media

import android.content.Context
import android.util.Log
import com.thelatenightstudio.favi.core.utils.FileHelper.recordFile
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Model(private val context: Context) {

    companion object {
        const val MODEL_PATH = "favi_metadata.tflite"
        const val FILENAME = "favi_labels.txt"
    }

    private lateinit var tflite: Interpreter
    private val labels = ArrayList<String>()

    fun init1() {
//        val model = FaviMetadata.newInstance(context)
//        val applicationContext = context.applicationContext
//
//        // Creates inputs for reference.
//        val audioClip = TensorBuffer.createFixedSize(intArrayOf(1, 124, 129, 1), DataType.FLOAT32)
//        val inputStream: InputStream = FileInputStream(applicationContext.recordFile)
//        val byteArray: ByteArray = inputStream.readBytes()
//        val byteBuffer = ByteBuffer.wrap(byteArray)
//
//        Log.d("Cek", "$byteBuffer")
//
//        audioClip.loadBuffer(byteBuffer)
//
//        // Runs model inference and gets result.
//        val outputs = model.process(audioClip)
//        val probability = outputs.probabilityAsTensorBuffer
//
//        Log.d("Cek", "$probability")
//
//        // Releases model resources if no longer used.
//        model.close()
    }

    fun init2() {
        loadLabels()
        Log.d("Cek", "init2: ${labels.size}")
        val modelFile = loadModelFile()
        if (modelFile != null) {
            Log.d("Cek", "init2: model is not null")
            tflite = Interpreter(modelFile)
        }

        val applicationContext = context.applicationContext
        val inputStream: InputStream = FileInputStream(applicationContext.recordFile)
        val byteArray: ByteArray = inputStream.readBytes()
        val byteBuffer = ByteBuffer.wrap(byteArray)

        tflite.run(byteBuffer, labels)
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer? {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels() {
        val text = context.readTextFromAsset(FILENAME).trim()
        val lines = text.split(" ")
        labels.addAll(lines)
    }

    private fun Context.readTextFromAsset(fileName: String): String {
        return assets.open(fileName).bufferedReader().use {
            it.readText()
        }
    }

}