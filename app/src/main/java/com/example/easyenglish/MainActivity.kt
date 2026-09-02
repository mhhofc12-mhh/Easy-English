package com.example.easyenglish

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.*
import java.util.*

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var target: TextView
    private lateinit var result: TextView

    private val sentences = listOf(
        "How are you today?",
        "I am learning English.",
        "What is your name?",
        "Where are you going?",
        "I want to speak English.",
        "Please help me.",
        "I like to read books.",
        "What are you doing?",
        "See you tomorrow.",
        "Have a nice day."
    )
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32,32,32,32)
        }
        val title = TextView(this).apply {
            text="Easy English"
            textSize=28f
        }
        target = TextView(this).apply { textSize=24f; setPadding(0,40,0,20) }
        result = TextView(this).apply { textSize=18f; setPadding(0,20,0,20) }

        val listen = Button(this).apply { text="🔊 Listen" }
        val speak = Button(this).apply { text="🎤 Speak & Check" }
        val next = Button(this).apply { text="➡ Next Sentence" }

        listen.setOnClickListener { speakTarget() }
        speak.setOnClickListener { startSpeechRecognition() }
        next.setOnClickListener { index=(index+1)%sentences.size; updateSentence() }

        layout.addView(title); layout.addView(target); layout.addView(listen)
        layout.addView(speak); layout.addView(next); layout.addView(result)
        setContentView(layout)
        updateSentence()
    }

    private fun updateSentence() {
        target.text=sentences[index]
        result.text="বলুন এবং আপনার pronunciation practice করুন।"
    }

    private fun speakTarget() {
        tts.speak(sentences[index], TextToSpeech.QUEUE_FLUSH, null, "target")
    }

    private fun startSpeechRecognition() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 10); return
        }
        val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the sentence")
        }
        startActivityForResult(intent, 20)
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==20 && resultCode==RESULT_OK) {
            val spoken=data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            val score=similarity(sentences[index], spoken)
            result.text="You said: $spoken\n\nPronunciation practice score: $score%\n" +
                    if(score>=85) "🎉 Excellent!" else if(score>=65) "👍 Good, try once more." else "🔁 Practice again."
        }
    }

    private fun similarity(a:String,b:String):Int {
        val x=a.lowercase(Locale.US).replace(Regex("[^a-z ]"),"").trim()
        val y=b.lowercase(Locale.US).replace(Regex("[^a-z ]"),"").trim()
        if(x==y) return 100
        val xa=x.split(" ").filter{it.isNotBlank()}; val ya=y.split(" ").filter{it.isNotBlank()}
        if(xa.isEmpty()) return 0
        val common=xa.count{it in ya}
        return ((common.toDouble()/xa.size)*100).toInt().coerceIn(0,100)
    }

    override fun onInit(status:Int) {
        if(status==TextToSpeech.SUCCESS) tts.language=Locale.US
    }
    override fun onDestroy() { tts.shutdown(); super.onDestroy() }
}
