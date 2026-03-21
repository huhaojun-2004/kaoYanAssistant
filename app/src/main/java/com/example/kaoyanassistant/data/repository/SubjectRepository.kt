package com.example.kaoyanassistant.data.repository

import com.example.kaoyanassistant.data.local.dao.SubjectDao
import com.example.kaoyanassistant.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

class SubjectRepository(
    private val subjectDao: SubjectDao,
) {
    val subjects: Flow<List<SubjectEntity>> = subjectDao.observeAll()

    suspend fun ensureDefaults() {
        if (subjectDao.count() > 0) {
            subjectDao.getAll().forEach { subject ->
                val normalizedName = normalizeSubjectName(subject.name)
                if (normalizedName != subject.name) {
                    subjectDao.update(subject.copy(name = normalizedName))
                }
            }
            return
        }

        subjectDao.insertAll(
            listOf(
                SubjectEntity(
                    name = "数学",
                    colorValue = 0xFF28594A,
                    sortOrder = 0,
                    isDefault = true,
                ),
                SubjectEntity(
                    name = "英语",
                    colorValue = 0xFF7AA7C7,
                    sortOrder = 1,
                    isDefault = true,
                ),
                SubjectEntity(
                    name = "政治",
                    colorValue = 0xFF9D4C4C,
                    sortOrder = 2,
                    isDefault = true,
                ),
                SubjectEntity(
                    name = "算法",
                    colorValue = 0xFFB06C2B,
                    sortOrder = 3,
                    isDefault = true,
                ),
                SubjectEntity(
                    name = "计组",
                    colorValue = 0xFFBC8540,
                    sortOrder = 4,
                    isDefault = true,
                ),
                SubjectEntity(
                    name = "OS",
                    colorValue = 0xFFD6A24C,
                    sortOrder = 5,
                    isDefault = true,
                ),
                SubjectEntity(
                    name = "计网",
                    colorValue = 0xFFE0B76E,
                    sortOrder = 6,
                    isDefault = true,
                ),
            ),
        )
    }

    suspend fun getById(id: Long): SubjectEntity? = subjectDao.getById(id)

    suspend fun save(id: Long?, name: String, parentId: Long?, colorValue: Long?) {
        val trimmedName = normalizeSubjectName(name.trim())
        if (trimmedName.isEmpty()) return

        if (id == null) {
            val nextOrder = subjectDao.getMaxSortOrder(parentId = null) + 1
            subjectDao.insert(
                SubjectEntity(
                    name = trimmedName,
                    parentId = null,
                    colorValue = colorValue ?: defaultColor(nextOrder),
                    sortOrder = nextOrder,
                ),
            )
        } else {
            val existing = subjectDao.getById(id) ?: return
            subjectDao.update(
                existing.copy(
                    name = trimmedName,
                    parentId = null,
                    colorValue = colorValue ?: existing.colorValue,
                ),
            )
        }
    }

    suspend fun delete(id: Long) {
        val subject = subjectDao.getById(id) ?: return
        subjectDao.delete(subject)
    }

    private fun defaultColor(index: Int): Long {
        val palette = listOf(
            0xFF28594A,
            0xFF7AA7C7,
            0xFF9D4C4C,
            0xFFD9A441,
            0xFF5874A2,
            0xFF5F9167,
        )
        return palette[index % palette.size]
    }

    private fun normalizeSubjectName(name: String): String {
        return when (name) {
            "计算机组成原理" -> "计组"
            "计算机网络" -> "计网"
            "计算机操作系统" -> "OS"
            "操作系统" -> "OS"
            "算法与数据结构" -> "算法"
            "数据结构" -> "算法"
            else -> name
        }
    }
}
