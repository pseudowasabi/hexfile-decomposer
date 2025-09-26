package com.pseudowasabi

import com.pseudowasabi.checkfile.CheckResponseFileForPostInspection

fun main(args: Array<String>) {
//    print("Enter file directory: ")
//    val filePath = readln()
    val filePath = args[0]
    CheckResponseFileForPostInspection(filePath).processFile()
}