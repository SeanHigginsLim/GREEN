package com.thsst2.greenapp
import android.os.Bundle
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.compose.setContent
import com.bumptech.glide.Glide
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text

class AndroidSmallHome : AppCompatActivity() {
	private var editTextValue1: String = ""
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_android_small_home)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/59e2tcay_expires_30_days.png").into(findViewById<ImageView>(R.id.rn6y677xm2op))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/g96kdk7e_expires_30_days.png").into(findViewById<ImageView>(R.id.r1niin00ofei))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/wklyakqn_expires_30_days.png").into(findViewById<ImageView>(R.id.ra0p1lhf2i3g))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/qz8xcg2m_expires_30_days.png").into(findViewById<ImageView>(R.id.rjvarnxwuapq))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/b60kjme5_expires_30_days.png").into(findViewById<ImageView>(R.id.rlqd4vorx07s))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/bh4hn9xl_expires_30_days.png").into(findViewById<ImageView>(R.id.rlxbeos9oum8))
		val editText1: EditText = findViewById(R.id.r0zz50xix97ik)
		editText1.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
				// before Text Changed
			}
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
				editTextValue1 = s.toString()  // on Text Changed
			}
			override fun afterTextChanged(s: Editable?) {
				// after Text Changed
			}
		})
		val button1: View = findViewById(R.id.rp0408m5ovlr)
		button1.setOnClickListener {
			println("Pressed")
		}
	}
}