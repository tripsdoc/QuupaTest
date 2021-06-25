package com.hsc.quupa.utilities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter

class ResponseWriter {
    companion object {
        fun writeResponse(response: String) {
            try {
                val fileName = Environment.getExternalStorageDirectory()
                    .absolutePath + "/response.txt"
                val fw = FileWriter(fileName, true)
                fw.write(response + "\n\n")
                fw.close()
            } catch (e: Exception) {
                Log.d("Error", e.localizedMessage!!.toString())
            }
        }

        fun openResponseFolder(context: Context) {
            val intent = Intent(Intent.ACTION_VIEW)
            val responseDir = Uri.parse("file://${Environment.getExternalStorageDirectory().absolutePath}")
            intent.setDataAndType(responseDir, "application/*")
            context.startActivity(intent)
        }
    }
}