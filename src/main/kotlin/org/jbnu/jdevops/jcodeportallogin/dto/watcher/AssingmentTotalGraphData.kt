package org.jbnu.jdevops.jcodeportallogin.dto.watcher

data class AssingmentTotalGraphData(
    val student_num: Int,
    val size_change: Long
)

data class AssingmentTotalGraphListData(
    val results: List<AssingmentTotalGraphData>
)