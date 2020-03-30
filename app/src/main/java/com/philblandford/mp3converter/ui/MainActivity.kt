package com.philblandford.mp3converter.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.google.android.gms.ads.*
import com.philblandford.mp3converter.BuildConfig
import com.philblandford.mp3converter.MidiFileDescr
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.ActivityMainBinding
import com.philblandford.mp3converter.ui.filepicker.FilePickerFragmentDirections
import kotlinx.android.synthetic.main.activity_main.view.*

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    checkPermissions()
  }


  private fun initScreen() {
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    initAds()
    checkIntent()
  }

  private fun checkPermissions() {
    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
      != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      ) != PackageManager.PERMISSION_DENIED
    ) {
      requestPermissions(
        arrayOf(
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), 0
      )
    } else {
      initScreen()
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>, grantResults: IntArray
  ) {
    initScreen()
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
        if (interstitialAd.isLoaded) {
          interstitialAd.show()
        }
      }
    }
  }

  private fun checkIntent() {
    if (intent?.action == "android.intent.action.VIEW") {
      intent.data?.let { uri ->
        val name = getFileName(uri)
        val midiFile = MidiFileDescr(0, name, uri)
        val action =
          FilePickerFragmentDirections.actionFilePickerFragmentToConvertDialogFragment(
            midiFile
          )
        findNavController(R.id.nav_host_fragment).navigate(action)
      }
    }
  }

  private fun getFileName(uri: Uri): String {
    val default = uri.path ?: "untitled"

    return if (uri.getScheme().equals("content")) {
      val cursor = getContentResolver().query(uri, null, null, null, null);
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