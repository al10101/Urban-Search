package al10101.android.urbansearch.ui

import al10101.android.urbansearch.R
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView

class LoadingDialog(context: Context) {

    private val dialog = Dialog(context)

    fun show(title: String) {

        dialog.setContentView(R.layout.dialog_loading)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val titleTextView: TextView = dialog.findViewById(R.id.title_text_view) as TextView
        titleTextView.text = title

        dialog.create()
        dialog.show()

    }

    fun hide() {
        dialog.dismiss()
    }

}