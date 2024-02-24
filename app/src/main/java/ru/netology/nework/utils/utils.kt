package ru.netology.nework.utils

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.floor
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun clicksCount(count: Int): String = when (count) {
    in 0..999 -> count.toString()
    in 1000..1099 -> "1K"
    in 1100..9999 -> (floor((count / 100).toDouble()) / 10).toString() + "K"
    in 10_000..999_999 -> (count / 1000).toString() + "K"
    else -> (floor((count / 100000).toDouble()) / 10).toString() + "M"
}

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

object SeparateIdPostArg : ReadWriteProperty<Bundle, Long> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): Long =
        thisRef.getLong(property.name)

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Long) {
        thisRef.putLong(property.name, value)
    }
}

object EditTextArg : ReadWriteProperty<Bundle, String?> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): String? =
        thisRef.getString(property.name)

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: String?) {
        thisRef.putString(property.name, value)
    }
}

object EventIdArg : ReadWriteProperty<Bundle, Long> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): Long =
        thisRef.getLong(property.name)

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Long) {
        thisRef.putLong(property.name, value)
    }
}

fun convertServerDateToLocalDate (serverDate : String): String? {
    if (serverDate == null) {
        return ""
    } else {
        val serverDateFormat = DateTimeFormatter.ISO_INSTANT
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());
        val localDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val localDateTime = serverDateFormat.parse(serverDate)

        return localDateFormat.format(localDateTime)
    }
}

object UserIdArg : ReadWriteProperty<Bundle, Long> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): Long =
        thisRef.getLong(property.name)

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Long) {
        thisRef.putLong(property.name, value)
    }
}

object PostIdArg : ReadWriteProperty<Bundle, Long> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): Long =
        thisRef.getLong(property.name)

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Long) {
        thisRef.putLong(property.name, value)
    }
}