import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.m_abdullah.quickglance.R
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

private lateinit var mAuth: FirebaseAuth
private lateinit var database: DatabaseReference
private lateinit var typeofuser: String
class chatsearch_recycle_adapter(private val items: MutableList<Chats>, private val snaparray: MutableList<Snap>): RecyclerView.Adapter<chatsearch_recycle_adapter.ViewHolder>() {
    private lateinit var onClickListener: chatsearch_recycle_adapter.OnClickListener
    private lateinit var onAcceptClickListener: OnAcceptClickListener

    class ViewHolder (itemview: View): RecyclerView.ViewHolder(itemview){
        val profleimg: CircleImageView = itemView.findViewById(R.id.user_img)
        val personname: TextView = itemView.findViewById(R.id.user_name)
        val messagebutton: Button = itemview.findViewById(R.id.message_button)
        val notifpic: ImageView = itemView.findViewById(R.id.snap_notif_pic)
        val notiftext: TextView = itemView.findViewById(R.id.snap_notif)
        val timeago: TextView = itemView.findViewById(R.id.timeago)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chatsearch_page_recycle, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = items[position]
        mAuth = Firebase.auth

        holder.notifpic.visibility = View.GONE
        holder.notiftext.visibility = View.GONE
        holder.timeago.visibility = View.GONE

        setrecyclerdata(holder, chat)

        holder.messagebutton.setOnClickListener(){
            onAcceptClickListener.onAcceptClick(position, chat)
        }
    }

    // A function to bind the onclickListener.
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
    fun setOnAcceptClickListener(onAcceptClickListener: OnAcceptClickListener) {
        this.onAcceptClickListener = onAcceptClickListener
    }

    // onClickListener Interface
    interface OnAcceptClickListener {
        fun onAcceptClick(position: Int, model: Chats)
    }
    interface OnClickListener {
        fun onClick(model: User)
    }

    fun setrecyclerdata(holder: ViewHolder, chat: Chats) {
        mAuth = Firebase.auth
        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    if (user != null) {
                        FirebaseDatabase.getInstance().getReference("User").child(user.id).child("Chats").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snap: DataSnapshot) {
                                if (snap.exists()) {
                                    for (temp in snap.children) {
                                        val chats = temp.getValue(String::class.java)
                                        if (chats != null) {
                                            if (chats == chat.id && user.id != mAuth.uid.toString()) {
                                                holder.personname.text = user.name

                                                Glide.with(holder.itemView)
                                                    .load(user.profilepic)
                                                    .into(holder.profleimg)
                                            }
                                        }
                                    }
                                    var set = false
                                    for (temp in snaparray) {
                                        if (temp.senderid == user.id && !set) {
                                            if (temp.tag == "image"){
                                                holder.notifpic.visibility = View.VISIBLE
                                                holder.notiftext.visibility = View.VISIBLE

                                                holder.notifpic.setImageResource(R.drawable.cyan_rectangle_chats)
                                                holder.notiftext.text = "New Glance"
                                                holder.notiftext.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.cyan_chats))
                                                set = true
                                            }
                                            else if (temp.tag == "video"){
                                                holder.notifpic.visibility = View.VISIBLE
                                                holder.notiftext.visibility = View.VISIBLE

                                                holder.notifpic.setImageResource(R.drawable.purple_rectangle_chats)
                                                holder.notiftext.text = "New Glance"
                                                holder.notiftext.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.purple_chats))
                                                set = true
                                            }
                                            holder.timeago.visibility = View.VISIBLE
                                            holder.timeago.text = getTimeAgo(temp.time)
                                        }
                                    }

                                    var temparray = mutableListOf<Snap>()
                                    for (temp in snaparray) {
                                        if (temp.senderid == user.id) {
                                            temparray.add(temp)
                                        }
                                    }

                                    holder.itemView.setOnClickListener {
                                        onClickListener.onClick(user)
                                    }

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getTimeAgo(time: String): String {
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
        val past = sdf.parse(time)
        val now = Date()

        val seconds = (now.time - past.time) / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes m"
            hours < 24 -> "$hours h"
            else -> "$days d"
        }
    }
}