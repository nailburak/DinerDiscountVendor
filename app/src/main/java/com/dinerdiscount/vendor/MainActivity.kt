package com.dinerdiscount.vendor

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.dinerdiscount.vendor.fragments.SettingsFragment
import com.dinerdiscount.vendor.fragments.TodaysDiscountFragment
import com.dinerdiscount.vendor.fragments.VerifyDiscountFragment
import com.google.android.gms.vision.barcode.Barcode
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val GO_SCAN_ACTIVITY = 1001
    val PERMISSIONS_REQUEST_CAMERA = 2001

    val TODAYS_DISCOUNTS = "today's discounts"
    val VERIFY_DISCOUNT = "verify discount"
    val SETTINGS = "settings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        fragment(TodaysDiscountFragment(), null, TODAYS_DISCOUNTS)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_day -> fragment(TodaysDiscountFragment(), null, TODAYS_DISCOUNTS)
            R.id.nav_verify_discount -> verifyDiscount()
            R.id.nav_add_user -> supportFragmentManager.beginTransaction().remove(TodaysDiscountFragment()).commit()
            R.id.nav_settings -> fragment(SettingsFragment(), null, SETTINGS)
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun verifyDiscount() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, Array(1, {android.Manifest.permission.CAMERA}), PERMISSIONS_REQUEST_CAMERA)
        else
            startActivityForResult(Intent(applicationContext, ScanActivity::class.java), GO_SCAN_ACTIVITY)
    }

    private fun fragment(fragment: Fragment, data: String?, tag: String){

        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        if (data != null) {
            val bundle = Bundle()
            bundle.putString("data", data)
            fragment.arguments = bundle
        }
        transaction.replace(R.id.content_main_frame_layout, fragment, tag)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if( requestCode == PERMISSIONS_REQUEST_CAMERA){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startActivityForResult(Intent(applicationContext, ScanActivity::class.java), GO_SCAN_ACTIVITY)
             else
                Snackbar.make(main_main, "You need give a permission to use this feature", Snackbar.LENGTH_LONG).show()
        }
        return
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GO_SCAN_ACTIVITY && resultCode == Activity.RESULT_OK){
            if (data != null) {
                val barcode: Barcode =  data.getParcelableExtra("barcode")
                fragment(VerifyDiscountFragment(), barcode.displayValue, VERIFY_DISCOUNT)
            }
        }
    }
}
