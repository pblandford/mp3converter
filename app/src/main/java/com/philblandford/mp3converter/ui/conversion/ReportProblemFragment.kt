package com.philblandford.mp3converter.ui.conversion

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.FragmentReportProblemBinding

class ReportProblemFragment : DialogFragment() {

    private lateinit var binding: FragmentReportProblemBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportProblemBinding.inflate(inflater)
        binding.button.setOnClickListener {
            composeEmail()
        }
        return binding.root
    }

    private fun composeEmail() {
        context?.let { ctx ->
            val email = ctx.getString(R.string.problem_email)
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
            intent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.problem_subject))
            startActivity(intent)
        }
    }
}