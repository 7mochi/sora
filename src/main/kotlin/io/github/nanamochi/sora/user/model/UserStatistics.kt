package io.github.nanamochi.sora.user.model

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "user_statistics")
@IdClass(UserStatisticsId::class)
class UserStatistics : PanacheEntityBase {
    @Id @Column(name = "user_id") var userId: Int = 0

    @Id @Enumerated(EnumType.ORDINAL) @Column(name = "mode") var mode: Gamemode = Gamemode.OSU

    @Column(name = "ranked_score") var rankedScore: Long = 0

    @Column(name = "total_score") var totalScore: Long = 0

    var pp: Float = 0f

    var accuracy: Float = 100f

    @Column(name = "play_count") var playCount: Int = 0

    @Column(name = "play_time") var playTime: Int = 0

    @Column(name = "total_hits") var totalHits: Long = 0

    @Column(name = "maximum_combo") var maximumCombo: Int = 0

    @Column(name = "global_rank") var globalRank: Int? = null

    @Column(name = "country_rank") var countryRank: Int? = null

    @Column(name = "grade_ss") var gradeSs: Int = 0

    @Column(name = "grade_ssh") var gradeSsh: Int = 0

    @Column(name = "grade_s") var gradeS: Int = 0

    @Column(name = "grade_sh") var gradeSh: Int = 0

    @Column(name = "grade_a") var gradeA: Int = 0
}

data class UserStatisticsId(var userId: Int = 0, var mode: Gamemode = Gamemode.OSU) : Serializable
