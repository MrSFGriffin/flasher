package com.example.flasher

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class CameraTorchController(private val cameraManager: CameraManager) {

    private val torchCameraId: String? = findTorchCameraId()

    val hasFlash: Boolean get() = torchCameraId != null

    fun setTorch(on: Boolean): Boolean {
        val id = torchCameraId ?: return false
        return try {
            cameraManager.setTorchMode(id, on)
            true
        } catch (e: CameraAccessException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    private fun findTorchCameraId(): String? = try {
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    } catch (e: CameraAccessException) {
        null
    }
}
