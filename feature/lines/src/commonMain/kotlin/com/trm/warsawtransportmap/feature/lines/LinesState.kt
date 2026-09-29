package com.trm.warsawtransportmap.feature.lines

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateSetOf
import com.trm.warsawtransportmap.core.model.Line

@Stable
class LinesState(val lines: Map<String, List<Line>>, selectedLines: Set<String>) {
  private val allNumbers: List<String> = lines.values.flatten().map(Line::number).distinct()
  val selectedLines = mutableStateSetOf<String>().apply { addAll(selectedLines) }

  val allSelected: Boolean
    get() = allNumbers.size == selectedLines.size

  fun selectAll() {
    selectedLines.addAll(allNumbers)
  }

  fun toggleLine(number: String) {
    if (selectedLines.contains(number)) selectedLines.remove(number) else selectedLines.add(number)
  }

  fun toggleAll() {
    if (allSelected) selectedLines.clear() else selectAll()
  }
}
