package com.example.mymessagingapp.Main.system.Fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.mymessagingapp.Main.system.Fragment.utilities.Inites
import com.example.mymessagingapp.R
import java.lang.IllegalStateException

private const val ARG_PICTURE = "picture"
private const val TAG = "PicturePickerFragment"
class PicturePickerFragment : DialogFragment() {
    private lateinit var zoomPicture : ImageView
    override fun onCreateDialog(savedInstanceState: Bundle?) : Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.full_size_layout, null)
            builder.setView(view)
            zoomPicture = view.findViewById(R.id.imageInZoomDialog) as ImageView
            val imageString =  arguments?.getSerializable(ARG_PICTURE) as String
            val bitmap = Inites.getImage(imageString)
            zoomPicture.setImageBitmap(bitmap)
            builder.setTitle("Avatar").setNegativeButton("Cancel",
                DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })
            builder.create()
        } ?: throw IllegalStateException("Activity can be full")
    }
    companion object {
        fun newInstance(imageString : String) : PicturePickerFragment{
            val args = Bundle().apply{
                putSerializable(ARG_PICTURE, imageString)
            }
            return PicturePickerFragment().apply {
                arguments = args
            }
        }
    }
}