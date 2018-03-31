package ru.cab404.ticketchecker.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.telephony.PhoneNumberUtils
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

    suspend fun markTicket(orderId: Int): JSONObject = suspendCoroutine { coro ->
        requestThread.execute {

            val url = "https://event.mirfandoma.ru/setorderstatus?order_id=$orderId&status_id=3&secret_key=${BuildConfig.USERDATA_TOKEN}"
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

    fun showError(e: String?) {
        vError.visibility = VISIBLE
        vProgress.visibility = GONE
        vUserdata.visibility = GONE
        vError.text = e
    }

    fun startProgress() {

        vProgress.visibility = VISIBLE
        vUserdata.visibility = GONE
        vError.visibility = GONE
    }

    fun showUserdata() {
        vUserdata.visibility = VISIBLE
        vProgress.visibility = GONE
        vError.visibility = GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startProgress()

        async(HandlerC) fetch@{

            val data = try {
                val data = getTicketData(arguments!!.getString("ticketId"))
                if (data.has("error"))
                    throw RuntimeException(path(data, "error_description").toString())
                data
            } catch (e: Exception) {
                Log.w("UserChecker", e)
                showError(e.message)
                return@fetch
            }


            val name = path(data, "user", "fullname")?.toString()
            val email = path(data, "user", "email")?.toString()
            val phone = path(data, "address", "phone")?.toString()
            val ticketCost = path(data, "products", 0, "price")?.toString()?.toFloat()?.toInt()
            val ticketType = path(data, "products", 0, "pagetitle")?.toString()

            fun TextView.setTextOrInv(text: String?) {
                visibility = if (text.isNullOrEmpty())
                    GONE
                else
                    VISIBLE
                this.text = text
            }

            fun String?.formatNumber(): String? {
                if (this == null || length != 10) return this
                return "+7 (${this.substring(0..2)}) ${this.substring(3..5)}-${this.substring(6..7)}-${this.substring(8..9)}"
            }

            vName.setTextOrInv(name)
            vPhone.setTextOrInv(phone.formatNumber())
            vEmail.setTextOrInv(email)
            vCost.setTextOrInv("$ticketCost р.")
            vType.setTextOrInv(ticketType)

            // Checking if ticket is marked, and showing mark button/mark info accordingly
            listOf(vInfoEntered, vMark).forEach { it.visibility = GONE }

            if (path(data, "order", "status") as? Int == 3)
                vInfoEntered.visibility = VISIBLE
            else
                vMark.visibility = VISIBLE

            val orderId = path(data, "order", "id") as Int

            vMark.setOnClickListener {
                async(HandlerC) mark@{
                    startProgress()

                    val (message, success) = try {
                        val data = markTicket(orderId)
                        val message = path(data, "message") as? String
                        val success = path(data, "success") as? Boolean ?: false
                        message to success
                    } catch (e: Throwable) {
                        e.message to false
                    }

                    Toast.makeText(
                            getContext(),
                            message
                                    ?: "Wat. Some shitty server data format error occured, probably. IDK.",
                            Toast.LENGTH_LONG
                    ).show()

                    if (success)
                        vMark.visibility = GONE
                    showUserdata()

                }
            }

            showUserdata()

        }

    }


}