package ru.cab404.ticketchecker.utils

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.experimental.android.HandlerContext

/**
 * Created on 3/27/18.
 * @author cab404
 */


@SuppressLint("ValidFragment")
open class BaseFragment(@LayoutRes val layout: Int = -1) : Fragment() {
    val HandlerC by lazy {  HandlerContext(Handler()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        HandlerC
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            if (layout == -1)
                super.onCreateView(inflater, container, savedInstanceState)
            else
                inflater.inflate(layout, container, false)


}

open class BaseActivity: AppCompatActivity() {
    val HandlerC by lazy {  HandlerContext(Handler()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        HandlerC
        super.onCreate(savedInstanceState)
    }

}

inline fun <reified T> T.v(text: Any?) {
    Log.v(T::class.java.simpleName ?: "null", text?.toString() ?: "<null message>")
}