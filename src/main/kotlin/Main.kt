package com.pseudowasabi

import com.pseudowasabi.checkfile.CheckResponseFileForPostInspection

fun main() {
    val filePath = readln()
    CheckResponseFileForPostInspection(filePath).processFile()
}

