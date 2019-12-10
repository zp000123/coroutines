package com.leetcode.kotlin

class Solution {
    fun subtractProductAndSum(n: Int): Int {
        if (n == 0) return 0
        var m = n
        var sum = 0
        var p = 1
        var i: Int

        while (m != 0) {
            i = m % 10
            sum += i
            if (p != 0) p *= i
            m /= 10
        }
        return p - sum
    }

    fun numJewelsInStones(J: String, S: String): Int {
        if (J.isEmpty() || S.isEmpty()) return 0
        var count = 0
        for (c in S.toCharArray()) {
            if (J.indexOf(c) != -1) count++
        }

        return count
    }

    fun defangIPaddr(address: String): String {
        val dot = '.'
        val leftB = '['
        val rightB = ']'
        val index = address.indexOf(dot)
        val index1 = address.indexOf(dot, index + 1)
        val index2 = address.lastIndexOf(dot)
        var offset = 6
        val result = CharArray(address.length + offset)

        for (i in address.length - 1 downTo 0) {
            if (i < index) {
                result[i] = address[i]
            } else if (i == index || i == index1 || i == index2) {
                result[i + offset] = rightB
                offset--
                result[i + offset] = dot
                offset--
                result[i + offset] = leftB
            } else {
                result[i + offset] = address[i]
            }
        }
        return String(result)
    }

}

fun main() {
    val s = Solution()
    print(s.numJewelsInStones("ja", "jjjjasdcaJ"))
}