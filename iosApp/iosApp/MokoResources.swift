import ComposeApp
import SwiftUI

typealias AppStrings = ComposeAppMR.strings
typealias CoreStrings = CoreCommonMR.strings
typealias MapStrings = FeatureMapMR.strings
typealias MapPlurals = FeatureMapMR.plurals
typealias LinesStrings = FeatureLinesMR.strings

enum MokoResources {
    static let appStrings = AppStrings()
    static let coreStrings = CoreStrings()
    static let mapStrings = MapStrings()
    static let mapPlurals = MapPlurals()
    static let linesStrings = LinesStrings()
}

extension String {
    init(_ resource: StringResource) {
        self.init(MokoResourcesKt.getString(stringResource: resource).localized())
    }

    init(_ resource: StringResource, parameter: Any) {
        self.init(
            MokoResourcesKt.getString(stringResource: resource, parameter: parameter).localized()
        )
    }

    init(plural resource: PluralsResource, number: Int, parameter: Any) {
        self.init(
            MokoResourcesKt.getPluralString(
                pluralsResource: resource,
                number: Int32(number),
                parameter: parameter
            ).localized()
        )
    }

    init(_ resourceKeyPath: KeyPath<AppStrings, StringResource>) {
        self.init(
            MokoResourcesKt.getString(
                stringResource: MokoResources.appStrings[keyPath: resourceKeyPath]
            ).localized()
        )
    }

    init(_ resourceKeyPath: KeyPath<AppStrings, StringResource>, parameter: Any) {
        self.init(
            MokoResourcesKt.getString(
                stringResource: MokoResources.appStrings[keyPath: resourceKeyPath],
                parameter: parameter
            ).localized()
        )
    }

    init(_ resourceKeyPath: KeyPath<CoreStrings, StringResource>) {
        self.init(
            MokoResourcesKt.getString(
                stringResource: MokoResources.coreStrings[keyPath: resourceKeyPath]
            ).localized()
        )
    }

    init(_ resourceKeyPath: KeyPath<CoreStrings, StringResource>, parameter: Any) {
        self.init(
            MokoResourcesKt.getString(
                stringResource: MokoResources.coreStrings[keyPath: resourceKeyPath],
                parameter: parameter
            ).localized()
        )
    }

    init(_ resourceKeyPath: KeyPath<MapStrings, StringResource>) {
        self.init(
            MokoResourcesKt.getString(
                stringResource: MokoResources.mapStrings[keyPath: resourceKeyPath]
            ).localized()
        )
    }

    init(_ resourceKeyPath: KeyPath<MapStrings, StringResource>, parameter: Any) {
        self.init(
            MokoResourcesKt.getString(
                stringResource: MokoResources.mapStrings[keyPath: resourceKeyPath],
                parameter: parameter
            ).localized()
        )
    }

    init(_ resourceKeyPath: KeyPath<LinesStrings, StringResource>) {
        self.init(
            MokoResourcesKt.getString(
                stringResource: MokoResources.linesStrings[keyPath: resourceKeyPath]
            ).localized()
        )
    }

    init(_ resourceKeyPath: KeyPath<LinesStrings, StringResource>, parameter: Any) {
        self.init(
            MokoResourcesKt.getString(
                stringResource: MokoResources.linesStrings[keyPath: resourceKeyPath],
                parameter: parameter
            ).localized()
        )
    }

    init(plural resourceKeyPath: KeyPath<MapPlurals, PluralsResource>, count: Int) {
        self.init(
            plural: MokoResources.mapPlurals[keyPath: resourceKeyPath],
            number: count,
            parameter: count
        )
    }
}
