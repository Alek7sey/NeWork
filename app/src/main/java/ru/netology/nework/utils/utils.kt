package ru.netology.nework.utils

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

fun convertServerDateTimeToLocalDateTime (serverDateTime : String?): String? {
    return if (serverDateTime == null) {
        ""
    } else {
        val serverDateFormat = DateTimeFormatter.ISO_INSTANT
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        val localDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val localDateTime = serverDateFormat.parse(serverDateTime)

        localDateFormat.format(localDateTime)
    }
}

fun convertLocalDateToServerDate (localDate : String?): String? {
    return if (localDate == null) {
        ""
    } else {
        val serverDateFormat = DateTimeFormatter.ISO_DATE
        val localDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val serverDate = localDateFormat.parse(localDate.trim())

        serverDateFormat.format(serverDate)
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