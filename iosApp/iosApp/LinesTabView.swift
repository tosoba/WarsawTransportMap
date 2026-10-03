import ComposeApp
import SwiftUI
import UIKit

struct LinesTabView: View {
    @State private var searchText = ""
    @State private var stateBridge = LinesScreenStateBridge()
    @State private var selectionState: Bool? = nil

    var body: some View {
        NavigationStack {
            LinesComposeView(
                searchText: searchText,
                stateBridge: stateBridge,
                selectionState: $selectionState
            )
            .navigationTitle(String(\AppStrings.lines_navigation_label))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    toggleAllButton
                }
            }
            .searchable(
                text: $searchText,
                placement: .navigationBarDrawer(displayMode: .always),
                prompt: String(\LinesStrings.search_lines_placeholder)
            )
            .toolbarBackground(.thickMaterial, for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
            .toolbarBackground(.thickMaterial, for: .tabBar)
            .toolbarBackground(.visible, for: .tabBar)
            .ignoresSafeArea()
        }
        .tabItem {
            Label(String(\AppStrings.lines_navigation_label), systemImage: "square.grid.2x2")
        }
    }

    private var toggleAllButton: some View {
        Button {
            stateBridge.toggleAllLines()
        } label: {
            Image(
                systemName: selectionState == true
                    ? "checkmark.square.fill"
                    : "square"
            )
        }
        .accessibilityLabel(
            selectionState == true
                ? String(\LinesStrings.deselect_all_content_description)
                : String(\LinesStrings.select_all_content_description)
        )
        .disabled(selectionState == nil)
    }
}

private struct LinesComposeView: UIViewControllerRepresentable {
    let searchText: String
    let stateBridge: LinesScreenStateBridge

    @Binding var selectionState: Bool?

    func makeUIViewController(context _: Context) -> UIViewController {
        stateBridge.searchText = searchText
        return ViewControllersKt.linesViewController(
            stateBridge: stateBridge,
            onSelectionStateChanged: { selectionState = $0?.boolValue }
        )
    }

    func updateUIViewController(_: UIViewController, context _: Context) {
        stateBridge.searchText = searchText
    }
}
