package com.dinerdiscount.vendor.help

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by nbura on 2017-08-01.
 */

class Help {
    fun destroyFragment(fragment: Fragment, context: FragmentActivity){
        context.supportFragmentManager.beginTransaction().remove(fragment).commit()
    }

    fun getToday() : Long {
        val cal = Calendar.getInstance() //current date and time
        cal.add(Calendar.DAY_OF_MONTH, 1) //add a day
        cal.set(Calendar.HOUR_OF_DAY, 23) //set hour to last hour
        cal.set(Calendar.MINUTE, 59) //set minutes to last minute
        cal.set(Calendar.SECOND, 59) //set seconds to last second
        cal.set(Calendar.MILLISECOND, 999) //set milliseconds to last millisecond
        return  cal.timeInMillis
    }
}
