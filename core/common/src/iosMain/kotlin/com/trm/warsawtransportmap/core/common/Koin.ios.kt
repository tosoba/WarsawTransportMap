package com.trm.warsawtransportmap.core.common

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.UIKit.UIApplicationWillTerminateNotification

actual fun platformCommonModule(): Module = module {
  single(AppLifecycle) { AppLifecycleOwner.lifecycle }
}

private object AppLifecycleOwner : LifecycleOwner {
  private val _lifecycle = LifecycleRegistry(this)
  override val lifecycle: Lifecycle = _lifecycle

  init {
    _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

    val currentState = UIApplication.sharedApplication.applicationState
    when (currentState) {
      UIApplicationState.UIApplicationStateActive -> {
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
      }
      UIApplicationState.UIApplicationStateInactive -> {
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
      }
      else -> {}
    }

    val center = NSNotificationCenter.defaultCenter
    val queue = NSOperationQueue.mainQueue

    center.addObserverForName(
      name = UIApplicationWillEnterForegroundNotification,
      `object` = null,
      queue = queue,
    ) {
      if (_lifecycle.currentState == Lifecycle.State.CREATED) {
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
      }
    }

    center.addObserverForName(
      name = UIApplicationDidBecomeActiveNotification,
      `object` = null,
      queue = queue,
    ) {
      if (_lifecycle.currentState == Lifecycle.State.CREATED) {
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
      }
      _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    center.addObserverForName(
      name = UIApplicationWillResignActiveNotification,
      `object` = null,
      queue = queue,
    ) {
      _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    center.addObserverForName(
      name = UIApplicationDidEnterBackgroundNotification,
      `object` = null,
      queue = queue,
    ) {
      _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    center.addObserverForName(
      name = UIApplicationWillTerminateNotification,
      `object` = null,
      queue = queue,
    ) {
      if (_lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
      }
      _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
  }
}
