package com.thelatenightstudio.favi.core.media

import android.content.Context
import android.media.AudioRecord
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.os.HandlerCompat
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier

class ModelWithAudioRecord(private val context: Context) {

    companion object {
        private const val MODEL_FILE = "favi_metadata.tflite"
        private const val MINIMUM_DISPLAY_THRESHOLD: Float = 0.5f
        private const val CLASSIFICATION_INTERVAL =
            500L // how often should classification run in milli-secs
    }

    var onStart: (() -> Unit)? = null
    var onStop: (() -> Unit)? = null
    var onAmpListener: ((Int) -> Unit)? = null

    private var classifier: AudioClassifier? = null
    private var audioTensor: TensorAudio? = null
    private var recorder: AudioRecord? = null

    private var startTime: Long = 0

    private lateinit var handler: Handler // background thread handler to run classification

    var isRecording = false
        private set

    fun init(): ModelWithAudioRecord {
        // Initialize the audio classifier
        classifier = AudioClassifier.createFromFile(context, MODEL_FILE)
        audioTensor = classifier?.createInputTensorAudio()

        // Initialize the audio recorder
        recorder = classifier?.createAudioRecord()

        val handlerThread = HandlerThread("backgroundThread")
        handlerThread.start()
        handler = HandlerCompat.createAsync(handlerThread.looper)

        return this
    }

    fun toggleRecording() {
        isRecording = if (!isRecording) {
            startTime = System.currentTimeMillis()
            recorder?.startRecording()
            classificationProcess()
            onStart?.invoke()
            true
        } else {
            recorder?.stop()
            handler.removeCallbacksAndMessages(null)
            onStop?.invoke()
            false
        }
    }

    fun classificationProcess() {
        // Define the classification runnable
        val run = object : Runnable {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun run() {
                val startTime = System.currentTimeMillis()

                // Load the latest audio sample
                audioTensor?.load(recorder)
                val output = classifier?.classify(audioTensor)

                // Filter out results above a certain threshold, and sort them descendingly
                val filteredModelOutput = output?.get(0)?.categories?.filter {
                    it.score > MINIMUM_DISPLAY_THRESHOLD
                }?.sortedBy {
                    -it.score
                }

                val finishTime = System.currentTimeMillis()

                Log.d("MWAR", "Latency = ${finishTime - startTime}ms")
                Log.d("MWAR", "run: $filteredModelOutput")

                // Rerun the classification after a certain interval
                handler.postDelayed(this, CLASSIFICATION_INTERVAL)
            }
        }

        // Start the classification process
        handler.post(run)
    }

    fun getCurrentTime() = System.currentTimeMillis() - startTime

    fun release() {
        onStart = null
        onStop = null
//        recorder.onAmplitudeListener = null
        classifier = null
        audioTensor = null
        recorder = null
    }

}