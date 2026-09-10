package com.example.flasher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.flasher.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var torchController: CameraTorchController
    private lateinit var player: TorchPatternPlayer

    private var isRunning = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startSelectedPattern()
            } else {
                Toast.makeText(this, R.string.error_permission_denied, Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            disableForNoFlash()
            return
        }

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        torchController = CameraTorchController(cameraManager)
        if (!torchController.hasFlash) {
            disableForNoFlash()
            return
        }

        player = TorchPatternPlayer(Handler(Looper.getMainLooper())) { on ->
            torchController.setTorch(on)
        }

        binding.startStopButton.setOnClickListener {
            if (isRunning) stopPattern() else onStartRequested()
        }

        binding.patternGroup.setOnCheckedChangeListener { _, _ ->
            if (isRunning) startSelectedPattern()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::player.isInitialized && isRunning) {
            stopPattern()
        }
    }

    private fun disableForNoFlash() {
        binding.startStopButton.isEnabled = false
        binding.patternGroup.isEnabled = false
        for (i in 0 until binding.patternGroup.childCount) {
            binding.patternGroup.getChildAt(i).isEnabled = false
        }
        binding.statusText.text = getString(R.string.error_no_flash)
    }

    private fun onStartRequested() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startSelectedPattern()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startSelectedPattern() {
        val pattern = selectedPattern()
        player.start(pattern)
        isRunning = true
        binding.startStopButton.text = getString(R.string.action_stop)
        binding.statusText.text = getString(R.string.status_running, patternLabel(pattern))
    }

    private fun stopPattern() {
        player.stop()
        isRunning = false
        binding.startStopButton.text = getString(R.string.action_start)
        binding.statusText.text = getString(R.string.status_idle)
    }

    private fun selectedPattern(): TorchPattern = when (binding.patternGroup.checkedRadioButtonId) {
        R.id.radioStrobe -> TorchPattern.STROBE
        R.id.radioSos -> TorchPattern.SOS
        else -> TorchPattern.STEADY
    }

    private fun patternLabel(pattern: TorchPattern): String = when (pattern) {
        TorchPattern.STEADY -> getString(R.string.pattern_steady)
        TorchPattern.STROBE -> getString(R.string.pattern_strobe)
        TorchPattern.SOS -> getString(R.string.pattern_sos)
    }
}
