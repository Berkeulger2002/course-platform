package com.example.course_platform.service;

import com.example.course_platform.entity.Cart;
import com.example.course_platform.entity.CartItem;
import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.Order;
import com.example.course_platform.entity.OrderItem;
import com.example.course_platform.entity.Student;
import com.example.course_platform.entity.User;

import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.OrderRepository;
import com.example.course_platform.repository.StudentRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderService {


    private final OrderRepository orderRepository;

    private final CartService cartService;

    private final StudentRepository studentRepository;

    private final CourseRepository courseRepository;

    private final PaymentHistoryService paymentHistoryService;

    private final NotificationService notificationService;

    private final CurrentUserService currentUserService;


    @PersistenceContext
    private EntityManager entityManager;


    // =========================================================
    // SİPARİŞİ TAMAMLA
    // =========================================================

    @Transactional
    public Order placeOrder(
            Long studentId
    ) {


        // =====================================================
        // 1. OWNERSHIP
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        // =====================================================
        // 2. STUDENT
        // =====================================================

        Student student =
                studentRepository
                        .findById(
                                studentId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Öğrenci bulunamadı!"
                                )
                        );


        // =====================================================
        // 3. CART
        // =====================================================

        Cart cart =
                student.getCart();


        if (
                cart == null
                        ||
                        cart.getItems() == null
                        ||
                        cart.getItems().isEmpty()
        ) {

            throw new RuntimeException(
                    "Sepetiniz boş, sipariş verilemez!"
            );
        }


        List<CartItem> cartItems =
                new ArrayList<>(
                        cart.getItems()
                );


        // =====================================================
        // 4. COURSE ID'LERİNİ SIRALA
        //
        // Deadlock riskini azaltmak için bütün checkout'lar
        // kursları aynı sırada kilitler.
        // =====================================================

        List<Long> sortedCourseIds =
                cartItems
                        .stream()
                        .map(
                                item ->
                                        item
                                                .getCourse()
                                                .getId()
                        )
                        .distinct()
                        .sorted()
                        .toList();


        // =====================================================
        // 5. COURSE LOCK
        // =====================================================

        Map<Long, Course> lockedCourses =
                new LinkedHashMap<>();


        for (
                Long courseId
                :
                sortedCourseIds
        ) {


            Course lockedCourse =
                    courseRepository
                            .findByIdForUpdate(
                                    courseId
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Kurs bulunamadı!"
                                    )
                            );


            entityManager.refresh(
                    lockedCourse,
                    LockModeType.PESSIMISTIC_WRITE
            );


            lockedCourses.put(
                    courseId,
                    lockedCourse
            );
        }


        // =====================================================
        // 6. CHECKOUT KONTROLLERİ
        // =====================================================

        for (
                CartItem cartItem
                :
                cartItems
        ) {


            Long courseId =
                    cartItem
                            .getCourse()
                            .getId();


            Course course =
                    lockedCourses.get(
                            courseId
                    );


            // =================================================
            // SATIŞA AÇIK MI?
            // =================================================

            if (
                    !course.isPurchasable()
            ) {

                throw new RuntimeException(
                        "\""
                                +
                                course.getName()
                                +
                                "\" kursu şu anda satın alınamaz."
                );
            }


            // =================================================
            // KONTENJAN
            // =================================================

            if (
                    course.getMaxCapacity() > 0
                            &&
                            course.getCurrentEnrolled()
                                    >=
                                    course.getMaxCapacity()
            ) {

                throw new RuntimeException(
                        "\""
                                +
                                course.getName()
                                +
                                "\" kursunun kontenjanı dolmuştur."
                );
            }


            // =================================================
            // ZATEN SAHİP Mİ?
            // =================================================

            boolean alreadyEnrolled =
                    student
                            .getEnrolledCourses()
                            .stream()
                            .anyMatch(
                                    enrolledCourse ->
                                            enrolledCourse
                                                    .getId()
                                                    .equals(
                                                            courseId
                                                    )
                            );


            if (
                    alreadyEnrolled
            ) {

                throw new RuntimeException(
                        course.getName()
                                +
                                " kursunu zaten satın aldınız."
                );
            }
        }


        // =====================================================
        // 7. ORDER
        // =====================================================

        Order order =
                new Order();


        order.setStudent(
                student
        );


        order.setOrderCode(
                "ORD-"
                        +
                        UUID.randomUUID()
                                .toString()
                                .substring(
                                        0,
                                        8
                                )
                                .toUpperCase()
        );


        // =====================================================
        // 8. CART ITEM -> ORDER ITEM
        // =====================================================

        for (
                CartItem cartItem
                :
                cartItems
        ) {


            Long courseId =
                    cartItem
                            .getCourse()
                            .getId();


            Course course =
                    lockedCourses.get(
                            courseId
                    );


            OrderItem orderItem =
                    new OrderItem();


            orderItem.setOrder(
                    order
            );


            orderItem.setCourseId(
                    course.getId()
            );


            orderItem.setCourseName(
                    course.getName()
            );


            // =================================================
            // TEACHER SNAPSHOT
            // =================================================

            if (
                    course.getTeacher() != null
            ) {

                orderItem.setTeacherId(
                        course
                                .getTeacher()
                                .getId()
                );


                orderItem.setTeacherName(
                        course
                                .getTeacher()
                                .getName()
                );
            }


            // =================================================
            // GERÇEK SATIN ALMA FİYATI
            //
            // Kaynak:
            //
            // CartItem.priceAtAddition
            // =================================================

            BigDecimal purchasePrice =
                    cartItem.getPriceAtAddition()
                            != null

                            ?
                            cartItem.getPriceAtAddition()

                            :
                            (
                                    course.getPrice() != null

                                            ?
                                            course.getPrice()

                                            :
                                            BigDecimal.ZERO
                            );


            orderItem.setPriceAtPurchase(
                    purchasePrice
            );


            order.getItems()
                    .add(
                            orderItem
                    );
        }


        // =====================================================
        // 9. ORDER TOTAL
        // =====================================================

        BigDecimal orderTotal =
                order.getItems()
                        .stream()
                        .map(
                                OrderItem::getPriceAtPurchase
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        order.setTotalPrice(
                orderTotal
        );


        // =====================================================
        // 10. ENROLLMENT
        // =====================================================

        for (
                CartItem cartItem
                :
                cartItems
        ) {


            Long courseId =
                    cartItem
                            .getCourse()
                            .getId();


            Course course =
                    lockedCourses.get(
                            courseId
                    );


            student
                    .getEnrolledCourses()
                    .add(
                            course
                    );


            course.setCurrentEnrolled(

                    course.getCurrentEnrolled()
                            +
                            1
            );


            // =================================================
            // SON KONTENJAN
            // =================================================

            if (
                    course.getMaxCapacity() > 0
                            &&
                            course.getCurrentEnrolled()
                                    >=
                                    course.getMaxCapacity()
            ) {

                course.setPurchasable(
                        false
                );
            }
        }


        studentRepository.save(
                student
        );


        // =====================================================
        // 11. ORDER + ORDER ITEMS
        // =====================================================

        Order savedOrder =
                orderRepository.save(
                        order
                );


        // =====================================================
        // 12. PAYMENT HISTORY + NOTIFICATION
        // =====================================================

        for (
                OrderItem orderItem
                :
                savedOrder.getItems()
        ) {


            // =================================================
            // ÖDEME GEÇMİŞİ
            //
            // Artık Course.getPrice() kullanılmıyor.
            //
            // Gerçek checkout fiyatı doğrudan
            // OrderItem'tan aktarılıyor.
            // =================================================

            paymentHistoryService
                    .createPaymentRecordIfNecessary(

                            studentId,

                            orderItem.getCourseId(),

                            orderItem.getPriceAtPurchase(),

                            orderItem.getCourseName()
                    );


            // =================================================
            // TEACHER NOTIFICATION
            // =================================================

            if (
                    orderItem.getTeacherId() != null
            ) {


                String message =
                        student.getName()
                                +
                                ", \""
                                +
                                orderItem.getCourseName()
                                +
                                "\" kursunu "
                                +
                                orderItem.getPriceAtPurchase()
                                +
                                " TL tutarında satın aldı.";


                notificationService
                        .createTeacherNotificationIfNecessary(

                                orderItem.getTeacherId(),

                                "Yeni Kurs Satışı 🛒",

                                message,

                                "SALE"
                        );
            }
        }


        // =====================================================
        // 13. CART'I TEMİZLE
        // =====================================================

        cartService.emptyCart(
                studentId
        );


        return savedOrder;
    }


    // =========================================================
    // ORDER CODE
    // =========================================================

    @Transactional(readOnly = true)
    public Order getOrderForCode(
            String orderCode
    ) {


        User currentUser =
                currentUserService
                        .requireStudentRole();


        Order order =
                orderRepository
                        .findByOrderCode(
                                orderCode
                        );


        if (
                order == null
        ) {

            throw new RuntimeException(
                    "Bu koda ait bir sipariş bulunamadı!"
            );
        }


        if (
                order.getStudent() == null
                        ||
                        !Objects.equals(
                                order
                                        .getStudent()
                                        .getId(),

                                currentUser.getId()
                        )
        ) {

            throw new AccessDeniedException(
                    "Başka bir öğrencinin siparişine erişemezsiniz."
            );
        }


        return order;
    }


    // =========================================================
    // STUDENT ORDERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Order> getAllOrdersForCustomer(
            Long studentId
    ) {


        currentUserService.requireStudent(
                studentId
        );


        return orderRepository
                .findAllByStudentId(
                        studentId
                );
    }
}