package com.dinerdiscount.vendor.data

/**
 * Created by nbura on 2017-07-26.
 */

// Today's Discounts
data class Discounts(val hour: String? = null, val discountRate: Int? = null)
data class SeekBars(val key: String, val discount: Int)
val seekBarArray = ArrayList<SeekBars>()