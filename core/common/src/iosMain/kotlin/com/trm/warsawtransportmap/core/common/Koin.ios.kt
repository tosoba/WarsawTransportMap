package com.trm.warsawtransportmap.core.common

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSThread
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIApplicationWillResignActiveNotification

actual fun platformCommonModule(): Module = module {
  single(AppLifecycleOwner) { ProcessLifecycleOwner }.bind(LifecycleOwner::class)
}

private object ProcessLifecycleOwner : LifecycleOwner {
  override val lifecycle: Lifecycle
    field = LifecycleRegistry(this)

  init {
    check(NSThread.isMainThread) { "ProcessLifecycleOwner must be created on the main thread" }

    lifecycle.currentState = Lifecycle.State.CREATED
    lifecycle.currentState =
      when (UIApplication.sharedApplication.applicationState) {
        UIApplicationState.UIApplicationStateActive -> Lifecycle.State.RESUMED
        UIApplicationState.UIApplicationStateInactive -> Lifecycle.State.STARTED
        else -> Lifecycle.State.CREATED
      }

    observe(UIApplicationWillEnterForegroundNotification) {
      if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        lifecycle.currentState = Lifecycle.State.STARTED
      }
    }
    observe(UIApplicationDidBecomeActiveNotification) {
      lifecycle.currentState = Lifecycle.State.RESUMED
    }
    observe(UIApplicationWillResignActiveNotification) {
      if (lifecycle.currentState == Lifecycle.State.RESUMED) {
        lifecycle.currentState = Lifecycle.State.STARTED
      }
    }
    observe(UIApplicationDidEnterBackgroundNotification) {
      lifecycle.currentState = Lifecycle.State.CREATED
    }
  }

  private fun observe(name: String?, block: () -> Unit) {
    NSNotificationCenter.defaultCenter.addObserverForName(
      name = name,
      `object` = null,
      queue = NSOperationQueue.mainQueue,
    ) {
      block()
    }
  }
}
