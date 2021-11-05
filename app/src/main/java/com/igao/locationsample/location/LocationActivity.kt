package com.igao.locationsample.location

import android.os.Bundle
import com.igao.locationhelper.databinding.ActivityLocationBinding
import com.igao.locationsample.R

class LocationActivity : LocationHelperActivity() {

    private lateinit var binding: ActivityLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}