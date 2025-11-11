package com.takanakonbu.academiamagica.model

import java.math.BigDecimal

/**
 * ゲーム全体の状態を保持するデータクラス。
 *
 * @param mana 現在のマナ保有量。
 * @param gold 現在のゴールド保有量。
 * @param totalMagicalPower 学校の総合魔力。ゲームの主要な指標。
 * @param philosophersStones 賢者の石の数。周回リセット時に得られる永続ボーナス。
 * @param departments 各学科の状態。
 * @param facilities 各施設の状態。
 * @param students 生徒に関する状態。
 */
data class GameState(
    // リソース
    val mana: BigDecimal = BigDecimal.ZERO,
    val gold: BigDecimal = BigDecimal.ZERO,

    // 主要指標
    val totalMagicalPower: BigDecimal = BigDecimal.ZERO,

    // 周回要素
    val philosophersStones: Long = 0,

    // 育成カテゴリ
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
)

/**
 * 学科の種類。
 */
enum class DepartmentType {
    ATTACK_MAGIC,   // 攻撃魔法
    BOTANY,         // 植物学
    DEFENSE_MAGIC,  // 防衛魔法
    ANCIENT_MAGIC   // 古代魔術
}

/**
 * 学科の状態。
 *
 * @param level 現在のレベル。
 */
data class DepartmentState(
    val level: Int = 0
)

/**
 * 施設の種類。
 */
enum class FacilityType {
    GREAT_HALL,          // 大講堂
    RESEARCH_WING,       // 研究棟
    DIMENSIONAL_LIBRARY  // 次元図書館
}

/**
 * 施設の状態。
 *
 * @param level 現在のレベル。
 */
data class FacilityState(
    val level: Int = 0
)

/**
 * 生徒に関する状態。
 *
 * @param totalStudents 総生徒数。
 * @param specializedStudents 特定学科の特待生数。キーはDepartmentType。
 */
data class StudentState(
    val totalStudents: Int = 0,
    val specializedStudents: Map<DepartmentType, Int> = emptyMap()
)
