package com.pseudowasabi.checkfile

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.math.ceil

private const val BLOCK_SIZE = 1024

data class DataInfo(
    val recordType: String,
    val recordNo: Int,
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

data class IndexAndRecordNo(
    val index: Int,
    val recordNo: Int
)

class CheckResponseFileForPostInspection(val filePath: String) {

    /**
     * process input file using FileChannel
     *
     */
    fun processFile() {
        val path = Paths.get(filePath)

        FileChannel.open(path, StandardOpenOption.READ).use { channel ->
            val fileSize = channel.size()
            require(fileSize % BLOCK_SIZE == 0L) {
                "The file size must be a multiple of $BLOCK_SIZE bytes."
            }

            val buffer = ByteBuffer.allocate(BLOCK_SIZE)
            var blockIndex = 0
            var expectedNextDataInfoBlockIndex = 1
            var currentRecordNo = 1

            while (channel.read(buffer) > 0) {
                buffer.flip()
                val blockData = ByteArray(buffer.remaining())
                buffer.get(blockData)

                // process each block
                if (blockIndex == expectedNextDataInfoBlockIndex) {
                    val requiredBlockCounts: IndexAndRecordNo = processBlock(blockData, IndexAndRecordNo(blockIndex, currentRecordNo))
                    if (requiredBlockCounts.index == -1) {
                        return
                    }
                    expectedNextDataInfoBlockIndex += requiredBlockCounts.index
                    currentRecordNo = requiredBlockCounts.recordNo
                }

                buffer.clear()
                blockIndex++
            }
        }
    }

    /**
     * process input block and return required index counts of current data block
     *
     * @param data
     * @param index
     * @return
     */
    fun processBlock(data: ByteArray, currentIndex: IndexAndRecordNo): IndexAndRecordNo {
        printBlock(data.copyOf(400))
        try {
            val dataRecord: DataInfo = extractDataInfo(data)
            println("[index: ${currentIndex.index}, recordNo: ${dataRecord.recordNo}] dataRecord.agreementDataLength + dataInfoSize: ${dataRecord.agreementDataLength + 400}")

            val requiredBlockCounts = ceil((dataRecord.agreementDataLength + 400) / BLOCK_SIZE.toDouble()).toInt()
            println("[index: ${currentIndex.index}, recordNo: ${dataRecord.recordNo}] required block counts: $requiredBlockCounts")

            return IndexAndRecordNo(requiredBlockCounts, dataRecord.recordNo)
        } catch (e: Exception) {
            println("An exception occurred at [index: ${currentIndex.index}, recordNo: ${currentIndex.recordNo}]")
            return IndexAndRecordNo(-1, currentIndex.recordNo)
        }
    }

    /**
     * extract data info (first 400 bytes of each data block)
     *
     * @param data
     * @return
     */
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

    /**
     * print ByteArray data block in String, in Hex
     *
     * @param data
     */
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
}