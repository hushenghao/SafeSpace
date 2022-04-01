package com.dede.safespace

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager.LayoutParams.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dede.safespace.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btStartAct.setOnClickListener {
            startSafeSpaceAct()
        }
        binding.layoutCutoutMode.rgCutoutMode.isVisible =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        binding.layoutCutoutMode.rbAlways.isVisible =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    private fun startSafeSpaceAct() {

        var mode = -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            when (binding.layoutCutoutMode.rgCutoutMode.checkedRadioButtonId) {
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
        }
        val config = SafeSpaceActivity.Config(
            fullScreen = binding.cbFullScreen.isChecked,
            hideNavigation = binding.cbHideNavigation.isChecked,
            transparentStatusBar = binding.cbTransparentStatusBar.isChecked,
            transparentNavigationBar = binding.cbTransparentNavigationBar.isChecked,
            fitsSystemWindows = binding.cbFitsSystemWindows.isChecked,
            mode = mode
        )

        startActivity(Intent(this, SafeSpaceActivity::class.java)
            .putExtra(SafeSpaceActivity.EXTRA_CONFIG, config))
    }

}