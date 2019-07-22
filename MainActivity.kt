package com.digimva.imageupload

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest

import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.HashMap
import java.util.jar.Attributes

private var UploadBn: Button? = null
private var ChooseBn:Button? = null
private var NAME: EditText? = null
private var imgView: ImageView? = null
private val IMG_REQUEST = 1
private var bitmap: Bitmap? = null
private val URL_ROOT = "http://172.14.0.160/image/upload.php"

class MainActivity : AppCompatActivity(), View.OnClickListener {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		UploadBn = findViewById(R.id.uploadBn) as Button
		ChooseBn = findViewById(R.id.chooseBn) as Button
		NAME = findViewById(R.id.name) as EditText
		imgView = findViewById(R.id.imageview) as ImageView
		UploadBn!!.setOnClickListener(this)
		ChooseBn!!.setOnClickListener(this)
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.chooseBn -> selectImage()
			R.id.uploadBn -> uploadImage()
		}
	}

	private fun selectImage() {
		val intent = Intent()
		intent.type = "image/*"
		intent.action = Intent.ACTION_GET_CONTENT
		startActivityForResult(intent, IMG_REQUEST)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == IMG_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
			val path = data.data
			try {
				bitmap = MediaStore.Images.Media.getBitmap(contentResolver, path)
				imgView!!.setImageBitmap(bitmap)
				imgView!!.setVisibility(View.VISIBLE)
				NAME!!.setVisibility(View.VISIBLE)
			} catch (e: IOException) {
				e.printStackTrace()
			}

		}
	}

	private fun uploadImage() {
		val stringRequest = object : StringRequest(Request.Method.POST, URL_ROOT,
			Response.Listener { response ->
				try {
					val jsonObject = JSONObject(response)
					val Response = jsonObject.getString("response")
					Toast.makeText(this@MainActivity, "response from server is$Response", Toast.LENGTH_SHORT).show()
					imgView!!.setImageResource(0)
					imgView!!.setVisibility(View.GONE)
					NAME!!.setText("")
					NAME!!.setVisibility(View.GONE)
				} catch (e: JSONException) {
					e.printStackTrace()
				}
			},
			Response.ErrorListener {
				 Toast.makeText(this@MainActivity, "Can not send", Toast.LENGTH_SHORT).show()
			}
		) {
			@Throws(AuthFailureError::class)
			override fun getParams(): Map<String, String> {
				val params = HashMap<String, String>()
				params["name"] = NAME!!.getText().toString().trim { it <= ' ' }
				params["image"] = imageToString(bitmap!!)
				return params
			}
		}

		MySingleton.getInstance(this@MainActivity).addToRequestQueue(stringRequest)

	}

	private fun imageToString(bitmap: Bitmap): String {
		val byteArrayOutputStream = ByteArrayOutputStream()
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
		val imgBytes = byteArrayOutputStream.toByteArray()
		return Base64.encodeToString(imgBytes, Base64.DEFAULT)
	}

}
