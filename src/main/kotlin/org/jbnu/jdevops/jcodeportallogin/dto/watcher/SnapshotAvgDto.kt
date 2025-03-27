package org.jbnu.jdevops.jcodeportallogin.dto.watcher

import java.time.LocalDateTime

class SnapshotAvgDto (
    val snapshot_avg: Double,
    val snapshot_size_avg: Double,
    val first: LocalDateTime?,
    val last: LocalDateTime?,
    val total: Double?,
    val interval: Int?
)