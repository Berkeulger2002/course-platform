package com.example.course_platform.config;


import com.example.course_platform.entity.Cart;
import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.Student;
import com.example.course_platform.entity.Teacher;
import com.example.course_platform.entity.User;

import com.example.course_platform.repository.CartRepository;
import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.StudentRepository;
import com.example.course_platform.repository.TeacherRepository;
import com.example.course_platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;


import java.math.BigDecimal;


@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {


    // =========================================================
    // REPOSITORY
    // =========================================================

    private final TeacherRepository teacherRepository;

    private final StudentRepository studentRepository;

    private final CourseRepository courseRepository;

    private final CartRepository cartRepository;

    private final UserRepository userRepository;


    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // APPLICATION START
    // =========================================================

    @Override
    public void run(String... args) {


        // =====================================================
        // ESKİ DÜZ METİN ŞİFRELERİ BCRYPT'E ÇEVİR
        // =====================================================
        //
        // Eski sistemde:
        //
        // 12345
        // 123456
        //
        // gibi şifreler doğrudan veritabanında tutuluyordu.
        //
        // Yeni sistemde bunları:
        //
        // $2a$10$....
        //
        // şeklinde BCrypt hash'e dönüştürüyoruz.
        //
        // Kullanıcının gerçek şifresi değişmez.
        //
        // Örneğin:
        //
        // osman@gmail.com
        // 12345
        //
        // ile giriş yapmaya devam edebilir.
        //
        // =====================================================

        migrateExistingPasswords();


        // =====================================================
        // MOCK DATA
        // =====================================================
        //
        // Veritabanında hiç öğretmen yoksa
        // örnek veriler oluşturulur.
        //
        // =====================================================

        if (teacherRepository.count() == 0) {


            // =================================================
            // 1. ÖĞRETMEN
            // =================================================

            Teacher teacher1 = new Teacher();

            teacher1.setName("Engin Demiroğ");

            teacher1.setEmail(
                    "engin@gmail.com"
            );

            teacher1.setPassword(
                    passwordEncoder.encode("123456")
            );

            teacher1.setRole(
                    Role.TEACHER
            );

            teacherRepository.save(
                    teacher1
            );


            Teacher teacher2 = new Teacher();

            teacher2.setName(
                    "Atıl Samancıoğlu"
            );

            teacher2.setEmail(
                    "atil@gmail.com"
            );

            teacher2.setPassword(
                    passwordEncoder.encode("123456")
            );

            teacher2.setRole(
                    Role.TEACHER
            );

            teacherRepository.save(
                    teacher2
            );


            // =================================================
            // 2. ÖĞRENCİLER
            // =================================================

            Student student1 = new Student();

            student1.setName(
                    "Osman Berke Ülger"
            );

            student1.setEmail(
                    "osmanberke@gmail.com"
            );

            student1.setPassword(
                    passwordEncoder.encode("123456")
            );

            student1.setRole(
                    Role.STUDENT
            );

            studentRepository.save(
                    student1
            );


            // =================================================
            // ÖĞRENCİ SEPETİ
            // =================================================

            Cart cart1 = new Cart();

            cart1.setStudent(
                    student1
            );

            cartRepository.save(
                    cart1
            );


            Student student2 = new Student();

            student2.setName(
                    "Ayşe Yılmaz"
            );

            student2.setEmail(
                    "ayse@gmail.com"
            );

            student2.setPassword(
                    passwordEncoder.encode("123456")
            );

            student2.setRole(
                    Role.STUDENT
            );

            studentRepository.save(
                    student2
            );


            Cart cart2 = new Cart();

            cart2.setStudent(
                    student2
            );

            cartRepository.save(
                    cart2
            );


            // =================================================
            // 3. KURSLAR
            // =================================================

            Course course1 = new Course();

            course1.setName(
                    "Java ve Spring Boot Geliştirici Kampı"
            );

            course1.setPrice(
                    new BigDecimal("1250.00")
            );

            course1.setMaxCapacity(
                    50
            );

            course1.setPurchasable(
                    true
            );

            course1.setCurrentEnrolled(
                    0
            );

            course1.setTeacher(
                    teacher1
            );

            courseRepository.save(
                    course1
            );


            Course course2 = new Course();

            course2.setName(
                    "Kotlin ile Android Geliştirme"
            );

            course2.setPrice(
                    new BigDecimal("900.00")
            );

            course2.setMaxCapacity(
                    30
            );

            course2.setPurchasable(
                    true
            );

            course2.setCurrentEnrolled(
                    0
            );

            course2.setTeacher(
                    teacher2
            );

            courseRepository.save(
                    course2
            );


            Course course3 = new Course();

            course3.setName(
                    "C# ve .NET Core Mimari Tasarım"
            );

            course3.setPrice(
                    new BigDecimal("1500.00")
            );

            course3.setMaxCapacity(
                    20
            );

            course3.setPurchasable(
                    true
            );

            course3.setCurrentEnrolled(
                    0
            );

            course3.setTeacher(
                    teacher1
            );

            courseRepository.save(
                    course3
            );


            System.out.println(
                    "Mock veriler BCrypt şifrelerle başarıyla oluşturuldu."
            );
        }
    }


    // =========================================================
    // ESKİ PASSWORD MIGRATION
    // =========================================================

    private void migrateExistingPasswords() {


        for (User user : userRepository.findAll()) {


            String currentPassword =
                    user.getPassword();


            if (
                    currentPassword != null
                            &&
                            !currentPassword.isBlank()
                            &&
                            !isBcryptPassword(currentPassword)
            ) {


                user.setPassword(
                        passwordEncoder.encode(
                                currentPassword
                        )
                );


                userRepository.save(
                        user
                );


                System.out.println(
                        "Şifre BCrypt'e dönüştürüldü: "
                                + user.getEmail()
                );
            }
        }
    }


    // =========================================================
    // BCRYPT KONTROLÜ
    // =========================================================

    private boolean isBcryptPassword(
            String password
    ) {


        return password.startsWith("$2a$")
                ||
                password.startsWith("$2b$")
                ||
                password.startsWith("$2y$");
    }
}