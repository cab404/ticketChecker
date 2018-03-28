package ru.cab404.ticketchecker.utils

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created on 3/27/18.
 * @author cab404
 */

@SuppressLint("ValidFragment")
open class BaseFragment(@LayoutRes val layout: Int = -1) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            if (layout == -1)
                super.onCreateView(inflater, container, savedInstanceState)
            else
                inflater.inflate(layout, container, false)


}


inline fun <reified T> T.v(text: Any?) {
    Log.v(T::class.java.simpleName ?: "null", text?.toString() ?: "<null message>")
}