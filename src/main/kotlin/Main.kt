package com.pseudowasabi

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.math.ceil

private const val BLOCK_SIZE = 1024

data class DataInfo(
    val recordType: String,
    val order: Int,
    val requestType: String,
    val crn: Int,
    val orgCode: String,
    val spareField1: String,
    val payerNo: String,
    val finCorpCode: String,
    val accountNo: String,
    val requestDate: Int,
    val agreementDataType: String,
    val agreementDataExists: String,
    val agreementDataFileExtension: String,
    val agreementDataLength: Int,
    val spareField2: String
)
val fieldLengths = listOf(1, 10, 1, 10, 20, 10, 30, 3, 20, 8, 1, 1, 4, 7, 274)

fun main() {
    val filePath = readln()
    processFile(filePath)
}

fun processFile(filePath: String) {
    val path = Paths.get(filePath)
    
    FileChannel.open(path, StandardOpenOption.READ).use { channel ->
        val fileSize = channel.size()
        require(fileSize % BLOCK_SIZE == 0L) {
            "파일 크기가 ${BLOCK_SIZE}로 나누어떨어지지 않습니다."
        }
        
        val buffer = ByteBuffer.allocate(BLOCK_SIZE)
        var blockIndex = 0
        
        while (channel.read(buffer) > 0) {
            buffer.flip()
            val blockData = ByteArray(buffer.remaining())
            buffer.get(blockData)
            
            // 여기에 각 블록 처리 로직 추가
            processBlock(blockData, blockIndex)
            
            buffer.clear()
            blockIndex++
        }
    }
}

fun processBlock(data: ByteArray, index: Int) {

    if (index == 0) {
        println()
        printBlock(data)
    } else if (index == 1) {
        printBlock(data.copyOf(400))

        val dataRecord: DataInfo = extractDataInfo(data)
        println("dataRecord.agreementDataLength + dataInfoSize: ${dataRecord.agreementDataLength + 400}")

        val requiredBlockCounts = ceil((dataRecord.agreementDataLength + 400) / BLOCK_SIZE.toDouble()).toInt()
        println("required block counts: $requiredBlockCounts")
    }
}

fun extractDataInfo(data: ByteArray): DataInfo {
    val fields = mutableListOf<String>()
    var offset = 0
    for (len in fieldLengths) {
        val slice = data.copyOfRange(offset, offset + len)
        fields += slice.toString(Charsets.UTF_8).trim()
        offset += len
    }

    return DataInfo(
        fields[0],
        Integer.parseInt(fields[1]),
        fields[2],
        Integer.parseInt(fields[3]),
        fields[4],
        fields[5],
        fields[6],
        fields[7],
        fields[8],
        Integer.parseInt(fields[9]),
        fields[10],
        fields[11],
        fields[12],
        Integer.parseInt(fields[13]),
        fields[14]
    )
}

fun printBlock(data: ByteArray) {
    val stringContent = String(data, Charsets.UTF_8)
    println("in String: [$stringContent]")

    println("in Hex (first 64 bytes):")
    for (i in 0 until minOf(64, data.size)) {
        print("%02X ".format(data[i]))
        if ((i + 1) % 16 == 0) println()
    }
    if (data.size > 64) println("...")
}