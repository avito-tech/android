package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.TextInputElement
import com.avito.android.ui.R

class KeyboardScreen : PageObject() {
    val input: TextInputElement = element(withId(R.id.input))
}
