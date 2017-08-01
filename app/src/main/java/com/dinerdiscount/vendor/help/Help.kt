package com.dinerdiscount.vendor.help

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

/**
 * Created by nbura on 2017-08-01.
 */

class Help {
    fun destroyFragment(fragment: Fragment, context: FragmentActivity){
        context.supportFragmentManager.beginTransaction().remove(fragment).commit()
    }
}
