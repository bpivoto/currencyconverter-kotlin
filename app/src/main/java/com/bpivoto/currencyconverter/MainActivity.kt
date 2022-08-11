package com.bpivoto.currencyconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.bpivoto.currencyconverter.api.Endpoint
import com.bpivoto.currencyconverter.databinding.ActivityMainBinding
import com.bpivoto.currencyconverter.util.NetworkUtils
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var btnConvert: Button
    private lateinit var txtConversionResult: TextView
    private lateinit var inputFrom: EditText
    private val api_path = "https://cdn.jsdelivr.net/"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        spinnerFrom = binding.spinnerFrom
        spinnerTo = binding.spinnerTo
        btnConvert = binding.btnConvert
        txtConversionResult = binding.txtConversionResult
        inputFrom = binding.inputFrom

        getCurrencies()
        btnConvert.setOnClickListener { convertCurrency() }
    }

    fun convertCurrency() {
        val retrofitClient = NetworkUtils.getRetrofitInstance(api_path)
        val endpoint = retrofitClient.create(Endpoint::class.java)
        endpoint.getCurrencyRate(spinnerFrom.selectedItem.toString(), spinnerTo.selectedItem.toString()).enqueue(object:
            Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                var data = response.body()?.entrySet()?.find { it.key == spinnerTo.selectedItem.toString() }
                val rate: Double = data?.value.toString().toDouble()
                val conversion = inputFrom.text.toString().toDouble() * rate
                txtConversionResult.text = conversion.toString()

            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                println("convertCurrency() failed")
            }

        })
    }

    fun getCurrencies() {
        val retrofitClient = NetworkUtils.getRetrofitInstance(api_path)
        val endpoint = retrofitClient.create(Endpoint::class.java)
        endpoint.getCurrencies().enqueue(object: Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                var data = mutableListOf<String>()
                response.body()?.keySet()?.iterator()?.forEach {
                    data.add(it)
                }
                println(data.count())

                val posBRL = data.indexOf("brl")
                val posUSD = data.indexOf("usd")

                val adapter = ArrayAdapter(baseContext, android.R.layout.simple_spinner_dropdown_item, data)
                spinnerFrom.adapter = adapter
                spinnerTo.adapter = adapter

                spinnerFrom.setSelection(posBRL)
                spinnerTo.setSelection(posUSD)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                println("getCurrencies() failed")
            }

        })
    }
}