package ru.cab404.ticketchecker.fragments

import android.os.Bundle
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
        if (vCode.text.length == 8)
            listener?.onCodeGot("eff" + vCode.text.toString())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vCancel.setOnClickListener {
            listener?.onCancel()
        }

        vCheck.setOnClickListener {
            checkCode()
        }

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