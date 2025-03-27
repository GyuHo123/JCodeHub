package org.jbnu.jdevops.jcodeportallogin.dto.watcher

data class GraphDataDto(
    val timestamp: String,
    val total_size: Long,
    val size_change: Long
)

data class GraphDataListDto(
    val trends: List<GraphDataDto>
)
