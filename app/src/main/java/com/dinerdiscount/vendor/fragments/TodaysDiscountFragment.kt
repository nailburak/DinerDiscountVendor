package com.dinerdiscount.vendor.fragments

import android.content.DialogInterface
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
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

import com.dinerdiscount.vendor.R
import com.dinerdiscount.vendor.data.Discounts
import com.dinerdiscount.vendor.data.SeekBars
import com.dinerdiscount.vendor.data.seekBarArray
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TodaysDiscountFragment : Fragment() {

    val db = FirebaseDatabase.getInstance().reference.child("users").child("voxNCwpFFOduKxpZXaOUwHaWVfB3").child("07-26-2017")

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val r = inflater?.inflate(R.layout.fragment_todays_discount, container, false)

        val recycler = r?.findViewById<RecyclerView>(R.id.todays_discount_recycler_view)
        val fab = r?.findViewById<FloatingActionButton>(R.id.todays_discount_fab)
        val switch = r?.findViewById<Switch>(R.id.todays_discount_switch)

        recycler(recycler)
        fab?.setOnClickListener { fab() }
        switch(switch)

        return r
    }

    private fun recycler(recyclerView: RecyclerView?){
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = object : FirebaseRecyclerAdapter<Discounts, DiscountHolder>(
                Discounts::class.java,
                R.layout.fragment_todays_discount_item,
                DiscountHolder::class.java,
                db
        ){
            override fun populateViewHolder(viewHolder: DiscountHolder?, model: Discounts?, position: Int) {
                val key =  this.getRef(position).key

                viewHolder?.hour?.text = model?.hour
                viewHolder?.seekBar(model, key)
            }
        }
    }

    private class DiscountHolder(view: View): RecyclerView.ViewHolder(view) {
        val hour = view.findViewById<TextView>(R.id.todays_discount_item_hour)
        val seekBar = view.findViewById<SeekBar>(R.id.todays_discount_item_seek_bar)
        val discount = view.findViewById<TextView>(R.id.todays_discount_item_discount)

        fun seekBar(model: Discounts?, key: String) {
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
                            val db = FirebaseDatabase.getInstance().reference.child("users").child("voxNCwpFFOduKxpZXaOUwHaWVfB3").child("07-26-2017").child(key)
                            db.child("discountRate").setValue(progress)
                        }
                    }
            )
        }
    }

    private fun fab(){
        AlertDialog.Builder(activity)
                .setTitle("Publish Discount")
                .setMessage("Do you really want to publish discount to users and send a notification?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, i ->
                    db.parent.child("mainSwitch").setValue(true)
                })
                .setNegativeButton("Nah, I am good", null).show()
    }

    private fun switch(switch: Switch?){
        db.parent.child("mainSwitch").addValueEventListener(
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
                        db.parent.child("mainSwitch").setValue(switch?.isChecked)
                    })
                    .setNegativeButton("Nah, I am good", { dialogInterface, i ->
                        switch?.isChecked = false
                    }).show()
            else
                db.parent.child("mainSwitch").setValue(switch?.isChecked)
        }
    }
}
