package `in`.ceeq.lyte.messages

import `in`.ceeq.lyte.data.Message
import android.arch.lifecycle.LifecycleObserver
import android.databinding.ObservableArrayList
import com.google.firebase.database.*

class FirebaseDatabaseHelper<T> : LifecycleObserver {

    private val mFirebaseDatabase:
            FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    private val mDatabaseReference:
            DatabaseReference by lazy { mFirebaseDatabase.reference.child(mNode) }

    private lateinit var mDataSet: ObservableArrayList<T>

    private lateinit var mNode: String

    private lateinit var mClazz: Class<T>

    fun setDataSet(list: ObservableArrayList<T>) {
        mDataSet = list
    }

    fun setNode(node: String) {
        mNode = node
    }

    init {
        addListener(mClazz)
    }

    private fun addListener(clazz: Class<T>) {
        val childEventListener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                mDataSet.add(p0?.getValue(clazz))
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                mDataSet.add(p0?.getValue(clazz))
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                mDataSet.remove(p0?.getValue(clazz))
            }
        }
        mDatabaseReference.addChildEventListener(childEventListener)
    }

    fun push(message: Message) {
        mDatabaseReference.push().setValue(message)
    }
}
