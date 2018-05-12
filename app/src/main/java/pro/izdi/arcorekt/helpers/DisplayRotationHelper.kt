/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pro.izdi.arcorekt.helpers

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.WindowManager
import com.google.ar.core.Session

/**
 * Helper to track the display rotations. In particular, the 180 degree rotations are not notified
 * by the onSurfaceChanged() callback, and thus they require listening to the android display
 * events.
 */
class DisplayRotationHelper
/**
 * Constructs the DisplayRotationHelper but does not register the listener yet.
 *
 * @param context the Android [Context].
 */
(private val context: Context) : DisplayManager.DisplayListener {
    private var viewportChanged: Boolean = false
    private var viewportWidth: Int = 0
    private var viewportHeight: Int = 0
    private val display: Display = context.getSystemService(WindowManager::class.java)!!.defaultDisplay

    /**
     * Returns the current rotation state of android display. Same as [Display.getRotation].
     */
    val rotation: Int
        get() = display.rotation

    /** Registers the display listener. Should be called from [Activity.onResume].  */
    fun onResume() {
        context.getSystemService(DisplayManager::class.java)!!.registerDisplayListener(this, null)
    }

    /** Unregisters the display listener. Should be called from [Activity.onPause].  */
    fun onPause() {
        context.getSystemService(DisplayManager::class.java)!!.unregisterDisplayListener(this)
    }

    /**
     * Records a change in surface dimensions. This will be later used by [ ][.updateSessionIfNeeded].
     * Should be called from [ ].
     *
     * @param width the updated width of the surface.
     * @param height the updated height of the surface.
     */
    fun onSurfaceChanged(width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        viewportChanged = true
    }

    /**
     * Updates the session display geometry if a change was posted either by [ ][.onSurfaceChanged] call
     * or by [.onDisplayChanged] system callback. This
     * function should be called explicitly before each call to [Session.update]. This
     * function will also clear the 'pending update' (viewportChanged) flag.
     *
     * @param session the [Session] object to update if display geometry changed.
     */
    fun updateSessionIfNeeded(session: Session) {
        if (viewportChanged) {
            val displayRotation = display.rotation
            session.setDisplayGeometry(displayRotation, viewportWidth, viewportHeight)
            viewportChanged = false
        }
    }

    override fun onDisplayAdded(displayId: Int) {}

    override fun onDisplayRemoved(displayId: Int) {}

    override fun onDisplayChanged(displayId: Int) {
        viewportChanged = true
    }
}
