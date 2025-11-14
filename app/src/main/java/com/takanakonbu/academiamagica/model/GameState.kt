package com.takanakonbu.academiamagica.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.math.RoundingMode

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
 */
@Serializable
data class GameState(
    val schoolName: String = "",
    @Serializable(with = BigDecimalSerializer::class)
    val mana: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalSerializer::class)
    val gold: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalSerializer::class)
    val totalMagicalPower: BigDecimal = BigDecimal.ZERO,
    val philosophersStones: Long = 0,
    val departments: Map<DepartmentType, DepartmentState> = DepartmentType.values().associateWith { DepartmentState() },
    val facilities: Map<FacilityType, FacilityState> = FacilityType.values().associateWith { if (it == FacilityType.GREAT_HALL || it == FacilityType.RESEARCH_WING) FacilityState(level = 1) else FacilityState() },
    val students: StudentState = StudentState(totalStudents = 3),
    val prestigeSkills: Map<PrestigeSkillType, PrestigeSkillState> = PrestigeSkillType.values().associateWith { PrestigeSkillState() },
    val boostRemainingSeconds: Int = 0,
    val lastOnlineTimestamp: Long = System.currentTimeMillis()
) {
    /**
     * 研究棟のレベルに基づいて、全ての学科の最大レベルを計算する算出プロパティ。
     */
    val maxDepartmentLevel: Int
        get() = (facilities[FacilityType.RESEARCH_WING]?.level ?: 0) * 5

    private val rewardBonusMultiplier: BigDecimal
        get() {
            val magicCreatureStudents = students.specializedStudents[DepartmentType.MAGIC_CREATURE_STUDIES] ?: 0
            val breedingHutLevel = facilities[FacilityType.BREEDING_HUT]?.level ?: 0
            val rewardSkillLevel = prestigeSkills[PrestigeSkillType.REWARD_BOOST]?.level ?: 0

            val studentBonus = BigDecimal(magicCreatureStudents).multiply(BigDecimal("0.005"))
            val facilityBonus = BigDecimal(breedingHutLevel).multiply(BigDecimal("0.01"))
            val skillBonus = BigDecimal(rewardSkillLevel).multiply(BigDecimal("0.10"))

            return BigDecimal.ONE + studentBonus + facilityBonus + skillBonus
        }

    val manaGoldRewardMultiplier: BigDecimal
        get() = BigDecimal(2).multiply(rewardBonusMultiplier)

    val productionBoostMultiplier: BigDecimal
        get() = BigDecimal(4).multiply(rewardBonusMultiplier)

    /**
     * 毎秒のマナ生産量を計算する。すべてのボーナス（学科レベル、生徒配属、超越スキル）を含む。
     */
    val manaPerSecond: BigDecimal
        get() {
            val botanyStudentBonus = BigDecimal.ONE + (students.specializedStudents[DepartmentType.BOTANY]?.toBigDecimal()?.multiply(BigDecimal("0.05")) ?: BigDecimal.ZERO)
            val manaBoost = BigDecimal.ONE + (prestigeSkills[PrestigeSkillType.MANA_BOOST]?.level?.toBigDecimal()?.multiply(BigDecimal("0.3")) ?: BigDecimal.ZERO)
            val botanyMultiplier = BigDecimal.ONE + (departments[DepartmentType.BOTANY]?.level?.toBigDecimal()?.multiply(BigDecimal("0.1")) ?: BigDecimal.ZERO)
            val baseProduction = students.totalStudents.toBigDecimal().multiply(botanyMultiplier)
            var finalProduction = baseProduction.multiply(botanyStudentBonus).multiply(manaBoost)
            if (boostRemainingSeconds > 0) {
                finalProduction = finalProduction.multiply(productionBoostMultiplier)
            }
            return finalProduction
        }

    /**
     * 毎秒のゴールド生産量を計算する。すべてのボーナス（学科レベル、生徒配属、超越スキル）を含む。
     */
    val goldPerSecond: BigDecimal
        get() {
            val botanyStudentBonus = BigDecimal.ONE + (students.specializedStudents[DepartmentType.BOTANY]?.toBigDecimal()?.multiply(BigDecimal("0.05")) ?: BigDecimal.ZERO)
            val goldBoost = BigDecimal.ONE + (prestigeSkills[PrestigeSkillType.GOLD_BOOST]?.level?.toBigDecimal()?.multiply(BigDecimal("0.3")) ?: BigDecimal.ZERO)
            val botanyMultiplier = BigDecimal.ONE + (departments[DepartmentType.BOTANY]?.level?.toBigDecimal()?.multiply(BigDecimal("0.1")) ?: BigDecimal.ZERO)
            val baseProduction = students.totalStudents.toBigDecimal().multiply(botanyMultiplier)
            var finalProduction = baseProduction.divide(BigDecimal(2), 2, RoundingMode.HALF_UP).multiply(botanyStudentBonus).multiply(goldBoost)
            if (boostRemainingSeconds > 0) {
                finalProduction = finalProduction.multiply(productionBoostMultiplier)
            }
            return finalProduction
        }
}

@Serializable
enum class DepartmentType {
    // 生徒配属効果: 1人につき総合魔力の基礎値+5
    ATTACK_MAGIC,
    // 生徒配属効果: 1人につきマナ・ゴールド生産量+5% (乗算)
    BOTANY,
    // 生徒配属効果: 1人につき総合魔力+1% (乗算)
    DEFENSE_MAGIC,
    // 生徒配属効果: 1人につき賢者の石獲得量+1% (乗算)
    ANCIENT_MAGIC,
    // 生徒配属効果: 1人につきリワードボーナス+0.5% (加算)
    MAGIC_CREATURE_STUDIES
}

@Serializable
data class DepartmentState(val level: Int = 0)

@Serializable
enum class FacilityType {
    GREAT_HALL, RESEARCH_WING, DIMENSIONAL_LIBRARY, BREEDING_HUT
}

@Serializable
data class FacilityState(val level: Int = 0)

@Serializable
data class StudentState(
    val totalStudents: Int = 0,
    val specializedStudents: Map<DepartmentType, Int> = DepartmentType.values().associateWith { 0 }
) {
    val unassignedStudents: Int
        get() = totalStudents - specializedStudents.values.sum()
}

/**
 * 超越（プレステージ）アップグレードの種類を定義するenum。
 */
@Serializable
enum class PrestigeSkillType {
    MANA_BOOST,          // マナ生産量ボーナス
    GOLD_BOOST,          // ゴールド生産量ボーナス
    RESEARCH_DISCOUNT,   // 学科研究コスト割引
    FACILITY_DISCOUNT,   // 施設改築コスト割引
    STONE_BOOST,          // 賢者の石獲得量ボーナス
    OFFLINE_TIME_EXTENSION, // 放置可能時間延長
    MAGICAL_POWER_BOOST, // 永続的な総合魔力ボーナス
    REWARD_BOOST // リワード広告ボーナス
}

/**
 * 各超越スキルの状態を保持するデータクラス。
 * @param level 現在のスキルレベル
 */
@Serializable
data class PrestigeSkillState(val level: Int = 0)
