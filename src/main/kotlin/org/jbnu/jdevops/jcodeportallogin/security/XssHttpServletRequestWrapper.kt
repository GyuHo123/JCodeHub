package org.jbnu.jdevops.jcodeportallogin.security

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper

class XssHttpServletRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    // 단일 파라미터 정제
    override fun getParameter(name: String?): String? {
        val value = super.getParameter(name)
        return value?.let { sanitize(it) }
    }

    // 파라미터 배열 정제
    override fun getParameterValues(name: String?): Array<String>? {
        val values = super.getParameterValues(name)
        // sanitize()가 non-null을 반환하므로, null 체크 후 빈 문자열로 대체하는 방식으로 처리
        return values?.map { sanitize(it) }?.toTypedArray()
    }

    // 파라미터 맵 정제 (필요시 사용)
    override fun getParameterMap(): MutableMap<String, Array<String>> {
        val originalMap = super.getParameterMap()
        val sanitizedMap = mutableMapOf<String, Array<String>>()
        for ((key, values) in originalMap) {
            sanitizedMap[key] = values.map { sanitize(it) }.toTypedArray()
        }
        return sanitizedMap
    }

    // sanitize() 함수: non-null 입력을 받고, non-null 결과를 반환합니다.
    private fun sanitize(input: String): String {
        // 모든 태그 제거: 안전한 텍스트만 남김
        return Jsoup.clean(input, Safelist.none())
    }
}
