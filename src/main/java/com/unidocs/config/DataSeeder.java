package com.unidocs.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unidocs.domain.Course;
import com.unidocs.domain.Faculty;
import com.unidocs.domain.University;
import com.unidocs.repository.CourseRepository;
import com.unidocs.repository.FacultyRepository;
import com.unidocs.repository.UniversityRepository;
import com.unidocs.utils.SlugUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UniversityRepository universityRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DataSeeder(UniversityRepository universityRepository,
                      FacultyRepository facultyRepository,
                      CourseRepository courseRepository,
                      org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.universityRepository = universityRepository;
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE documents DROP CONSTRAINT IF EXISTS documents_file_type_check");
            
            // Cleanup accidentally created Faculties (e.g., Courses that became Faculties due to deep ZIP structures)
            String badFacultiesQuery = "SELECT id FROM faculties WHERE name NOT LIKE 'Khoa %' AND name NOT LIKE 'Trường %' AND name NOT LIKE 'Viện %' AND name NOT LIKE 'Bộ môn %' AND name NOT LIKE 'Môn học đại cương%' AND name != 'Khoa Khác'";
            jdbcTemplate.execute("DELETE FROM documents WHERE course_id IN (SELECT id FROM courses WHERE faculty_id IN (" + badFacultiesQuery + "))");
            jdbcTemplate.execute("DELETE FROM courses WHERE faculty_id IN (" + badFacultiesQuery + ")");
            jdbcTemplate.execute("DELETE FROM faculties WHERE id IN (" + badFacultiesQuery + ")");
        } catch (Exception e) {
            // Ignore constraint drop errors (e.g. on H2)
        }
        seedData();
    }

    private void seedData() throws Exception {
        List<University> existingUnis = universityRepository.findAll();

        University husc = getOrCreateUniversity(existingUnis, "Trường Đại học Khoa học", "HUSC", "Trường ĐHKH, Đại học Huế", "https://www.google.com/s2/favicons?domain=husc.edu.vn&sz=128", "bg-blue-600");
        University hul = getOrCreateUniversity(existingUnis, "Trường Đại học Luật", "HUL", "Trường ĐHL, Đại học Huế", "https://www.google.com/s2/favicons?domain=hul.edu.vn&sz=128", "bg-rose-600");

        List<Faculty> existingFaculties = facultyRepository.findAll();
        List<Course> existingCourses = courseRepository.findAll();

        String json = "[\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Báo chí - Truyền thông\",\n" +
                "    \"subjects\": [\n" +
                "      \"Quản trị truyền thông trong khủng hoảng\",\n" +
                "      \"Truyền thông tương tác\",\n" +
                "      \"Sáng tạo nội dung truyền thông\",\n" +
                "      \"Phương thức kể trong sản phẩm truyền thông\",\n" +
                "      \"Báo in\",\n" +
                "      \"Các phương tiện truyền thông mới\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Ngữ văn\",\n" +
                "    \"subjects\": [\n" +
                "      \"Hán Nôm căn bản\",\n" +
                "      \"Mỹ học đại cương\",\n" +
                "      \"Cơ sở Việt ngữ học\",\n" +
                "      \"Văn học dân gian\",\n" +
                "      \"Tiến trình văn học\",\n" +
                "      \"Tiếp nhận văn học\",\n" +
                "      \"Lý luận văn học nhập môn\",\n" +
                "      \"Cơ sở ngôn ngữ học\",\n" +
                "      \"Tiếng Việt thực hành\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Sinh học\",\n" +
                "    \"subjects\": [\n" +
                "      \"Nhập môn công nghệ sinh học\",\n" +
                "      \"Di truyền học\",\n" +
                "      \"Sinh học tế bào\",\n" +
                "      \"Tế bào - Mô - Phôi\",\n" +
                "      \"Tài nguyên sinh học biển\",\n" +
                "      \"Công nghệ DNA tái tổ hợp\",\n" +
                "      \"Sinh học bảo tồn\",\n" +
                "      \"Đạo đức sinh học và an toàn sinh học\",\n" +
                "      \"Vi sinh vật học\",\n" +
                "      \"Sinh học phân tử\",\n" +
                "      \"Hóa sinh học\",\n" +
                "      \"Nhập môn Hệ gen học\",\n" +
                "      \"Cơ sở dữ liệu sinh học\",\n" +
                "      \"Nhập môn Hệ chuyển hóa\",\n" +
                "      \"Quá trình và thiết bị công nghệ sinh học\",\n" +
                "      \"Sinh lý học người và động vật\",\n" +
                "      \"Công nghệ sinh học thực phẩm\",\n" +
                "      \"Kỹ thuật công nghệ sinh học\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Hóa học\",\n" +
                "    \"subjects\": [\n" +
                "      \"Hoá học phân tích\",\n" +
                "      \"Phương pháp tính\",\n" +
                "      \"Cơ sở quá trình & thiết bị công nghệ hóa học II - Quá trình truyền nhiệt\",\n" +
                "      \"Kỹ thuật nhiệt\",\n" +
                "      \"Hóa lý silicate 1\",\n" +
                "      \"Hóa lý 2\",\n" +
                "      \"Hoá học hữu cơ\",\n" +
                "      \"Hóa học đại cương\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Công nghệ thông tin\",\n" +
                "    \"subjects\": [\n" +
                "      \"Nhập môn cơ sở dữ liệu\",\n" +
                "      \"Nguyên lý hệ điều hành\",\n" +
                "      \"Phân tích và thiết kế các hệ thống thông tin\",\n" +
                "      \"Phân tích và thiết kế thuật toán\",\n" +
                "      \"Thiết kế cơ sở dữ liệu\",\n" +
                "      \"Học máy\",\n" +
                "      \"Lập trình Front-End\",\n" +
                "      \"XML và ứng dụng\",\n" +
                "      \"Mạng máy tính\",\n" +
                "      \"Kỹ thuật lập trình\",\n" +
                "      \"Quản trị dự án phần mềm\",\n" +
                "      \"Cơ sở dữ liệu\",\n" +
                "      \"Ngôn ngữ truy vấn có cấu trúc (SQL)\",\n" +
                "      \"Lập trình nâng cao\",\n" +
                "      \"Cấu trúc dữ liệu và thuật toán\",\n" +
                "      \"Java cơ bản\",\n" +
                "      \"Kiến trúc máy tính\",\n" +
                "      \"Mẫu thiết kế\",\n" +
                "      \"Kỹ nghệ phần mềm\",\n" +
                "      \"Lập trình Python\",\n" +
                "      \"Lập trình hướng đối tượng\",\n" +
                "      \"Toán học rời rạc\",\n" +
                "      \"Mạng không dây và di động\",\n" +
                "      \"Trí tuệ nhân tạo\",\n" +
                "      \"Kiểm định phần mềm\",\n" +
                "      \"Đồ họa máy tính\",\n" +
                "      \"Các hệ quản trị cơ sở dữ liệu\",\n" +
                "      \"Phân tích dữ liệu với ngôn ngữ R\",\n" +
                "      \"Độ phức tạp thuật toán\",\n" +
                "      \"Khóa luận tốt nghiệp\",\n" +
                "      \"Đồ án công nghệ phần mềm\",\n" +
                "      \"Java nâng cao\",\n" +
                "      \"Nhập môn lập trình\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Điện, Điện tử và Công nghệ vật liệu\",\n" +
                "    \"subjects\": [\n" +
                "      \"Cơ sở truyền thông số\",\n" +
                "      \"Vật lý đại cương\",\n" +
                "      \"Vật lý đại cương 2\",\n" +
                "      \"Kỹ thuật số\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Xã hội học và Công tác xã hội\",\n" +
                "    \"subjects\": [\n" +
                "      \"Xã hội học đại cương\",\n" +
                "      \"Tâm lý học đại cương\",\n" +
                "      \"Xã hội học nông thôn\",\n" +
                "      \"Xã hội học truyền thông đại chúng và dư luận xã hội\",\n" +
                "      \"Lý thuyết xã hội học hiện đại\",\n" +
                "      \"Nghiên cứu và xử lý thông tin định lượng\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Toán học\",\n" +
                "    \"subjects\": [\n" +
                "      \"Toán cao cấp 1\",\n" +
                "      \"Lý thuyết tối ưu\",\n" +
                "      \"Phương pháp tính\",\n" +
                "      \"Đại số tuyến tính\",\n" +
                "      \"Xác suất thống kê\",\n" +
                "      \"Giải tích\",\n" +
                "      \"Phép tính vi tích phân hàm một biến\",\n" +
                "      \"Khoa học dữ liệu thực hành\",\n" +
                "      \"Phân tích dữ liệ trong Excel\",\n" +
                "      \"Cơ sở toán\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Môi trường\",\n" +
                "    \"subjects\": [\n" +
                "      \"Giáo dục môi trường đại cương\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Lịch sử\",\n" +
                "    \"subjects\": [\n" +
                "      \"Văn hóa Việt Nam đại cương\",\n" +
                "      \"Lịch sử văn minh thế giới\",\n" +
                "      \"Lịch sử Việt Nam cổ trung đại từ nguyên thủy đến 1407\",\n" +
                "      \" Nhập môn nghiên cứu Trung Quốc\",\n" +
                "      \"DPH3053\",\n" +
                "      \"LIS3283\",\n" +
                "      \"Lịch sử Việt Nam đại cương\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Trường Đại học Luật\",\n" +
                "    \"subjects\": [\n" +
                "      \"Pháp luật Việt Nam đại cương\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Lý luận chính trị\",\n" +
                "    \"subjects\": [\n" +
                "      \"Tư tưởng Hồ Chí Minh\",\n" +
                "      \"Thể chế chính trị Việt Nam\",\n" +
                "      \"Chủ nghĩa xã hội khoa học\",\n" +
                "      \"Kinh tế chính trị Mác - Lênin\",\n" +
                "      \"Lịch sử Đảng Cộng sản Việt Nam\",\n" +
                "      \"Cơ sở khoa học của con đường đi lên chủ nghĩa xã hội ở Việt Nam\",\n" +
                "      \"Quản trị học\",\n" +
                "      \"Quan hệ công chúng và giao tiếp công vụ\",\n" +
                "      \"Lịch sử triết học phương Tây cổ trung đại\",\n" +
                "      \"Triết học Mác - Lênin\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"faculty\": \"Khoa Kiến trúc\",\n" +
                "    \"subjects\": [\n" +
                "      \"Hình học họa hình 2\",\n" +
                "      \"Lịch sử kiến trúc thế giới\",\n" +
                "      \"Vật lý kiến trúc\",\n" +
                "      \"Thực tập tốt nghiệp\"\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> data = mapper.readValue(json, new TypeReference<>() {});

        for (Map<String, Object> item : data) {
            String facultyName = (String) item.get("faculty");
            @SuppressWarnings("unchecked")
            List<String> subjects = (List<String>) item.get("subjects");

            University targetUni = husc;
            if (facultyName.equalsIgnoreCase("Trường Đại học Luật")) {
                targetUni = hul;
            }

            Faculty faculty = getOrCreateFaculty(existingFaculties, facultyName, SlugUtils.toSlug(facultyName), targetUni);

            for (String subjectName : subjects) {
                subjectName = subjectName.trim();
                String courseSlug = SlugUtils.toSlug(subjectName) + "-" + faculty.getId();
                createCourseIfNotExist(existingCourses, subjectName, courseSlug, faculty);
            }
        }

        seedFacultiesOnly(existingFaculties, hul, Arrays.asList(
                "Luật", "Luật kinh tế"
        ));
    }

    private University getOrCreateUniversity(List<University> existingUnis, String name, String shortName, String description, String logoUrl, String color) {
        Optional<University> found = existingUnis.stream()
                .filter(u -> u.getShortName().equalsIgnoreCase(shortName))
                .findFirst();
        if (found.isPresent()) {
            return found.get();
        }
        University university = University.builder()
                .name(name)
                .slug(SlugUtils.toSlug(name))
                .shortName(shortName)
                .description(description)
                .logoUrl(logoUrl)
                .color(color)
                .build();
        University savedUni = universityRepository.save(university);
        existingUnis.add(savedUni);
        return savedUni;
    }

    private Faculty getOrCreateFaculty(List<Faculty> existingFaculties, String name, String slug, University university) {
        Optional<Faculty> found = existingFaculties.stream()
                .filter(f -> f.getName().equalsIgnoreCase(name) && f.getUniversity().getId().equals(university.getId()))
                .findFirst();
        if (found.isPresent()) {
            return found.get();
        }
        Faculty faculty = Faculty.builder()
                .name(name)
                .slug(slug)
                .university(university)
                .build();
        Faculty savedFaculty = facultyRepository.save(faculty);
        existingFaculties.add(savedFaculty);
        return savedFaculty;
    }

    private void createCourseIfNotExist(List<Course> existingCourses, String name, String slug, Faculty faculty) {
        boolean exists = existingCourses.stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name) && c.getFaculty().getId().equals(faculty.getId()));
        if (!exists) {
            Course course = Course.builder()
                    .name(name)
                    .slug(slug)
                    .faculty(faculty)
                    .build();
            Course savedCourse = courseRepository.save(course);
            existingCourses.add(savedCourse);
        }
    }

    private void seedFacultiesOnly(List<Faculty> existingFaculties, University university, List<String> facultyNames) {
        for (String facultyName : facultyNames) {
            String facultySlug = SlugUtils.toSlug(facultyName) + "-" + university.getShortName().toLowerCase();
            getOrCreateFaculty(existingFaculties, facultyName, facultySlug, university);
        }
    }
}