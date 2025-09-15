package com.thsst2.greenapp
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class AndroidSmallLogin : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_android_small_login)
		val button1: View = findViewById(R.id.rumm9kmpqgsm)
		button1.setOnClickListener {
			println("Pressed")
		}
		val button2: View = findViewById(R.id.rrc8z2w6cq5l)
		button2.setOnClickListener {
			println("Pressed")
		}
		val button3: View = findViewById(R.id.r7k1xvbfvys)
		button3.setOnClickListener {
			println("Pressed")
		}
	}
}