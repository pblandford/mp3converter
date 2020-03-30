package com.philblandford.mp3converter.ui

//import com.jaiselrahman.filepicker.activity.FilePickerActivity
//import com.jaiselrahman.filepicker.model.MediaFile

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.philblandford.mp3converter.MidiFileDescr
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.ActivityMainBinding
import com.philblandford.mp3converter.ui.filepicker.FilePickerFragmentDirections
import kotlinx.android.synthetic.main.activity_main.view.*


private const val FILE_REQUEST_CODE = 0

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var adView: AdView
  private lateinit var interstitialAd: InterstitialAd


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    initAds()
    checkIntent()
  }

  private fun initAds() {
    MobileAds.initialize(this) {
      adView = findViewById<AdView>(R.id.adView)
      val adRequest: AdRequest = AdRequest.Builder().build()
      adView.loadAd(adRequest)

      interstitialAd = InterstitialAd(this)
      interstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
      interstitialAd.loadAd(AdRequest.Builder().build())
      findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { controller, destination, arguments ->
        if (destination.id == R.id.file_picker_fragment) {
          if (interstitialAd.isLoaded) {
            interstitialAd.show()
          }
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
          FilePickerFragmentDirections.actionFilePickerFragmentToConvertOptionsFragment(midiFile)
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