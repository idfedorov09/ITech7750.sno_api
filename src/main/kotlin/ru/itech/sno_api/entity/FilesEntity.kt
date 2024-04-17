package ru.itech.sno_api.entity
import jakarta.persistence.*


@Entity
@Table(name = "files")
data class FilesEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    val fileId: Long? = null,

    @Column(name = "file_path")
    var filePath: String,

    @JoinColumn(name = "lecture_id")
    var lecture_id: Long,
)