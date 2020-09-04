package io.bleu.androidx.widget.samples

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
                valueTv.text = getString(R.string.ruler_value_with_unit).format(value)
                valueTv.setTextColor(rulerView.getIndicatorColor())
            }
        })
    }
}