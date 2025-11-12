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

/**
 * ゲーム全体の状態を保持するデータクラス。
 * @param mana 現在のマナ量
 * @param gold 現在のゴールド量
 * @param totalMagicalPower 総合魔力
 * @param philosophersStones 賢者の石の所持数
 * @param departments 各学科の状態
 * @param facilities 各施設の状態
 * @param students 生徒の状態
 */
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
        // 初期状態で生徒を10人まで収容可能にするため、レベルを1に設定
        FacilityType.GREAT_HALL to FacilityState(level = 1),
        // 初期状態で学科をレベル5までアップグレード可能にするため、レベルを1に設定
        FacilityType.RESEARCH_WING to FacilityState(level = 1),
        FacilityType.DIMENSIONAL_LIBRARY to FacilityState()
    ),
    // 初期状態でリソースが生産されるよう、生徒数を3人に設定
    val students: StudentState = StudentState(totalStudents = 3)
) {
    /**
     * 研究棟のレベルに基づいて、全ての学科の最大レベルを計算する算出プロパティ。
     * 研究棟レベル1ごとに、最大レベルが5ずつ上昇する。
     * この値を変更すると、ゲーム全体の学科最大レベル計算に反映される。
     */
    val maxDepartmentLevel: Int
        get() = (facilities[FacilityType.RESEARCH_WING]?.level ?: 0) * 5
}

/**
 * 学科の種類を定義するenum。
 */
@Serializable
enum class DepartmentType {
    ATTACK_MAGIC,   // 攻撃魔法
    BOTANY,         // 植物学
    DEFENSE_MAGIC,  // 防衛魔法
    ANCIENT_MAGIC   // 古代魔術
}

/**
 * 各学科の状態を保持するデータクラス。
 * @param level 現在の学科レベル
 */
@Serializable
data class DepartmentState(
    val level: Int = 0
)

/**
 * 施設の種類を定義するenum。
 */
@Serializable
enum class FacilityType {
    GREAT_HALL,          // 大講堂
    RESEARCH_WING,       // 研究棟
    DIMENSIONAL_LIBRARY  // 次元図書館
}

/**
 * 各施設の状態を保持するデータクラス。
 * @param level 現在の施設レベル
 */
@Serializable
data class FacilityState(
    val level: Int = 0
)

/**
 * 生徒の状態を保持するデータクラス。
 * @param totalStudents 総生徒数
 * @param specializedStudents 各学科に特化した生徒数（現在は未使用）
 */
@Serializable
data class StudentState(
    val totalStudents: Int = 0,
    val specializedStudents: Map<DepartmentType, Int> = emptyMap()
)
