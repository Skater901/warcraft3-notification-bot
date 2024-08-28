package au.com.skater901.wc3connect.utils

import com.google.inject.Injector

internal inline fun <reified T> Injector.getInstance(): T = getInstance(T::class.java)