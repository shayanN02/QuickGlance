package com.m_abdullah.quickglance

import User
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.random.Random

private lateinit var storage: FirebaseStorage
private lateinit var mAuth: FirebaseAuth
class Profile_Activity : AppCompatActivity() {
    private var usr = User()
    private var profilePictureUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)
        mAuth = Firebase.auth


        val profilepic: CircleImageView = findViewById(R.id.profile_picture)
        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    usr = snapshot.getValue(User::class.java)!!
                    findViewById<TextView>(R.id.username).text = usr.username
                    findViewById<TextView>(R.id.name).text = usr.name

                    if (!(this@Profile_Activity).isFinishing and !(this@Profile_Activity).isDestroyed) {
                        Glide.with(this@Profile_Activity)
                            .load(usr.profilepic)
                            .into(profilepic)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        profilepic.setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            openGalleryForImage()
        }

        val rootLayout = findViewById<LinearLayout>(R.id.root_layout)
        val swipeup = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = e2.y - e1!!.y
                if (diffX < -300) {
                    finish()
                    overridePendingTransition(R.anim.slide_in_bottom,R.anim.fade_out)
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
        rootLayout.setOnTouchListener { _, event ->
            swipeup.onTouchEvent(event)
            true
        }

        findViewById<View>(R.id.acceptfriends_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, accept_friends_activity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.settings_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, Settings_Activity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.back_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            finish()
            overridePendingTransition(0, R.anim.slide_out_top)
        }

    }

    private val galleryprofileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null){
            profilePictureUri = uri
            usr.profilepic = uri.toString()
            updateprofilepic()
        }
    }
    private fun openGalleryForImage() {
        galleryprofileLauncher.launch("image/*")
    }

    private fun updateprofilepic(){
        storage = FirebaseStorage.getInstance()
        val storageref = storage.reference
        val userId = mAuth.uid;

        profilePictureUri?. let{
            storageref.child("profilepics").child(userId!! + Random.nextInt(0,100000).toString()).putFile(profilePictureUri!!).addOnSuccessListener {
                it.metadata!!.reference!!.downloadUrl.addOnSuccessListener {task ->
                    usr.profilepic = task.toString()
                    Log.w("TAG", "Upload Success")

                    FirebaseDatabase.getInstance().getReference("User").child(userId).child("profilepic").setValue(usr.profilepic).addOnSuccessListener {
                        Log.w("TAG", "Upload Success")
                    }
                }
            }.addOnFailureListener{
                Log.w("TAG", "Upload failed")
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(0, R.anim.slide_out_top)
    }
}