package com.takanakonbu.academiamagica.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Random

data class RivalSchool(val rank: Int, val name: String, val power: BigDecimal)

object SchoolRanking {

    private val schoolNames = listOf(
        "銀月の魔術学院", "グリマーウッド研究所", "影沼の大学", "星見の塔", "翠玉の聖域",
        "アストラル・アカデミー", "賢者の石学園", "ドラゴンの血脈大学", "フェニックスの羽学寮",
        "忘却の図書館", "エレメンタル・アーツ校", "グリモワールの庭", "ルーン文字の会堂",
        "幻影の学び舎", "時の織り手ギルド", "深淵の探求者たち", "太陽の聖歌隊",
        "北風の隠れ家", "地脈の守り手", "エーテルの流れ", "混沌の渦", "調和の円卓",
        "無限の螺旋", "夜明けの先駆者", "黄昏の番人", "アルケインの礎", "クリスタル・ヴェイン校",
        "黒曜石の評議会", "霊木の根源", "アイアンウッド魔法学校", "ミスリル・ホール",
        "スターフォール学院", "フロストバイト・アカデミー", "ヴォルカニック・インスティテュート",
        "サンストーン大学", "ムーンストーンの秘密", "スカイハイの頂", "タイダル・プール",
        "ウィスパーウィンドの丘", "ストーンサークルの誓い", "エンシェント・オークの叡智",
        "ゴールデン・グリフィンの翼", "シルバー・ユニコーンの角", "ブロンズ・ゴーレムの心臓",
        "スペルウィーバーの集い", "ポーションマスターズ・ギルド", "スクロールキーパーズの会",
        "ワンドメイカーズの工房", "チャントキャスターズの円環", "アミュレット・フォージ",
        "タリスマンの結社", "オーブ・オブ・パワー", "クラウン・オブ・ソーサリー",
        "セプター・オブ・マジェスティ", "ローブ・オブ・ジ・アルキミスト", "ブーツ・オブ・トラベリング",
        "グローブ・オブ・クリエーション", "マント・オブ・イリュージョン", "サークレット・オブ・ヴィジョン"
    )

    val rivals: List<RivalSchool> = generateRivals()

    private fun generateRivals(): List<RivalSchool> {
        val schools = mutableListOf<RivalSchool>()
        var currentPower = BigDecimal("30000")
        val multiplier = BigDecimal("1.75")

        // 固定のシード値でランダム生成することで、起動ごとに名前が変わらないようにする
        val random = Random(12345L) // 固定シード
        val shuffledNames = schoolNames.shuffled(random)
        var nameIndex = 0

        // 99位から1位までを生成
        for (i in 99 downTo 1) {
            // 1位の名前は固定、他はシャッフルされたリストから順番に使う
            val name = if (i == 1) {
                "天上魔法学府アークメイジ"
            } else {
                // リストのサイズを超えたら循環して名前を再利用する
                shuffledNames[nameIndex++ % shuffledNames.size]
            }

            // 小数点以下を切り捨ててリストに追加
            schools.add(RivalSchool(rank = i, name = name, power = currentPower.setScale(0, RoundingMode.FLOOR)))

            // 次の順位の魔力を計算
            currentPower = currentPower.multiply(multiplier)
        }

        // ランク順（昇順）にソートして返す
        return schools.sortedBy { it.rank }
    }
}
