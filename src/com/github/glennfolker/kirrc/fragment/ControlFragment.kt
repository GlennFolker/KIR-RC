package com.github.glennfolker.kirrc.fragment

import android.annotation.*
import android.content.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.fragment.app.*
import com.github.glennfolker.kirrc.*

class ControlFragment: Fragment(R.layout.fragment_control) {
    private lateinit var buttons: Array<ImageButton>
    private val axes: Array<Int> = Array(4) { 0 }

    private var done = false;

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        buttons = arrayOf(
            view.findViewById(R.id.btn_control_n),
            view.findViewById(R.id.btn_control_e),
            view.findViewById(R.id.btn_control_s),
            view.findViewById(R.id.btn_control_w),
        )

        for(i in 0..<4) {
            buttons[i].setOnTouchListener { btnView, motion ->
                when(motion.action) {
                    MotionEvent.ACTION_DOWN -> {
                        axes[i] = 1
                        update(btnView.context)
                    }
                    MotionEvent.ACTION_UP -> {
                        axes[i] = 0
                        update(btnView.context)
                    }
                }

                true
            }
        }

        view.findViewById<Button>(R.id.btn_cancel_control).setOnClickListener { btnView ->
            (btnView.context as? BluetoothConnector)?.cancel()
        }

        val looper = Looper.getMainLooper();
        val handler = Handler(looper)
        handler.postDelayed(object: Runnable {
            override fun run() {
                (context as? BluetoothConnector)?.refresh()
                if(!done) handler.postDelayed(this, 1000)
            }
        }, 1000)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        done = true
    }

    private fun update(context: Context) {
        val x = axes[1] - axes[3]
        val y = axes[0] - axes[2]
        (context as? BluetoothConnector)?.command(x, y)
    }
}
