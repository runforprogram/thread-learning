package com.example.threadlearning

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.threadlearning.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonFirst.setOnClickListener {
            Thread({
                Thread.sleep(5000)
            }, "thisishhxxyynnuuiippasdfasdfasdfasdfasdfds").start()
        }
        binding.buttonSecond.setOnClickListener {
            Thread({
//                Thread.sleep(5000)
            }, "shortthreadname").start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}