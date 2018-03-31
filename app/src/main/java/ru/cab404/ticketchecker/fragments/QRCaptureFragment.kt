package ru.cab404.ticketchecker.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import com.google.zxing.*
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.android.synthetic.main.fragment_checker.*
import kotlinx.coroutines.experimental.async
import ru.cab404.ticketchecker.R
import ru.cab404.ticketchecker.utils.BaseFragment
import ru.cab404.ticketchecker.utils.v
import java.sql.SQLOutput
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Created on 3/27/18.
 * @author cab404
 */


class QRCaptureFragment : BaseFragment(R.layout.fragment_checker) {


    val qrReader = QRCodeReader()

    interface QRCaptureCallback {
        fun onQrCodeCaptured(code: String)
        fun onError(error: String)
    }

    var captureCallback = object : QRCaptureCallback {
        override fun onQrCodeCaptured(code: String) = v("captured $code")

        override fun onError(error: String) = v("error occured: $error")
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = context ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ctx.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 9999)
                return
            }
        }

        onConfigurationChanged(context!!.resources.configuration)
//        init()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val ctx = context ?: return

        if (!grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            captureCallback.onError(ctx.getString(R.string.lacking_camera_permission))
        } else {
//            init()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        orientationChanged = true
    }

    var orientationChanged = true
    var cameraLaunched = true

    val cameraThread = Executors.newSingleThreadExecutor()
    var camera: Camera? = null
    fun camera(run: Camera.() -> Unit) = cameraThread.execute { camera?.apply(run) }

    fun init() {
        v("init camera")

        val desisiredPixelSize = 500 * 500

        camera = Camera.open().apply {
            parameters = parameters.apply {
                val size = supportedPreviewSizes
                        .sortedBy { it.width * it.height }
                        .firstOrNull { it.width * it.height > desisiredPixelSize }
                        ?: supportedPictureSizes[0]
                v("selected ps ${size.width}x${size.height}")
                setPreviewSize(size.width, size.height)
                previewFormat = ImageFormat.YV12
                focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }
        }

        fun linkSurface(surface: SurfaceTexture) {
            camera {
                v("surface linked")
                setPreviewTexture(surface)
                startPreview()
            }
        }

        if (vSurface.isAvailable) linkSurface(vSurface.surfaceTexture)

        vSurface.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, w: Int, h: Int) = Unit
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = updateSurf()
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) = linkSurface(surface)

            fun updateSurf() {
                if (orientationChanged) {
                    start()
                }
                if (cameraLaunched) {
                    cameraLaunched = true
                    async(HandlerC) {
                        vProgress?.visibility = View.GONE
                    }
                }
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                pause()
                return true
            }


        }

    }

    fun pause() {
        v("release camera")
        camera {
            camera = null
            try {
                stopPreview()
                setPreviewTexture(null)
            } catch (c: Exception) {
                context?.apply {
                    captureCallback.onError(getString(R.string.stop_error))
                }
            } finally {
                release()
            }
        }
    }

    fun start() {
        async(HandlerC) {
            vProgress?.visibility = View.VISIBLE
        }

        if (camera == null) init()
        v("start camera")

        camera {


            val rotation = (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?)?.defaultDisplay?.rotation
                    ?: 0
            try {
                stopPreview()
            } catch (c: Exception) {
                context?.apply {
                    captureCallback.onError(getString(R.string.init_error))
                }
            }

            when (rotation) {
                0 -> setDisplayOrientation(90)
                1 -> setDisplayOrientation(0)
                2 -> setDisplayOrientation(270)
                3 -> setDisplayOrientation(180)
            }

            val ratio = parameters.previewSize.width / parameters.previewSize.height.toFloat()
            async(HandlerC) {
                vSurface?.setTransform(Matrix().apply {
                    when (rotation) {
                        0 -> setScale(1f, ratio)
                        1 -> setScale(ratio, 1f)
                        2 -> setScale(1f, ratio)
                        3 -> setScale(ratio, 1f)
                    }
                })
            }

            var frameRule = 0

            setPreviewCallback { data, camera ->
                camera {

                    val w = parameters.previewSize.width
                    val h = parameters.previewSize.height

                    val dataLS = PlanarYUVLuminanceSource(data, w, h, 0, 0, w, h, false)


                    frameRule++
                    if (frameRule % 5 == 0) {
                        try {
                            val result = qrReader.decode(BinaryBitmap(GlobalHistogramBinarizer(dataLS)))
                            println(result.barcodeFormat.name)
                            println(result.timestamp)
                            println(result.text)
                            async(HandlerC) {
                                captureCallback.onQrCodeCaptured(result.text)
                            }
                        } catch (e: ReaderException) {
                            // ignoooored
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }

                }

            }

            orientationChanged = false
            try {
                startPreview()
            } catch (c: Exception) {
                async(HandlerC) {
                    captureCallback.onError(getString(R.string.init_error))
                }
            }

        }

    }

}