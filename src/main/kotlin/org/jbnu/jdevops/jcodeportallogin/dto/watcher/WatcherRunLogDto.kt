package org.jbnu.jdevops.jcodeportallogin.dto.watcher

import java.time.LocalDateTime

data class WatcherRunLogDto(
    val cmdline: String,
    val exit_code: Int,
    val cwd: String,
    val target_path: String,
    val process_type: String,
    val timestamp: LocalDateTime,
    val file_size: Long,
)

data class WatcherRunLogListDto(
    val run_logs: List<WatcherRunLogDto>
)