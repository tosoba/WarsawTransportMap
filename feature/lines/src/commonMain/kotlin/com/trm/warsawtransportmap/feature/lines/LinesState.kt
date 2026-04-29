package com.trm.warsawtransportmap.feature.lines

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateSetOf
import com.trm.warsawtransportmap.core.model.Line

@Stable
class LinesState(val lines: Map<String, List<Line>>, selectedLines: Set<String>) {
  val selectedLines = mutableStateSetOf<String>().apply { addAll(selectedLines) }

  val allSelected: Boolean
    get() =
      lines.map { (_, lines) -> lines.size }.reduce { acc, i -> acc + i } == selectedLines.size

  fun selectAll() {
    selectedLines.addAll(lines.values.flatten().map(Line::number))
  }

  fun toggleLine(number: String) {
    if (selectedLines.contains(number)) selectedLines.remove(number) else selectedLines.add(number)
  }

  fun toggleAll() {
    if (allSelected) selectedLines.clear() else selectAll()
  }
}
