package com.dinerdiscount.vendor.fragments

import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import com.dinerdiscount.vendor.R
import com.dinerdiscount.vendor.data.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Created by nbura on 2017-08-01.
 */
class SettingsFragment() : PreferenceFragmentCompat() {

    var dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
        activity.title = "Settings"

        dbRef = dbRef.child(getString(R.string.db_users)).child("voxNCwpFFOduKxpZXaOUwHaWVfB3")

        val masterSwitch = findPreference(getString(R.string.master_switch)) as SwitchPreference
        val scanDiscounts = findPreference(getString(R.string.scan_discounts)) as SwitchPreference
        val allowUsers = findPreference(getString(R.string.allow_your_users_to_edit_discounts)) as SwitchPreference
        val sendUpdates = findPreference(getString(R.string.send_discount_updates)) as SwitchPreference
        val receiveNotif = findPreference(getString(R.string.receive_notice_of_activated_discounts)) as SwitchPreference

        dbRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(User::class.java)

                    masterSwitch.isChecked = user?.mainSwitch!!
                    scanDiscounts.isChecked = user?.scanDiscounts!!
                    allowUsers.isChecked = user?.allowUsersToEdit!!
                    sendUpdates.isChecked = user?.senUpdates!!
                    receiveNotif.isChecked = user?.receiveNoticeOfActivatedDiscounts!!
                }

                override fun onCancelled(p0: DatabaseError?) { }

            }
        )


        if (masterSwitch != null) {
            masterSwitch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { p0, p1 ->
                val switch = p1 as Boolean

                dbRef.child("mainSwitch").setValue(switch)

                true
            }
        }

        if (scanDiscounts != null) {
            scanDiscounts.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { p0, p1 ->
                val switch = p1 as Boolean

                dbRef.child("scanDiscounts").setValue(switch)

                true
            }
        }

        if (allowUsers != null) {
            allowUsers.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { p0, p1 ->
                val switch = p1 as Boolean

                dbRef.child("allowUsersToEdit").setValue(switch)

                true
            }
        }

        if (sendUpdates != null) {
            sendUpdates.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { p0, p1 ->
                val switch = p1 as Boolean

                dbRef.child("senUpdates").setValue(switch)

                true
            }
        }

        if (receiveNotif != null) {
            receiveNotif.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { p0, p1 ->
                val switch = p1 as Boolean

                dbRef.child("receiveNoticeOfActivatedDiscounts").setValue(switch)

                true
            }
        }
    }

}