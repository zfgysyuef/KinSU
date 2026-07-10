package com.mikokernel.ui.screen.kpm

import com.mikokernel.data.model.KpmFileMetadata
import com.mikokernel.ui.util.isValidKpmModuleId
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val ELF_HEADER_SIZE = 64
private const val ELF64_SECTION_HEADER_SIZE = 64
private const val ELF64_SYMBOL_SIZE = 24L
private const val MAX_KPM_SIZE = 32 * 1024 * 1024
private const val SHT_PROGBITS = 1
private const val SHT_SYMTAB = 2
private const val SHT_STRTAB = 3
private const val SHT_NOBITS = 8
private const val SHF_ALLOC = 0x2L

private data class ElfSection(
    val name: String,
    val type: Int,
    val flags: Long,
    val size: Long,
    val link: Int,
    val entrySize: Long,
    val fileRange: IntRange?,
)

fun inspectKpmFile(file: File): Result<KpmFileMetadata> = runCatching {
    require(file.isFile) { "KPM file does not exist" }
    require(file.length() in ELF_HEADER_SIZE.toLong()..MAX_KPM_SIZE.toLong()) {
        "KPM file size is invalid"
    }

    val bytes = file.readBytes()
    require(
        bytes[0] == 0x7f.toByte() &&
            bytes[1] == 'E'.code.toByte() &&
            bytes[2] == 'L'.code.toByte() &&
            bytes[3] == 'F'.code.toByte()
    ) { "File is not an ELF object" }
    require(bytes[4].toInt() == 2) { "KPM must be ELF64" }
    require(bytes[5].toInt() == 1) { "KPM must use little-endian encoding" }

    val elf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    require(elf.getShort(16).toInt() and 0xffff == 1) { "KPM must be ET_REL" }
    require(elf.getShort(18).toInt() and 0xffff == 183) { "KPM must target AArch64" }

    val sectionOffset = elf.getLong(40)
    val sectionEntrySize = elf.getShort(58).toInt() and 0xffff
    val sectionCount = elf.getShort(60).toInt() and 0xffff
    val stringTableIndex = elf.getShort(62).toInt() and 0xffff

    require(sectionOffset >= 0) { "Invalid ELF section table" }
    require(sectionEntrySize == ELF64_SECTION_HEADER_SIZE) { "Invalid ELF section size" }
    require(sectionCount in 1..4096) { "Invalid ELF section count" }
    require(stringTableIndex in 0 until sectionCount) { "Invalid ELF string table" }

    fun checkedRange(offset: Long, size: Long): IntRange {
        require(offset >= 0 && size >= 0 && offset <= bytes.size.toLong()) {
            "ELF section range is invalid"
        }
        val end = offset + size
        require(end >= offset && end <= bytes.size.toLong()) { "ELF section exceeds file" }
        return offset.toInt() until end.toInt()
    }

    fun sectionHeader(index: Int): Int {
        val offset = sectionOffset + index.toLong() * sectionEntrySize
        checkedRange(offset, ELF64_SECTION_HEADER_SIZE.toLong())
        return offset.toInt()
    }

    val stringHeader = sectionHeader(stringTableIndex)
    require(elf.getInt(stringHeader + 4) == SHT_STRTAB) { "Invalid ELF string table type" }
    val stringRange = checkedRange(
        elf.getLong(stringHeader + 24),
        elf.getLong(stringHeader + 32)
    )
    require(!stringRange.isEmpty()) { "ELF string table is empty" }

    fun readCString(range: IntRange, relativeOffset: Int): String {
        require(relativeOffset >= 0 && relativeOffset < range.count()) {
            "Invalid ELF string offset"
        }
        val start = range.first + relativeOffset
        var end = start
        while (end <= range.last && bytes[end] != 0.toByte()) end++
        require(end <= range.last) { "Unterminated ELF string" }
        return bytes.copyOfRange(start, end).toString(Charsets.UTF_8)
    }

    val sections = ArrayList<ElfSection>(sectionCount)
    repeat(sectionCount) { index ->
        val header = sectionHeader(index)
        val nameOffset = elf.getInt(header).toLong() and 0xffff_ffffL
        require(nameOffset <= Int.MAX_VALUE) { "Invalid ELF string offset" }
        val name = if (nameOffset == 0L) "" else readCString(stringRange, nameOffset.toInt())
        val type = elf.getInt(header + 4)
        val offset = elf.getLong(header + 24)
        val size = elf.getLong(header + 32)
        val link = elf.getInt(header + 40).toLong() and 0xffff_ffffL
        require(link <= Int.MAX_VALUE) { "Invalid ELF section link" }

        sections += ElfSection(
            name = name,
            type = type,
            flags = elf.getLong(header + 8),
            size = size,
            link = link.toInt(),
            entrySize = elf.getLong(header + 56),
            // SHT_NOBITS reserves memory but has no bytes in the ELF file.
            fileRange = if (type == SHT_NOBITS) null else checkedRange(offset, size),
        )
    }

    fun requiredKpmSection(name: String, minimumSize: Long): ElfSection {
        val matches = sections.filter { it.name == name }
        require(matches.size == 1) { "Missing or duplicate $name section" }
        return matches.single().also { section ->
            require(section.type == SHT_PROGBITS) { "$name must be PROGBITS" }
            require(section.flags and SHF_ALLOC != 0L) { "$name must be allocated" }
            require(section.size >= minimumSize) { "$name section is too small" }
            require(section.fileRange != null) { "$name has no file contents" }
        }
    }

    requiredKpmSection(".kpm.init", 8L)
    requiredKpmSection(".kpm.exit", 8L)
    val infoRange = requiredKpmSection(".kpm.info", 1).fileRange!!

    val symbolTable = sections.firstOrNull { it.type == SHT_SYMTAB }
        ?: error("KPM has no symbol table")
    require(
        symbolTable.entrySize == ELF64_SYMBOL_SIZE &&
            symbolTable.size >= ELF64_SYMBOL_SIZE &&
            symbolTable.size % ELF64_SYMBOL_SIZE == 0L
    ) { "Invalid ELF symbol table" }
    require(symbolTable.link in sections.indices) { "Invalid ELF symbol string table" }
    require(sections[symbolTable.link].type == SHT_STRTAB) {
        "Invalid ELF symbol string table"
    }

    val properties = mutableMapOf<String, String>()
    var cursor = infoRange.first
    while (cursor <= infoRange.last) {
        var end = cursor
        while (end <= infoRange.last && bytes[end] != 0.toByte()) end++
        require(end <= infoRange.last) { "Unterminated KPM metadata entry" }
        if (end > cursor) {
            val value = bytes.copyOfRange(cursor, end).toString(Charsets.UTF_8)
            val separator = value.indexOf('=')
            if (separator > 0) {
                val key = value.substring(0, separator)
                require(key !in properties) { "Duplicate KPM metadata key: $key" }
                properties[key] = value.substring(separator + 1)
            }
        }
        cursor = end + 1
    }

    val id = properties["name"].orEmpty()
    require(isValidKpmModuleId(id)) { "KPM module name is missing or unsafe" }
    require(properties["version"].orEmpty().isNotBlank()) { "KPM version is missing" }

    KpmFileMetadata(
        id = id,
        version = properties["version"].orEmpty(),
        license = properties["license"].orEmpty(),
        author = properties["author"].orEmpty(),
        description = properties["description"].orEmpty(),
        args = properties["args"].orEmpty(),
    )
}
