package ru.cab404.ticketchecker.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_userdata.*
import kotlinx.coroutines.experimental.async
import ru.cab404.ticketchecker.R
import ru.cab404.ticketchecker.fragments.HintFragment
import ru.cab404.ticketchecker.fragments.QRCaptureFragment
import ru.cab404.ticketchecker.fragments.UserDataFragment
import ru.cab404.ticketchecker.utils.BaseActivity

/**
 * Created on 3/27/18.
 * @author cab404
 */


class MainActivity : BaseActivity() {

    val qrcap = QRCaptureFragment().apply {
        captureCallback = object : QRCaptureFragment.QRCaptureCallback {
            override fun onQrCodeCaptured(code: String) {
                closeViewer()
                async(HandlerC) {

                    (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
                            .vibrate(100)

                    supportFragmentManager?.apply {
                        beginTransaction()
                                .replace(R.id.vRoot, UserDataFragment().apply {
                                    arguments = Bundle().apply {
                                        putString("ticketId", code)
                                    }
                                })
                                .commit()
                    }
                }

                println("captured $code")

            }

            override fun onError(error: String) {
                async(HandlerC) {
                    Toast.makeText(vRoot.context, error, Toast.LENGTH_SHORT).show()
                }
                println("captured $error")
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vQRScan.setOnTouchListener { v, event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    openViewer()

                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    closeViewer()
                }
            }
            true
        }

        supportFragmentManager
                .beginTransaction()
                .add(R.id.vRoot, HintFragment())
                .add(R.id.vQRScanContainer, qrcap)
                .commit()

    }

    fun openViewer() {
        vQRScanContainer
                .animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .start()
        qrcap.start()
    }

    fun closeViewer() {
        vQRScanContainer
                .animate()
                .scaleX(0f)
                .scaleY(0f)
                .translationX(vQRScanContainer.width.toFloat() * .25f)
                .translationY(vQRScanContainer.width.toFloat() * .25f)
                .start()
        qrcap.pause()
    }

}