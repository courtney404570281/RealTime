package tw.com.zenii.realtime

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.statusBarColor = Color.rgb(253, 131, 105)
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(animation)

        val intent = Intent(this, MainActivity::class.java)
        GlobalScope.launch {
            sleep(1000)
            startActivity(intent)
            finish()
        }
    }
}
