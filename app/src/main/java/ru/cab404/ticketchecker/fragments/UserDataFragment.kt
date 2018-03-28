package ru.cab404.ticketchecker.fragments

import android.os.Bundle
import android.view.View
import org.json.JSONArray
import org.json.JSONObject
import ru.cab404.ticketchecker.BuildConfig
import ru.cab404.ticketchecker.R
import ru.cab404.ticketchecker.utils.BaseFragment
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Created on 3/27/18.
 * @author cab404
 */


class UserDataFragment : BaseFragment(R.layout.fragment_userdata) {

    suspend fun getTicketData(id: String) {

        val url = "https://event.mirfandoma.ru/getorderdata?ticket_num=$id&secret_key=${BuildConfig.USERDATA_TOKEN}"
        val con = URL(url).openConnection() as HttpsURLConnection
        con.connect()
        val data = JSONObject(con.inputStream.reader().readText())

        fun path(from: Any, vararg elements: Any): Any {
            if (elements.isEmpty()) return this
            if (from is JSONArray) return path(from[elements[0] as Int], elements.drop(1))
            if (from is JSONObject) return path(from[elements[0] as String], elements.drop(1))
            throw RuntimeException("Failed to get vararg!")
        }

        val name = path(data, "user", "fullname")


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


}