package com.focusone.super_kitchen

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.focusone.super_kitchen.databinding.ActivityMainBinding
import com.focusone.super_kitchen.util.Const

class MainActivity : AppCompatActivity() {

    private val main_url = Const.MAIN_URL
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        initView()
    }

    private fun initView() {
        TODO("Not yet implemented")
    }
}