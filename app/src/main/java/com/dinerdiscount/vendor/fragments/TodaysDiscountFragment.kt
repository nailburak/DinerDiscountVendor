package com.dinerdiscount.vendor.fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

import com.dinerdiscount.vendor.R
import com.dinerdiscount.vendor.data.Discounts
import com.dinerdiscount.vendor.data.SeekBars
import com.dinerdiscount.vendor.data.User
import com.dinerdiscount.vendor.data.seekBarArray
import com.dinerdiscount.vendor.help.Help
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class TodaysDiscountFragment : Fragment() {

    var db = FirebaseDatabase.getInstance().reference


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val r = inflater?.inflate(R.layout.fragment_todays_discount, container, false)

        activity.title = "Today's Discounts"

        db = db.child(context.getString(R.string.db_users)).child("voxNCwpFFOduKxpZXaOUwHaWVfB3")
        checkHasData()

        val recycler = r?.findViewById<RecyclerView>(R.id.todays_discount_recycler_view)
        val switch = r?.findViewById<Switch>(R.id.todays_discount_switch)

        recycler(recycler)
        switch(switch)

        return r
    }

    private fun checkHasData() {
        db.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val user = p0.getValue(User::class.java)

                        db.child("discounts").addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onDataChange(p1: DataSnapshot) {
                                        if (!p1.hasChild(todayDate()) && time(today()) < time(user?.close)) {

                                            val newDb = db.child("discounts").child(todayDate())

                                            val start = (user?.open)!!.split(':')
                                            val close = (user?.close)!!.split(':')

                                            var iStart = start[0].toInt()
                                            var iStartMin = start[1].toInt()
                                            var iClose = close[0].toInt()
                                            var iCloseMin = close[1].toInt()

                                            for (x in 0..48) {
                                                val push = newDb.push()
                                                push.child("hour").setValue(iStart.toString() + ":" + iStartMin.toString() + if (iStartMin == 0) "0" else "")
                                                push.child("discountRate").setValue(0)
                                                if (iStart == iClose && iStartMin == iCloseMin)
                                                    return
                                                iStartMin = if (iStartMin == 0) 30 else 0
                                                iStart += if (iStartMin == 0 && (user?.close != (iStart.toString() + ":" + iStartMin + "0"))) 1 else 0
                                            }
                                        }
                                    }

                                    override fun onCancelled(p1: DatabaseError?) {
                                    }

                                }
                        )

                    }

                }
        )
        removeDataByClock()
    }

    private fun removeDataByClock() {
        db.child("discounts").child(todayDate()).addValueEventListener(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {  }

                    override fun onDataChange(p0: DataSnapshot) {
                        for (postSnapshot in p0.children) {
                            val data = postSnapshot.getValue(Discounts::class.java)

                            if (time(today()) > time(data?.hour)) {
                                db.child("discounts").child(todayDate()).child(postSnapshot.key).removeValue()
                            }
                        }
                    }
                }
        )
    }

    override fun onPause() {
        super.onPause()

        Help().destroyFragment(this, activity)
    }

    private fun recycler(recyclerView: RecyclerView?){
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = object : FirebaseRecyclerAdapter<Discounts, DiscountHolder>(
                Discounts::class.java,
                R.layout.fragment_todays_discount_item,
                DiscountHolder::class.java,
                db.child("discounts").child(todayDate())
        ){
            override fun populateViewHolder(viewHolder: DiscountHolder?, model: Discounts?, position: Int) {
                val key =  this.getRef(position).key

                viewHolder?.hour?.text = model?.hour
                viewHolder?.seekBar(model, key, context)
            }
        }
    }

    private class DiscountHolder(view: View): RecyclerView.ViewHolder(view) {
        val main = view.findViewById<LinearLayout>(R.id.todays_discount_item_main)
        val hour = view.findViewById<TextView>(R.id.todays_discount_item_hour)
        val seekBar = view.findViewById<SeekBar>(R.id.todays_discount_item_seek_bar)
        val discount = view.findViewById<TextView>(R.id.todays_discount_item_discount)

        fun seekBar(model: Discounts?, key: String, context: Context) {

            try {
                discount?.text = model?.discountRate!!.toString() + "%"
                seekBar?.progress = model?.discountRate!! / 5
                seekBar.setOnSeekBarChangeListener(
                        object : SeekBar.OnSeekBarChangeListener{
                            var progress = 0
                            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                                progress = p1 * 5
                                discount.text = progress.toString() + "%"
                            }

                            override fun onStartTrackingTouch(p0: SeekBar?) { }

                            override fun onStopTrackingTouch(p0: SeekBar?) {
                                if (!seekBarArray.contains(SeekBars(key, progress)))
                                    seekBarArray.add(SeekBars(key, progress))
                                val db = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.db_users)).child("voxNCwpFFOduKxpZXaOUwHaWVfB3")
                                        .child("discounts").child(TodaysDiscountFragment().todayDate()).child(key)

                                db.child("discountRate").setValue(progress)
                            }
                        }
                )
            } catch (e: Exception){ }

        }
    }

    private fun switch(switch: Switch?){
        db.child("mainSwitch").addValueEventListener(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) { }

                    override fun onDataChange(p0: DataSnapshot) {
                        val data = p0.getValue(Boolean::class.java)
                        if (data != null){
                            switch?.isChecked = data
                        }

                    }
                }
        )
        switch?.setOnClickListener {
            if (switch?.isChecked)
                AlertDialog.Builder(activity)
                    .setTitle("Publish Discount")
                    .setMessage("Do you really want to publish discount to users and send a notification?")
                    .setPositiveButton("Yes", { dialogInterface, i ->
                        db.child("mainSwitch").setValue(switch?.isChecked)
                    })
                    .setNegativeButton("Nah, I am good", { dialogInterface, i ->
                        switch?.isChecked = false
                    }).show()
            else
                db.child("mainSwitch").setValue(switch?.isChecked)
        }
    }

    private fun today() = SimpleDateFormat("HH:mm").format(Calendar.getInstance().time)

    private fun time(time: String?) = SimpleDateFormat("HH:mm").parse(time).time

    private fun todayDate(): String = SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().time)
}
