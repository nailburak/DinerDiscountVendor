package com.dinerdiscount.vendor.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import com.dinerdiscount.vendor.R

class VerifyDiscountFragment : Fragment() {

    var data = ""


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val r = inflater?.inflate(R.layout.fragment_custom_date, container, false)

        if (arguments != null) {
            data = arguments.getString("data")
            Log.wtf("data", data)
        }

        activity.title = "Verify Discounts"

        Log.wtf("hey", "heyyyy")

        return r
    }
}