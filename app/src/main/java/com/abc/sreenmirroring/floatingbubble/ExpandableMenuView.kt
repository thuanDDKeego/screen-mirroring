package com.abc.sreenmirroring.floatingbubble

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.abc.sreenmirroring.helper.logIfError
import com.abc.sreenmirroring.helper.onComplete

class ExpandableMenuView(
    private val builder: BuilderMenu,
) : BaseFloatingView(builder.context) {

    init {
        setupLayoutParams()
    }

    // interface -----------------------------------------------------------------------------------

    interface Action {

        fun popToBubble() {}
        fun onOpenExpandableView() {}
        fun onCloseExpandableView() {}
        fun navigateToTimerNoti() {}
        fun navigateToDrawingToolView() {}
        fun onCameraPreview() {}
    }

    // public --------------------------------------------------------------------------------------

    fun show() = logIfError {
        super.show(builder.rootView!!)
    }.onComplete {
        builder.listener.onOpenExpandableView()
    }


    fun remove() = logIfError {
        super.remove(builder.rootView!!)
    }.onComplete {
        builder.listener.onCloseExpandableView()
    }


    // private -------------------------------------------------------------------------------------

    override fun setupLayoutParams() {
        super.setupLayoutParams()

        logIfError {
            windowParams?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP
                flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                dimAmount = builder.dim         // default = 0.5f
//                softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
//            windowAnimations = R.style.TransViewStyle
            }

        }

    }

    // builder class -------------------------------------------------------------------------------

    class BuilderMenu : IExpandableMenuViewBuilder {

        lateinit var context: Context

        var rootView: View? = null
        var listener = object : ExpandableMenuView.Action {}

        var dim = 0.5f

        override fun with(context: Context): BuilderMenu {
            this.context = context
            return this
        }

        override fun setExpandableView(view: View): BuilderMenu {
            this.rootView = view
            return this
        }

        override fun addExpandableViewListener(action: Action): BuilderMenu {
            this.listener = action
            return this
        }

        override fun setDimAmount(dimAmount: Float): BuilderMenu {
            this.dim = dimAmount
            return this
        }


        override fun build(): ExpandableMenuView {
            return ExpandableMenuView(this)
        }

    }

    interface IExpandableMenuViewBuilder {

        fun with(context: Context): IExpandableMenuViewBuilder

        fun setExpandableView(view: View): IExpandableMenuViewBuilder

        fun addExpandableViewListener(action: Action): IExpandableMenuViewBuilder

        fun setDimAmount(dimAmount: Float): IExpandableMenuViewBuilder

        fun build(): ExpandableMenuView

    }
}

