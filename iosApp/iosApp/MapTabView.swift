import ComposeApp
import SwiftUI
import UIKit

struct MapTabView: View {
    @State private var vehicleCount = 0

    var body: some View {
        NavigationStack {
            mapContent
                .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
                .toolbarBackground(.visible, for: .navigationBar)
                .toolbarBackground(.ultraThinMaterial, for: .tabBar)
                .toolbarBackground(.visible, for: .tabBar)
                .ignoresSafeArea()
        }
        .tabItem {
            Label(String(\AppStrings.map_navigation_label), systemImage: "map")
        }
    }
}

private extension MapTabView {
    @ViewBuilder
    var mapContent: some View {
        if #available(iOS 26.0, *) {
            mapHost
                .navigationTitle(String(\MapStrings.app_name))
                .navigationBarTitleDisplayMode(.inline)
                .navigationSubtitle(
                    String(plural: \MapPlurals.tracking_vehicles, count: vehicleCount)
                )
        } else {
            mapHost
                .navigationTitle("")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .principal) {
                        VStack(spacing: 0) {
                            Text(String(\MapStrings.app_name))
                                .font(.headline)
                            Text(String(plural: \MapPlurals.tracking_vehicles, count: vehicleCount))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .accessibilityElement(children: .combine)
                    }
                }
        }
    }

    var mapHost: some View {
        MapComposeView(vehicleCount: $vehicleCount)
    }
}

private struct MapComposeView: UIViewControllerRepresentable {
    @Binding var vehicleCount: Int

    func makeUIViewController(context _: Context) -> UIViewController {
        ViewControllersKt.mapViewController(
            onVehicleCountChanged: { count in
                vehicleCount = count.intValue
            }
        )
    }

    func updateUIViewController(_: UIViewController, context _: Context) {}
}
