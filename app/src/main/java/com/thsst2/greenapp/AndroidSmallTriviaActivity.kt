package com.thsst2.greenapp
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import androidx.appcompat.app.AppCompatActivity

class AndroidSmallTriviaActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_android_small_trivia)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/euck9yhs_expires_30_days.png").into(findViewById(R.id.rtpmb3sfkxec))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/2a2i3aud_expires_30_days.png").into(findViewById(R.id.rv2mkds8qk))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/5tyj8imq_expires_30_days.png").into(findViewById(R.id.rm8ia53ujvf))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/eirszlo0_expires_30_days.png").into(findViewById(R.id.rwt2433i01up))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/8g667ulq_expires_30_days.png").into(findViewById(R.id.rjr0kuafq29l))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/dfc89q7y_expires_30_days.png").into(findViewById(R.id.rjim834nbvr))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/w0zoa2pz_expires_30_days.png").into(findViewById(R.id.r8jqrksqg5si))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/mq69qsuu_expires_30_days.png").into(findViewById(R.id.rybdh3kflfnm))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/g70x0upd_expires_30_days.png").into(findViewById(R.id.rjuo5tbsq7m))
		val button1: View = findViewById(R.id.rkpvn05dvtpr)
		button1.setOnClickListener {
			println("Pressed")
		}
		val button2: View = findViewById(R.id.rz51uowvk0cr)
		button2.setOnClickListener {
			println("Pressed")
		}
	}
}