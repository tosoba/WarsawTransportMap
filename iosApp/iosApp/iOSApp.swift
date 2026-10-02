import ComposeApp
import SwiftUI

@main
struct iOSApp: App {
    init() {
        ViewControllersKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
