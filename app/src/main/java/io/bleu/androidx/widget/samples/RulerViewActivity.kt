package io.bleu.androidx.widget.samples

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import io.bleu.android.widget.samples.R
import io.bleu.androidx.widget.RulerView

class RulerViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruler_view)
        val valueTv = findViewById<TextView>(R.id.tv_value)
        val rulerView = findViewById<RulerView>(R.id.rv_ruler)
        rulerView.setOnValueChangedListener(object : RulerView.OnValueChangedListener {
            override fun onValueChanged(value: Int) {
                valueTv.text = value.toString()
                valueTv.setTextColor(rulerView.getIndicatorColor())
            }
        })
    }
}