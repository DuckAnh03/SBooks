package com.example.sbooks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sbooks.databinding.FragmentBookDetailBinding

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val ARG_TITLE = "title"
        const val ARG_AUTHOR = "author"
        const val ARG_PRICE = "price"

        fun newInstance(title: String, author: String, price: String) = BookDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_AUTHOR, author)
                putString(ARG_PRICE, price)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            binding.tvBookTitle.text = it.getString(ARG_TITLE)
            binding.tvBookAuthor.text = it.getString(ARG_AUTHOR)
            binding.tvBookPrice.text = it.getString(ARG_PRICE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
