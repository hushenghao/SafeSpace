package com.dede.safespace

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import com.dede.safespace.databinding.ActivitySafeSpaceBinding
import java.io.Serializable

class SafeSpaceActivity : AppCompatActivity() {

    data class Config(
        val fullScreen: Boolean,
        val hideNavigation: Boolean,
        val transparentStatusBar: Boolean,
        val transparentNavigationBar: Boolean,
        val mode: Int,
        val fitsSystemWindows: Boolean,
    ) : Serializable {
        override fun toString(): String {
            val sb = StringBuilder()
                .append("isFullScreen=")
                .appendLine(fullScreen)
                .append("hideNavigation=")
                .appendLine(hideNavigation)
                .append("transparentStatusBar=")
                .appendLine(transparentStatusBar)
                .append("transparentNavigationBar=")
                .appendLine(transparentNavigationBar)
                .append("fitsSystemWindows=")
                .appendLine(fitsSystemWindows)
                .append("mode=")
                .append(mode)
            return sb.toString()
        }
    }

    companion object {
        const val EXTRA_CONFIG = "extra_config"
    }

    private val binding by lazy { ActivitySafeSpaceBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        val config = intent.getSerializableExtra(EXTRA_CONFIG) as? Config
        if (config != null) {
            var flag = SYSTEM_UI_FLAG_LAYOUT_STABLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flag = flag or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flag = flag or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }

            if (config.fullScreen) {
                flag = flag or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            if (config.hideNavigation) {
                flag = flag or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }
            window.decorView.systemUiVisibility = flag

            if (config.transparentStatusBar) {
                window.statusBarColor = Color.TRANSPARENT
            }
            if (config.transparentNavigationBar) {
                window.navigationBarColor = Color.TRANSPARENT
            }

            if (config.mode != -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode = config.mode
                }
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (config != null) {
            binding.root.fitsSystemWindows = config.fitsSystemWindows

            if (!config.fitsSystemWindows) {
                val left = binding.btRotate.marginLeft
                val right = binding.btRotate.marginRight
                // 默认会调用 requestFitSystemWindows()
                ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view: View, insets: WindowInsetsCompat ->
                    val stableInsets = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars() or
                                WindowInsetsCompat.Type.displayCutout()
                    )
                    @SuppressLint("SetTextI18n")
                    binding.tvText.text = "$stableInsets\n$config"
                    binding.btRotate.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        leftMargin = stableInsets.left + left
                        rightMargin = stableInsets.right + right
                        bottomMargin = stableInsets.bottom
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val windowInsets = insets.toWindowInsets()
                        binding.ivCorner.setImageDrawable(SCornerDrawable().apply {
                            val never = window.attributes.layoutInDisplayCutoutMode !=
                                    LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
                            setWindowInsets(never, windowInsets)
                        })
                    }
                    binding.layoutCutoutMode.rgCutoutMode.updatePadding(
                        left = stableInsets.left,
                        top = stableInsets.top,
                        right = stableInsets.right
                    )
                    return@setOnApplyWindowInsetsListener insets
                }
            }
            binding.tvText.text = config.toString()
            binding.layoutCutoutMode.rgCutoutMode.isVisible =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            binding.layoutCutoutMode.rbAlways.isVisible =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                binding.layoutCutoutMode.rgCutoutMode.check(
                    when (config.mode) {
                        LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT -> R.id.rb_default
                        LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS -> R.id.rb_always
                        LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER -> R.id.rb_never
                        LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES -> R.id.rb_short_edges
                        else -> R.id.rb_default
                    }
                )
                binding.layoutCutoutMode.rgCutoutMode.setOnCheckedChangeListener { _, checkedId ->
                    var mode = -1
                    when (checkedId) {
                        R.id.rb_default -> {
                            mode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                        }
                        R.id.rb_never -> {
                            mode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
                        }
                        R.id.rb_short_edges -> {
                            mode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        }
                        R.id.rb_always -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                mode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                            }
                        }
                    }
                    window.attributes = window.attributes.apply {
                        layoutInDisplayCutoutMode = mode
                    }
                }
            }
        }

        binding.btRotate.setOnClickListener {
            requestedOrientation =
                if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
        }
    }

}