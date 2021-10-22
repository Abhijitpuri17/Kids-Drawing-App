package com.example.kidsdrawingapp

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity()
{
    private var mImageButtonCurrentPaint : ImageButton? = null

    /** ON CREATE METHOD
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
                    super.onCreate(savedInstanceState)
                    setContentView(R.layout.activity_main)

                    /** Initially set brush size
                     */
                    drawing_view.setBrushSize(10f)

                    /** Set selected color button to black
                     */
                    mImageButtonCurrentPaint = llPaintColors[1] as ImageButton
                    mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this , R.drawable.pallet_pressed_black))


                    /** Setting onclick method on brush size Click
                     */
                    ib_brush.setOnClickListener {
                        showBrushSizeChooserDialog()
                    }

                    /** Selecting background image from gallery function
                     */
                    ib_gallery.setOnClickListener {
                        if (isReadStorageAllowed()) {
                            // run our code
                            val pickPhotoIntent = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            startActivityForResult(pickPhotoIntent , GALLERY)
                        } else {
                            requestStoragePermission()
                        }
                    }

                    ib_undo.setOnClickListener {
                        drawing_view.undoDrawing()
                    }

                    ib_save_image.setOnClickListener {
                        if (isReadStorageAllowed()){
                            BitmapAsyncTask(getBitmapFromView(frame_l_drawing)).execute()
                        } else {
                            requestStoragePermission()
                        }
                    }

    }

    /** AsyncTask to perform some tasks in background
     */
    private inner class BitmapAsyncTask(val mBitmap: Bitmap) : AsyncTask<Any , Void , String>(){
        override fun doInBackground(vararg params: Any?): String {
            var result = ""

            result = try {

                /** Covert image to PNG and save in a file
                  */

                val bytes = ByteArrayOutputStream()
                mBitmap.compress(Bitmap.CompressFormat.PNG ,90 , bytes)
                val f = File(externalCacheDir!!.absoluteFile.toString() + File.separator + "kidDrawingApp_" + System.currentTimeMillis()/1000 + ".png")
                val fos = FileOutputStream(f)
                fos.write(bytes.toByteArray())
                fos.close()
                f.absolutePath
            } catch (e : Exception){
                ""
            }

            return result

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (result!!.isNotEmpty()){
                Toast.makeText(this@MainActivity , "File saved successfully : $result" , Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity , "Something went wrong" , Toast.LENGTH_SHORT).show()
            }

        }
    }

    /** When user selects a photo
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null){
                        bg_image_drawing.visibility = View.VISIBLE
                        bg_image_drawing.setImageURI(data.data)
                    } else {
                        Toast.makeText(this , "Error in parsing the image" , Toast.LENGTH_SHORT).show()
                    }
                } catch (e : Exception){
                    e.printStackTrace()
                    Toast.makeText(this , "Error in parsing the image" , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** Request for Storage permission
     */
    private fun requestStoragePermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this , arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE ,
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())) {
//            Toast.makeText(this , "Need permission to add background" , Toast.LENGTH_SHORT).show()
//        }

        ActivityCompat.requestPermissions(this , arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE ,
            android.Manifest.permission.READ_EXTERNAL_STORAGE) , STORAGE_PERMISSION_CODE)
    }

    /** When user accepts / denies permission
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)  {
                Toast.makeText(this , "Permission granted. Now you can select background Image from your galley" , Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this , "OOPS ! You denied the permission" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Checks if storage permission is already given or not
     */
    private fun isReadStorageAllowed() : Boolean{
        val result = ContextCompat.checkSelfPermission(this , android.Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    /** Constant values
     */
    companion object {
        const val STORAGE_PERMISSION_CODE = 1
        const val GALLERY = 2
    }


    /** Method to show a dialog to select brush size
     */
    private fun showBrushSizeChooserDialog()
    {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size")
        val smallBtn = brushDialog.small_brush
        smallBtn.setOnClickListener {
            drawing_view.setBrushSize(10f)
            brushDialog.dismiss()
        }

        val mediumBtn = brushDialog.medium_brush
        mediumBtn.setOnClickListener {
            drawing_view.setBrushSize(20f)
            brushDialog.dismiss()
        }

        val largeBtn = brushDialog.large_brush
        largeBtn.setOnClickListener {
            drawing_view.setBrushSize(30f)
            brushDialog.dismiss()
        }

        brushDialog.show()
    }


    /** Method to change color
     */
    fun colorSelect(view : View)
    {
        if (mImageButtonCurrentPaint != view) {

            /** Change drawable of previous selected button
             */
            when(mImageButtonCurrentPaint)
            {
                ib_color_black -> mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_normal_black))
                ib_color_blue -> mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_normal_blue))
                ib_color_green -> mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_normal_green))
                ib_color_lollipop -> mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_normal_lollipop))
                ib_color_random -> mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_normal_random))
                ib_color_red -> mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_normal_red))
                ib_color_yellow -> mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_normal_yellow))
                ib_color_skin -> mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_normal_skin))
                else -> null
            }


            /** Change selected color
             */
            mImageButtonCurrentPaint = view as ImageButton

            /** call colorSet method from drawingView
             */
            drawing_view.setColor(view.tag.toString())


            /** Change drawable of currently selected button
             */
            when(view)
            {
                ib_color_random -> view.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_pressed_random))
                ib_color_lollipop -> view.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_pressed_lollipop))
                ib_color_yellow -> view.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_pressed_yellow))
                ib_color_blue -> view.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_pressed_blue))
                ib_color_green -> view.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_pressed_green))
                ib_color_red -> view.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_pressed_red))
                ib_color_black -> view.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_pressed_black))
                ib_color_skin -> view.setImageDrawable(ContextCompat.getDrawable(this ,R.drawable.pallet_pressed_skin))
                else -> null
            }



        }
    }

    /** Method to convert view to bitmap
     */
    private fun getBitmapFromView(v : View) : Bitmap{

        val bitmap = Bitmap.createBitmap(v.width , v.height , Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = v.background
        if (bgDrawable != null){
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        v.draw(canvas)
        return bitmap
    }

}