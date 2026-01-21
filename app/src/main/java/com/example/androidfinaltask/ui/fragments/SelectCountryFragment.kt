package com.example.androidfinaltask.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidfinaltask.databinding.FragmentSelectCountryBinding
import com.example.androidfinaltask.ui.adapter.Country
import com.example.androidfinaltask.ui.adapter.CountryAdapter

class SelectCountryFragment : Fragment() {
    private var _binding: FragmentSelectCountryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var countryAdapter: CountryAdapter
    private var selectedCountry: Country? = null
    
    var onNextClick: ((Country) -> Unit)? = null

    private val countries = listOf(
        Country("Afghanistan", "🇦🇫"),
        Country("Albania", "🇦🇱"),
        Country("Algeria", "🇩🇿"),
        Country("Andorra", "🇦🇩"),
        Country("Angola", "🇦🇴"),
        Country("Argentina", "🇦🇷"),
        Country("Armenia", "🇦🇲"),
        Country("Australia", "🇦🇺"),
        Country("Austria", "🇦🇹"),
        Country("Azerbaijan", "🇦🇿"),
        Country("India", "🇮🇳"),
        Country("Indonesia", "🇮🇩"),
        Country("Iran", "🇮🇷"),
        Country("Iraq", "🇮🇶"),
        Country("Ireland", "🇮🇪"),
        Country("United States", "🇺🇸"),
        Country("United Kingdom", "🇬🇧")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        countryAdapter = CountryAdapter { country ->
            selectedCountry = country
            binding.btnNext.isEnabled = true
        }

        binding.rvCountries.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = countryAdapter
        }

        countryAdapter.submitList(countries)

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterCountries(s.toString())
            }
        })

        binding.btnNext.setOnClickListener {
            selectedCountry?.let {
                onNextClick?.invoke(it)
            }
        }
    }

    private fun filterCountries(query: String) {
        val filtered = if (query.isBlank()) {
            countries
        } else {
            countries.filter { it.name.contains(query, ignoreCase = true) }
        }
        countryAdapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


