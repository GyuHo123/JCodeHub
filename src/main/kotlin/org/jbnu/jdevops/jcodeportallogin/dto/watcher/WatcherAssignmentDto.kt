package org.jbnu.jdevops.jcodeportallogin.dto.watcher

class WatcherAssignmentInfoDto (
    val student_num: Long,
    val timestamp: String,
    val code_size: Long
)

class WatcherAssignmentDto (
    val percentile_90: Double,
    val percentile_50: Double,
    val top_7: List<WatcherAssignmentInfoDto>,
    val avg_bytes: Long,
    val avg_num: Double
)