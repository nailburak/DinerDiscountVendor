package com.dinerdiscount.vendor.fragments

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.*
import com.dinerdiscount.vendor.R
import com.dinerdiscount.vendor.data.Discounts
import com.dinerdiscount.vendor.data.User
import com.dinerdiscount.vendor.help.Help
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by nbura on 2017-08-16.
 */

class CustomDateFragment : Fragment() {

    var db = FirebaseDatabase.getInstance().reference
    var recycler : RecyclerView? = null
    var selectedDate: String? = null
    val repeatDates = ArrayList<String>()
    var main : RelativeLayout? = null
    private var menu: Menu? = null
    private var selected = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val r = inflater?.inflate(R.layout.fragment_custom_date, container, false)

        db = db.child(context.getString(R.string.db_users)).child("voxNCwpFFOduKxpZXaOUwHaWVfB3")
        activity.title = "Date Picker"

        repeatDates.clear()

        main = r?.findViewById<RelativeLayout>(R.id.custom_main)
        var calendar = r?.findViewById<CalendarView>(R.id.fragment_custom_date_calendar_view)
        calendar?.minDate = Help().getToday()

        calendarListener(calendar, main)
        setHasOptionsMenu(true);

        recycler = r?.findViewById<RecyclerView>(R.id.fragment_custom_date_recycler_view)

        return r
    }

    private fun calendarListener(calendar: CalendarView?, main: RelativeLayout?) {
        calendar?.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val date = month.toString() + "-" + dayOfMonth + "-" + year
            if (selected){
                repeatDates.add(date)
            } else {
                selectedDate = date
                checkHasData(date)
            }
        }
    }

    private fun recycler(date: String){
        recycler?.layoutManager = LinearLayoutManager(activity)
        recycler?.adapter = object : FirebaseRecyclerAdapter<Discounts, DiscountsHolder>(
                Discounts::class.java,
                R.layout.fragment_todays_discount_item,
                DiscountsHolder::class.java,
                db.child("discounts").child(date)
        ){
            override fun populateViewHolder(viewHolder: DiscountsHolder?, model: Discounts?, position: Int) {
                val key =  this.getRef(position).key

                viewHolder?.hour?.text = model?.hour
                viewHolder?.seekBar(model, key, context, date)
            }
        }
    }

    private class DiscountsHolder(view: View): RecyclerView.ViewHolder(view) {
        val main = view.findViewById<LinearLayout>(R.id.todays_discount_item_main)
        val hour = view.findViewById<TextView>(R.id.todays_discount_item_hour)
        val seekBar = view.findViewById<SeekBar>(R.id.todays_discount_item_seek_bar)
        val discount = view.findViewById<TextView>(R.id.todays_discount_item_discount)

        fun seekBar(model: Discounts?, key: String, context: Context, date: String) {

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
//                                if (!seekBarArray.contains(SeekBars(key, progress)))
//                                    seekBarArray.add(SeekBars(key, progress))
                                val db = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.db_users)).child("voxNCwpFFOduKxpZXaOUwHaWVfB3")
                                        .child("discounts").child(date).child(key)

                                db.child("discountRate").setValue(progress)
                            }
                        }
                )
            } catch (e: Exception){ }

        }
    }

    private fun checkHasData(date: String) {
        val db = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.db_users)).child("voxNCwpFFOduKxpZXaOUwHaWVfB3")
        db.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val user = p0.getValue(User::class.java)



                        db.child("discounts").addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onDataChange(p1: DataSnapshot) {
                                        if (!p1.hasChild(date)){

                                            val newDb = db.child("discounts").child(date)

                                            val start = (user?.open)!!.split(':')
                                            val close = (user?.close)!!.split(':')

                                            var iStart = start[0].toInt()
                                            var iStartMin = start[1].toInt()
                                            var iClose = close[0].toInt()
                                            var iCloseMin = close[1].toInt()

                                            for (x in 0..48){
                                                val push = newDb.push()
                                                push.child("hour").setValue(iStart.toString() + ":" + iStartMin.toString() + if (iStartMin == 0) "0" else "")
                                                push.child("discountRate").setValue(0)
                                                if (iStart == iClose && iStartMin == iCloseMin)
                                                    return
                                                iStartMin = if (iStartMin == 0) 30 else 0
                                                iStart += if (iStartMin == 0 && (user?.close != (iStart.toString() + ":" + iStartMin + "0")) ) 1 else 0
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
        recycler(date)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        val item = menu.findItem(R.id.action_repeat)
        item.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_repeat -> {
                changeIcon()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun changeIcon() {

        val snackbar: Snackbar = Snackbar.make(main as View, "Select Date(s) to repeat!", Snackbar.LENGTH_INDEFINITE)
        if (menu != null) {
            val item = menu!!.findItem(R.id.action_repeat)
            if (item != null) {
                item.setIcon(
                        if (selected) {
                            snackbar.dismiss()
                            R.drawable.ic_repeat_white_24dp
                        } else {
                            snackbar.setAction("UPLOAD",  {
                                sendArrayTODb()
                                snackbar.dismiss()
                                Runnable {
                                    Thread.sleep(1800)
                                    repeatDates.clear()
                                }
                                item.setIcon(
                                        if (selected) {
                                            R.drawable.ic_repeat_white_24dp
                                        } else {
                                            R.drawable.ic_repeat_green_24dp
                                        }
                                )
                                selected = !selected
                            }).show()
                            R.drawable.ic_repeat_green_24dp
                        }
                )
            }
        }
        selected = !selected
    }

    private fun sendArrayTODb() {
        db.child("discounts").child(selectedDate).addListenerForSingleValueEvent( object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) { }

            override fun onDataChange(p0: DataSnapshot) {
                var i = 0
                for (snap in p0.children){
                    val dr = snap.child("discountRate").getValue(Int::class.java)
                    val hour = snap.child("hour").getValue(String::class.java)
                    Log.wtf(dr.toString(), hour)

                    if (i == 0)
                        for (x in 0 until repeatDates.size)
                            db.child("discounts").child(repeatDates[x]).setValue(null)

                    for (x in 0 until repeatDates.size){
                        db.child("discounts").child(repeatDates[x]).push().setValue(Discounts(hour, dr))
                        Log.wtf("hey", "oop")
                    }

                    i++
                }
            }

        })



    }

    private fun today() = SimpleDateFormat("HH:mm").format(Calendar.getInstance().time)

    private fun time(time: String?) = SimpleDateFormat("HH:mm").parse(time).time

    private fun todayDate(): String = SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().time)
}