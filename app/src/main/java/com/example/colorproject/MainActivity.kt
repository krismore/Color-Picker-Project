package com.example.colorproject

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var redSeekBar: SeekBar
    private lateinit var greenSeekBar: SeekBar
    private lateinit var blueSeekBar: SeekBar

    private lateinit var redInput: EditText
    private lateinit var greenInput: EditText
    private lateinit var blueInput: EditText

    private lateinit var redSwitch: Switch
    private lateinit var greenSwitch: Switch
    private lateinit var blueSwitch: Switch

    private lateinit var resetButton: Button
    private lateinit var colorDisplay: View

    private lateinit var viewModel: ColorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dataStore = ColorDataStore(applicationContext)
        viewModel = ColorViewModelFactory(dataStore).create(ColorViewModel::class.java)

        colorDisplay = findViewById(R.id.colorDisplay)

        redSeekBar = findViewById(R.id.redSeekBar)
        greenSeekBar = findViewById(R.id.greenSeekBar)
        blueSeekBar = findViewById(R.id.blueSeekBar)

        redInput = findViewById(R.id.redInput)
        greenInput = findViewById(R.id.greenInput)
        blueInput = findViewById(R.id.blueInput)

        redSwitch = findViewById(R.id.redSwitch)
        greenSwitch = findViewById(R.id.greenSwitch)
        blueSwitch = findViewById(R.id.blueSwitch)

        resetButton = findViewById(R.id.resetButton)

        setupComponent(redSeekBar, redInput, redSwitch, "red")
        setupComponent(greenSeekBar, greenInput, greenSwitch, "green")
        setupComponent(blueSeekBar, blueInput, blueSwitch, "blue")

        resetButton.setOnClickListener {
            viewModel.resetColors()
        }
    }

    private fun setupComponent(
        seekBar: SeekBar,
        input: EditText,
        toggle: Switch,
        component: String
    ) {
        var skipSeekUpdate = false
        var skipTextUpdate = false

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && !skipSeekUpdate) {
                    val value = progress / 1000f
                    viewModel.setColorComponent(component, value)
                    viewModel.saveState()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (skipTextUpdate) return

                val text = s.toString()
                val value = text.toFloatOrNull()

                if (value != null && value in 0f..1f) {
                    viewModel.setColorComponent(component, value)
                    viewModel.saveState()
                } else if (text.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "Enter value between 0 and 1", Toast.LENGTH_SHORT).show()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleComponent(component, isChecked)
            viewModel.saveState()
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                val (value, enabled) = when (component) {
                    "red" -> state.red to state.redEnabled
                    "green" -> state.green to state.greenEnabled
                    "blue" -> state.blue to state.blueEnabled
                    else -> 0f to false
                }

                skipSeekUpdate = true
                skipTextUpdate = true

                if (!enabled) {
                    seekBar.isEnabled = false
                    input.isEnabled = false
                    seekBar.progress = 0
                    input.setText("0.0")
                } else {
                    seekBar.isEnabled = true
                    input.isEnabled = true
                    seekBar.progress = (value * 1000).toInt()
                    if (input.text.toString() != String.format("%.3f", value)) {
                        input.setText(String.format("%.3f", value))
                    }
                }

                toggle.isChecked = enabled

                skipSeekUpdate = false
                skipTextUpdate = false

                // Update display color
                val r = if (state.redEnabled) state.red else 0f
                val g = if (state.greenEnabled) state.green else 0f
                val b = if (state.blueEnabled) state.blue else 0f
                colorDisplay.setBackgroundColor(Color.rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt()))
            }
        }
    }
}

class ColorViewModelFactory(private val dataStore: ColorDataStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ColorViewModel(androidx.lifecycle.SavedStateHandle(), dataStore) as T
    }
}
