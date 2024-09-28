package com.safe.resident.pro.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.safe.resident.pro.app.R
import com.safe.resident.pro.app.databinding.FragmentLiveBinding

class LiveFragment : Fragment() {

    private lateinit var binding: FragmentLiveBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_live, container, false)
        val view = binding.root


        return view
    }
}