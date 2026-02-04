package com.example.visualmoney.core


actual fun getCountries(): List<Country> {
    return emptyList()
}


//private fun codePointToString(codePoint: Int): String {
//    return if (codePoint <= 0xFFFF) {
//        codePoint.toChar().toString()
//    } else {
//        val cp = codePoint - 0x10000
//        val highSurrogate = 0xD800 + (cp shr 10)
//        val lowSurrogate = 0xDC00 + (cp and 0x3FF)
//        "${highSurrogate.toChar()}${lowSurrogate.toChar()}"
//    }
//}
//
//fun String.toFlagEmoji(): String {
//    return ""
////    val cc = this.trim().uppercase()
////    if (cc.length != 2) return ""
////
////    val base = 0x1F1E6
////    val a = 'A'.code
////
////    val first = base + (cc[0].code - a)
////    val second = base + (cc[1].code - a)
////
////    return codePointToString(first) + codePointToString(second)
//}