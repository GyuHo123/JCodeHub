package org.jbnu.jdevops.jcodeportallogin.dto.watcher

import java.time.LocalDateTime

data class WatcherBuildLogDto(
    val binary_path: String,
    val cmdline: String,
    val exit_code: Int,
    val cwd: String,
    val target_path: String,
    val timestamp: LocalDateTime,
    val file_size: Long,
)

data class WatcherBuildLogListDto(
    val build_logs: List<WatcherBuildLogDto>
)