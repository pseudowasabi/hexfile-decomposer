package com.pseudowasabi

import com.pseudowasabi.checkfile.CheckResponseFileForPostInspection

fun main() {
    print("Enter file directory: ")
    val filePath = readln()
    CheckResponseFileForPostInspection(filePath).processFile()
}

