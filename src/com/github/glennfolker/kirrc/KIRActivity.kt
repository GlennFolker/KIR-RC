package com.github.glennfolker.kirrc

import android.os.*
import androidx.activity.result.*
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.*
import androidx.fragment.app.*
import com.github.glennfolker.kirrc.frag.*

class KIRActivity: AppCompatActivity(R.layout.act_kir) {
    private lateinit var reqBluetooth: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        if(savedInstanceState == null) {
            reqBluetooth = registerForActivityResult(RequestPermission()) { isGranted ->
                if(isGranted) {

                } else {

                }
            }

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<RequestFrag>(R.id.view_request)
            }
        }
    }
}
