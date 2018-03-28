package ru.cab404.ticketchecker.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import ru.cab404.ticketchecker.R
import ru.cab404.ticketchecker.fragments.HintFragment
import ru.cab404.ticketchecker.fragments.QRCaptureFragment

/**
 * Created on 3/27/18.
 * @author cab404
 */


class MainActivity : AppCompatActivity() {

    val qrcap = QRCaptureFragment().apply {
        captureCallback = object : QRCaptureFragment.QRCaptureCallback {
            override fun onQrCodeCaptured(code: String) {
                closeViewer()
                println("captured $code")
            }

            override fun onError(error: String) {
                Handler(Looper.getMainLooper()).post {
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