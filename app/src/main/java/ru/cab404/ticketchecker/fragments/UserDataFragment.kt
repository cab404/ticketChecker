package ru.cab404.ticketchecker.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_userdata.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.HandlerContext
import kotlinx.coroutines.experimental.async
import org.json.JSONArray
import org.json.JSONObject
import ru.cab404.ticketchecker.BuildConfig
import ru.cab404.ticketchecker.R
import ru.cab404.ticketchecker.utils.BaseFragment
import ru.cab404.ticketchecker.utils.v
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created on 3/27/18.
 * @author cab404
 */


class UserDataFragment : BaseFragment(R.layout.fragment_userdata) {

    private val requestThread = Executors.newSingleThreadExecutor()

    suspend fun getTicketData(id: String): JSONObject = suspendCoroutine { coro ->
        requestThread.execute {

            val url = "https://event.mirfandoma.ru/getorderdata?ticket_num=$id&secret_key=${BuildConfig.USERDATA_TOKEN}"
            try {
                v("request start")
                val con = URL(url).openConnection() as HttpsURLConnection
                v("connection...")
                con.connect()
                v("connected")
                val data = JSONObject(con.inputStream.reader().readText())
                v(data)
                coro.resume(data)
            } catch (e: Exception) {
                coro.resumeWithException(e)
            }

        }

    }


    fun path(from: Any, vararg elements: Any, path: List<Any>? = null): Any? {
        val path = path ?: elements.toList()
        if (path.isEmpty()) return from

        if (from is JSONObject) {
            return path(from[path[0] as String], path = path.drop(1))
        }

        if (from is JSONArray) {
            return path(from[path[0] as Int], path = path.drop(1))
        }

        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vProgress.visibility = VISIBLE
        vUserdata.visibility = GONE

        async(HandlerContext(Handler())) {

            try {

                val data = getTicketData(arguments!!.getString("ticketId"))

                v("out of coro!")

                if (data.has("error"))
                    throw RuntimeException(path(data, "error_description").toString())

                val name = path(data, "user", "fullname").toString()
                val email = path(data, "user", "email").toString()
                val phone = path(data, "user", "phone").toString()
                val ticketCost = path(data, "products", 0, "price").toString().toFloat().toInt()
                val ticketType = path(data, "products", 0, "pagetitle").toString()

                fun TextView.setTextOrInv(text: String?) {
                    visibility = if (text.isNullOrEmpty())
                        GONE
                    else
                        VISIBLE
                    this.text = text
                }

                vName.setTextOrInv(name)
                vPhone.setTextOrInv(phone)
                vEmail.setTextOrInv(email)
                vCost.setTextOrInv("$ticketCost р.")
                vType.setTextOrInv(ticketType)

                vProgress.visibility = GONE
                vUserdata.visibility = VISIBLE

            } catch (e: Exception) {
                Log.w("UserChecker", e)

            }


        }

    }


}