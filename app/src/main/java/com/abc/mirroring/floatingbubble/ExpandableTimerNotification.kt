package com.abc.mirroring.floatingbubble

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import com.abc.mirroring.helper.logIfError
import com.abc.mirroring.helper.onComplete

class ExpandableTimerNotification(
    private val builder: BuilderTimerNoti,
) : BaseFloatingView(builder.context) {
    init {
        setupLayoutParams()
    }

    fun show() = logIfError {
        builder.rootView?.let { super.show(it) }
    }.onComplete {
        builder.listener.onOpenTimerNotiView()
    }

    fun remove() = logIfError {
        builder.rootView?.let { super.remove(it) }
    }.onComplete {
        builder.listener.onCloseTimerNotiView()
    }

    override fun setupLayoutParams() {
        super.setupLayoutParams()
        logIfError {
            windowParams?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP
                flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                dimAmount = builder.dim
            }
        }
    }

    interface Action {
        fun backToBubble() {}
        fun onOpenTimerNotiView() {}
        fun onCloseTimerNotiView() {}
    }

    class BuilderTimerNoti : IExpandableTimerNotiViewBuilder {
        lateinit var context: Context

        var rootView: View? = null
        var listener = object : Action {}
        var dim = 0f

        override fun with(context: Context): BuilderTimerNoti {
            this.context = context
            return this
        }

        @SuppressLint("UseCompatLoadingForDrawables", "UseCompatLoadingForColorStateLists")
        override fun setDrawingToolView(TimerNotiViewBinding: ViewBinding): BuilderTimerNoti {
            //set action draw tools

            this.rootView = TimerNotiViewBinding.root
            return this
        }

        override fun addDrawingToolViewListener(action: Action): BuilderTimerNoti {
            this.listener = action
            return this
        }

        override fun setDimAmount(dimAmount: Float): BuilderTimerNoti {
            this.dim = dimAmount
            return this
        }

        override fun build(): ExpandableTimerNotification {
            return ExpandableTimerNotification(this)
        }
    }

    interface IExpandableTimerNotiViewBuilder {

        fun with(context: Context): IExpandableTimerNotiViewBuilder

        fun setDrawingToolView(binding: ViewBinding): IExpandableTimerNotiViewBuilder

        fun addDrawingToolViewListener(action: ExpandableTimerNotification.Action): IExpandableTimerNotiViewBuilder

        fun setDimAmount(dimAmount: Float): IExpandableTimerNotiViewBuilder

        fun build(): ExpandableTimerNotification

    }
}