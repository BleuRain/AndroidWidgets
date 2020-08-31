package io.bleu.androidx.widget.samples

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import io.bleu.android.widget.samples.R
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun <T : Activity> showPage(clazz: Class<T>) {
        try {
            startActivity(Intent(this, clazz))
        } catch (e: Exception) {
            Log.e(TAG, "exception $e")
        }
    }

    fun showRulerViewPage(view: View) {
        Log.i(TAG, "$view clicked")
        showPage(RulerViewActivity::class.java)
    }
}