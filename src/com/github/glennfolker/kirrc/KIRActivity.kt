package com.github.glennfolker.kirrc

import android.bluetooth.*
import android.content.*
import android.os.*
import androidx.activity.result.*
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.*
import androidx.fragment.app.*
import com.github.glennfolker.kirrc.frag.*

class KIRActivity: AppCompatActivity(R.layout.act_kir) {
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val enableBluetooth: ActivityResultLauncher<Intent> = registerForActivityResult(StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            
        } else {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        if(savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<RequestFrag>(R.id.view_request)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter
        if(adapter == null) {
            AlertDialog.Builder(this)
                .setMessage(R.string.bluetooth_unsupported)
                .setOnDismissListener { finishAndRemoveTask() }
                .create()
                .show()
        }

        bluetoothAdapter = adapter
    }

    fun requestBluetooth() {
        enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }
}
