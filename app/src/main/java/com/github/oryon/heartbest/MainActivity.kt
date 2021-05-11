package com.github.oryon.heartbest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.oryon.heartbest.databinding.ActivityMainBinding
import com.github.oryon.heartbest.ui.face.FaceActivity
import com.github.oryon.heartbest.ui.finger.FingerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonStartFace.setOnClickListener {
            val intent = Intent(this, FaceActivity::class.java)
            startActivity(intent)
        }

        binding.buttonStartFinger.setOnClickListener {
            val intent = Intent(this, FingerActivity::class.java)
            startActivity(intent)
        }
    }
}