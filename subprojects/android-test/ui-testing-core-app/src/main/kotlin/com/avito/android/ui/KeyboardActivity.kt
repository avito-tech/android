package com.avito.android.ui

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class KeyboardActivity : AppCompatActivity() {

    private val imm: InputMethodManager by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private val input: EditText by lazy {
        findViewById(R.id.input)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyboard)
    }

    fun hideKeyboard() {
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        input.clearFocus()
    }

    fun openKeyboard() {
        input.requestFocus()
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
    }
}
