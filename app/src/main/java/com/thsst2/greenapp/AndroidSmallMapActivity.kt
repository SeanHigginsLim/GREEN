package com.thsst2.greenapp
import android.os.Bundle
import com.bumptech.glide.Glide
import androidx.appcompat.app.AppCompatActivity

class AndroidSmallMapActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_android_small_map)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/4a89py0h_expires_30_days.png").into(findViewById(R.id.rundefined))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/6nk1t21m_expires_30_days.png").into(findViewById(R.id.rm6mh7dlmac))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/g6j8jjk1_expires_30_days.png").into(findViewById(R.id.rr4suqnw5xu8))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/6ylj9vbk_expires_30_days.png").into(findViewById(R.id.r4ygvniz63xa))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/ufno6m2e_expires_30_days.png").into(findViewById(R.id.resno30rwma6))
	}
}