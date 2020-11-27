package com.philblandford.mp3converter.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.google.android.gms.ads.*
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.philblandford.mp3converter.BuildConfig
import com.philblandford.mp3convertercore.MediaFileDescr
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.ActivityMainBinding
import com.philblandford.mp3converter.ui.filepicker.FilePickerFragmentDirections
import kotlin.random.Random

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var reviewManager: ReviewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reviewManager = ReviewManagerFactory.create(this)
        requestPermissions {
            initScreen()
        }
    }

    private fun requestPermissions(done: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                0
            )
        } else done()
    }

    private fun initScreen() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAds()
        checkIntent()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        initScreen()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e("MAIN", "onConfigurationChanged")
    }

    private fun initAds() {
        MobileAds.initialize(this) {
            initBanner()
            initInterstitial()
        }
    }

    private fun initBanner() {
        val id = if (BuildConfig.DEBUG) R.string.banner_id_test else R.string.banner_id_release
        val adFrame = binding.adView
        val adView = AdView(this)
        adView.adUnitId = getString(id)
        adView.adSize = AdSize.BANNER
        adFrame.removeAllViews()
        adFrame.addView(adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun initInterstitial() {
        val id = if (BuildConfig.DEBUG) R.string.inter_id_test else R.string.inter_id_release
        val interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = getString(id)
        interstitialAd.loadAd(AdRequest.Builder().build())
        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.file_picker_fragment) {
                val askReview = isEstablishedUser() && Random.nextInt(2) == 0
                if (askReview) {
                    launchReview()
                } else {
                    if (interstitialAd.isLoaded) {
                        interstitialAd.show()
                    }
                }
            }
        }
    }

    private fun isEstablishedUser(): Boolean {
        val installDate = packageManager.getPackageInfo(packageName, 0).firstInstallTime
        val now = System.currentTimeMillis()
        return (now - installDate) > 1000 * 60 * 60 * 24 * 2
    }

    private fun launchReview() {

        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { req ->
            if (req.isSuccessful) {
                Log.e("PF", "request successful")
                val flow = reviewManager.launchReviewFlow(this, request.result)
                flow.addOnCompleteListener {
                    Log.e("PF", "Done")
                }
            }
        }
    }

    private fun checkIntent() {
        if (intent?.action == "android.intent.action.VIEW") {
            intent.data?.let { uri ->
                val name = getFileName(uri)
                val midiFile =
                    MediaFileDescr(0, name, uri)
                if (findNavController(R.id.nav_host_fragment).currentDestination?.id == R.id.file_picker_fragment) {
                    val action =
                        FilePickerFragmentDirections.actionFilePickerFragmentToConvertDialogFragment(
                            midiFile
                        )
                    findNavController(R.id.nav_host_fragment).navigate(action)
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        val default = uri.path ?: "untitled"

        return if (uri.scheme.equals("content")) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                } else {
                    default
                }
            } ?: default

        } else {
            default
        }
    }

}