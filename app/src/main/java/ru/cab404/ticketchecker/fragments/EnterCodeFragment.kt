package ru.cab404.ticketchecker.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_enter_text.*
import ru.cab404.ticketchecker.R
import ru.cab404.ticketchecker.utils.BaseFragment

/**
 * Created on 3/31/18.
 * @author cab404
 */


class EnterCodeFragment : BaseFragment(layout = R.layout.fragment_enter_text) {

    interface CodeListener {
        fun onCancel()
        fun onCodeGot(code: String)
    }

    var listener: CodeListener? = null


    fun checkCode() {
        if (vCode.text.length == 8 && vCodePrefix.text.length == 3) {
            val prefix = vCodePrefix.text.toString()
            // saving code prefix
            context
                    ?.getSharedPreferences("prefix_s", 0)
                    ?.edit()
                    ?.putString("prefix", prefix)
                    ?.apply()
            val code = vCode.text.toString()
            listener?.onCodeGot(prefix + code)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context
                ?.getSharedPreferences("prefix_s", 0)
                ?.getString("prefix", null)?.let {
                    vCodePrefix.setText(it)
                }

        vCancel.setOnClickListener {
            listener?.onCancel()
        }

        vCheck.setOnClickListener {
            checkCode()
        }

        vCodePrefix.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 3) vCode.requestFocus()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })

        vCode.setOnEditorActionListener { v, actionId, event ->
            println(event.action)
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                checkCode()
                true
            } else
                false
        }

    }

}