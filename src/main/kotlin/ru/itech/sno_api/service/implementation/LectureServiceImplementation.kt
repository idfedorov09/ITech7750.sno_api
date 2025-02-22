package ru.itech.sno_api.service.implementation

import jakarta.persistence.EntityNotFoundException
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.itech.sno_api.core.domain.LectureSpecification
import ru.itech.sno_api.dto.LectureDTO
import ru.itech.sno_api.dto.toEntity
import ru.itech.sno_api.entity.LectureEntity
import ru.itech.sno_api.entity.toDTO
import ru.itech.sno_api.repository.*
import ru.itech.sno_api.service.LectureService
import java.time.LocalDate

@Service
@Transactional
open class LectureServiceImplementation(
    private val lectureRepository: LectureRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val filesRepository: FilesRepository,
    private val forumRepository: ForumRepository
) : LectureService {

    override fun getAll(): List<LectureDTO> {
        return lectureRepository.findAll()
            .map { it.toDTO() }
    }

    override fun getById(lectureId: Long): LectureDTO {
        return lectureRepository.findById(lectureId)
            .orElseThrow { EntityNotFoundException("Lecture with ID $lectureId not found") }
            .toDTO()
    }

    override fun findByTitle(title: String): List<LectureDTO> =
        lectureRepository.findByTitle(title).map { it.toDTO() }

    override fun findByLecturer(lecturerId: Long): List<LectureDTO> =
        lectureRepository.findByLecturerUserId(lecturerId).map { it.toDTO() }

    override fun create(lecture: LectureDTO): LectureDTO {
        return lectureRepository.save(lecture.toEntity(courseRepository))
            .toDTO()
    }

    override fun update(lectureId: Long, lecture: LectureDTO): LectureDTO {
        val existingLecture = lectureRepository.findById(lectureId)
            .orElseThrow { EntityNotFoundException("Lecture with ID $lectureId not found") }

        existingLecture.title = lecture.title
        existingLecture.description = lecture.description
        existingLecture.date = lecture.date
        if (lecture.lecturer != null) existingLecture.lecturer = lecture.lecturer.toEntity(courseRepository)
        if (lecture.summary != null) existingLecture.summary = lecture.summary.toEntity()
        if (lecture.forum != null) existingLecture.forum = lecture.forum.toEntity()
        if (lecture.file != null) existingLecture.file = lecture.file.toEntity()

        return lectureRepository.save(existingLecture)
            .toDTO()
    }

    override fun delete(lectureId: Long) {
        lectureRepository.deleteById(lectureId)
    }

    override fun getAllPaginated(pageIndex: Int, pageSize: Int): List<LectureDTO> {
        return lectureRepository.findByOrderByLectureId(PageRequest.of(pageIndex, pageSize))
            .map { it.toDTO() }
    }

    override fun updateFile(lectureId: Long, fileId: Long) {
        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow { EntityNotFoundException("Lecture with ID $lectureId not found") }

        lecture.file = filesRepository.findById(fileId).orElse(null)
        lectureRepository.save(lecture)
    }

    override fun updateCourse(lectureId: Long, courseId: Long) {
        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow { EntityNotFoundException("Lecture with ID $lectureId not found") }

        lecture.course = courseRepository.findById(courseId).orElse(null)
        lectureRepository.save(lecture)
    }

    override fun updateLecturer(lectureId: Long, lecturerId: Long) {
        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow { EntityNotFoundException("Lecture with ID $lectureId not found") }

        lecture.lecturer = userRepository.findById(lecturerId).orElse(null)
        lectureRepository.save(lecture)
    }

    override fun updateDescription(lectureId: Long, description: String) {
        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow { EntityNotFoundException("Lecture with ID $lectureId not found") }

        lecture.description = description
        lectureRepository.save(lecture)
    }

    override fun updateForum(lectureId: Long, forumId: Long) {
        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow { EntityNotFoundException("Lecture with ID $lectureId not found") }

        lecture.forum = forumRepository.findById(forumId).orElse(null)
        lectureRepository.save(lecture)
    }

    override fun findAllFilteredAndSorted(
        title: String?,
        lecturerId: Long?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        sortBy: String,
        sortDirection: String
    ): List<LectureDTO> {
        val lectures = lectureRepository.findAll(
            LectureSpecification(title, lecturerId, startDate, endDate)
        )

        val direction = sortDirection?.let { if (it.equals("desc", ignoreCase = true)) "desc" else "asc" } ?: "asc"

        return lectures.sortedWith(
            when (sortBy) {
                "title" -> if (direction == "asc") compareBy<LectureEntity> { it.title } else compareByDescending { it.title }
                "date" -> if (direction == "asc") compareBy<LectureEntity> { it.date } else compareByDescending { it.date }
                else -> compareBy<LectureEntity> { it.title } // Default sorting
            }
        ).map { it.toDTO() }
    }
}
