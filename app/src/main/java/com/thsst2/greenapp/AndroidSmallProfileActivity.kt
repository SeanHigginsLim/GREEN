package com.thsst2.greenapp
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import androidx.appcompat.app.AppCompatActivity

class AndroidSmallProfileActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_android_small_profile)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/0228mxps_expires_30_days.png").into(findViewById(R.id.rorhz9ugpp3))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/ucnrpwvy_expires_30_days.png").into(findViewById(R.id.r92m9lwykfag))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/5uqcjups_expires_30_days.png").into(findViewById(R.id.r1sboj981btw))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/imp958ws_expires_30_days.png").into(findViewById(R.id.r82fycu2d3ik))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/erdezrmi_expires_30_days.png").into(findViewById(R.id.r6k6upscihau))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/gp023gk0_expires_30_days.png").into(findViewById(R.id.r5i26bda9wnh))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/f081lhs7_expires_30_days.png").into(findViewById(R.id.rr6cbpqtve3))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/3tidlk1m_expires_30_days.png").into(findViewById(R.id.rxvihqambbch))
		val button1: View = findViewById(R.id.rxhutp6h6x5j)
		button1.setOnClickListener {
			println("Pressed")
		}
		val button2: View = findViewById(R.id.rqts8trhvc1o)
		button2.setOnClickListener {
			println("Pressed")
		}
	}
}