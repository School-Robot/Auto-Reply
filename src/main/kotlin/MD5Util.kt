package tk.mcsog

import java.security.MessageDigest
import kotlin.text.StringBuilder

class MD5Util {
    companion object{
        fun encodeToHex(str: String): String{
            val hash: ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray())
            val sb: StringBuilder = StringBuilder(32)
            for (h in hash){
                var b = Integer.toHexString(h.toInt() and 0xff)
                if (b.length==1){
                    b= "0$b"
                }
                sb.append(b)
            }
            return sb.toString()
        }
    }
}