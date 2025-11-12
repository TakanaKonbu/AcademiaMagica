package com.takanakonbu.academiamagica.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }
    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }
}

@Serializable
data class GameState(
    @Serializable(with = BigDecimalSerializer::class)
    val mana: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalSerializer::class)
    val gold: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalSerializer::class)
    val totalMagicalPower: BigDecimal = BigDecimal.ZERO,
    val philosophersStones: Long = 0,
    val departments: Map<DepartmentType, DepartmentState> = mapOf(
        DepartmentType.ATTACK_MAGIC to DepartmentState(),
        DepartmentType.BOTANY to DepartmentState(),
        DepartmentType.DEFENSE_MAGIC to DepartmentState(),
        DepartmentType.ANCIENT_MAGIC to DepartmentState()
    ),
    val facilities: Map<FacilityType, FacilityState> = mapOf(
        FacilityType.GREAT_HALL to FacilityState(),
        FacilityType.RESEARCH_WING to FacilityState(),
        FacilityType.DIMENSIONAL_LIBRARY to FacilityState()
    ),
    val students: StudentState = StudentState()
) {
    val maxDepartmentLevel: Int
        get() = (facilities[FacilityType.RESEARCH_WING]?.level ?: 0) * 5
}

@Serializable
enum class DepartmentType {
    ATTACK_MAGIC,   // 攻撃魔法
    BOTANY,         // 植物学
    DEFENSE_MAGIC,  // 防衛魔法
    ANCIENT_MAGIC   // 古代魔術
}

@Serializable
data class DepartmentState(
    val level: Int = 0
)

@Serializable
enum class FacilityType {
    GREAT_HALL,          // 大講堂
    RESEARCH_WING,       // 研究棟
    DIMENSIONAL_LIBRARY  // 次元図書館
}

@Serializable
data class FacilityState(
    val level: Int = 0
)

@Serializable
data class StudentState(
    val totalStudents: Int = 0,
    val specializedStudents: Map<DepartmentType, Int> = emptyMap()
)
