package com.example.course_platform.service;

import com.example.course_platform.dto.CartItemResponse;
import com.example.course_platform.dto.CartResponse;
import com.example.course_platform.dto.PublicCourseResponse;

import com.example.course_platform.entity.Cart;
import com.example.course_platform.entity.CartItem;
import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.Student;

import com.example.course_platform.repository.CartItemRepository;
import com.example.course_platform.repository.CartRepository;
import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    private final CourseRepository courseRepository;

    private final StudentRepository studentRepository;

    private final CartItemRepository cartItemRepository;

    private final CurrentUserService currentUserService;


    // =========================================================
    // SEPETİ GETİR
    //
    // Entity doğrudan controller'a dönmez.
    // Transaction açıkken DTO oluşturulur.
    // =========================================================

    @Transactional
    public CartResponse getCart(
            Long studentId) {

        Cart cart =
                getOrCreateCartEntity(
                        studentId
                );


        return toResponse(
                cart
        );
    }


    // =========================================================
    // KURSU SEPETE EKLE
    // =========================================================

    @Transactional
    public CartResponse addCourseToCart(
            Long studentId,
            Long courseId) {

        Cart cart =
                getOrCreateCartEntity(
                        studentId
                );


        Course course =
                courseRepository
                        .findById(
                                courseId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Kurs bulunamadı."
                                )
                        );


        // =====================================================
        // SATIN ALINABİLİR Mİ?
        //
        // maxCapacity = 0
        // sınırsız kapasite anlamına gelir.
        // =====================================================

        if (
                !course.isPurchasable()
                        ||
                        (
                                course.getMaxCapacity() > 0
                                        &&
                                        course.getCurrentEnrolled()
                                                >=
                                                course.getMaxCapacity()
                        )
        ) {

            throw new IllegalArgumentException(
                    "Bu kursun kontenjanı dolmuştur veya şu anda satışa kapalıdır."
            );
        }


        // =====================================================
        // ÖĞRENCİ KURSA ZATEN SAHİP Mİ?
        // =====================================================

        boolean alreadyOwned =
                cart.getStudent()
                        .getEnrolledCourses()
                        !=
                        null
                        &&
                        cart.getStudent()
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
                alreadyOwned
        ) {

            throw new IllegalArgumentException(
                    "Bu kursu zaten satın aldınız."
            );
        }


        // =====================================================
        // KURS ZATEN SEPETTE Mİ?
        // =====================================================

        boolean alreadyInCart =
                cart.getItems()
                        .stream()
                        .anyMatch(
                                item ->
                                        item.getCourse()
                                                .getId()
                                                .equals(
                                                        courseId
                                                )
                        );


        if (
                alreadyInCart
        ) {

            throw new IllegalArgumentException(
                    "Bu kurs zaten sepetinizde bulunuyor."
            );
        }


        // =====================================================
        // CART ITEM
        // =====================================================

        CartItem cartItem =
                new CartItem();


        cartItem.setCart(
                cart
        );


        cartItem.setCourse(
                course
        );


        /*
         * Kurs fiyatı daha sonra değişse bile
         * sepete eklendiği andaki fiyat korunur.
         */
        cartItem.setPriceAtAddition(
                course.getPrice()
        );


        cartItemRepository.save(
                cartItem
        );


        cart.getItems()
                .add(
                        cartItem
                );


        recalculateCartTotal(
                cart
        );


        Cart savedCart =
                cartRepository.save(
                        cart
                );


        return toResponse(
                savedCart
        );
    }


    // =========================================================
    // SEPETTEN KURS ÇIKAR
    // =========================================================

    @Transactional
    public CartResponse removeCourseFromCart(
            Long studentId,
            Long cartItemId) {

        Cart cart =
                getOrCreateCartEntity(
                        studentId
                );


        CartItem itemToRemove =
                cart.getItems()
                        .stream()
                        .filter(
                                item ->
                                        item.getId()
                                                .equals(
                                                        cartItemId
                                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Sepette böyle bir ürün bulunamadı."
                                )
                        );


        cart.getItems()
                .remove(
                        itemToRemove
                );


        cartItemRepository.delete(
                itemToRemove
        );


        recalculateCartTotal(
                cart
        );


        Cart savedCart =
                cartRepository.save(
                        cart
                );


        return toResponse(
                savedCart
        );
    }


    // =========================================================
    // SEPETİ BOŞALT
    // =========================================================

    @Transactional
    public CartResponse emptyCart(
            Long studentId) {

        Cart cart =
                getOrCreateCartEntity(
                        studentId
                );


        cart.getItems()
                .clear();


        cart.setTotalPrice(
                BigDecimal.ZERO
        );


        Cart savedCart =
                cartRepository.save(
                        cart
                );


        return toResponse(
                savedCart
        );
    }


    // =========================================================
    // ENTITY OLARAK SEPETİ GETİR / OLUŞTUR
    //
    // Yalnızca CartService içerisindeki transaction'lı
    // public metotlardan çağrılır.
    // =========================================================

    private Cart getOrCreateCartEntity(
            Long studentId) {


        // =====================================================
        // OWNERSHIP / IDOR
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        Student student =
                studentRepository
                        .findById(
                                studentId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğrenci bulunamadı."
                                )
                        );


        Cart cart =
                student.getCart();


        // =====================================================
        // SEPET YOKSA OLUŞTUR
        // =====================================================

        if (
                cart == null
        ) {

            Cart newCart =
                    new Cart();


            newCart.setStudent(
                    student
            );


            newCart.setTotalPrice(
                    BigDecimal.ZERO
            );


            cart =
                    cartRepository.save(
                            newCart
                    );


            student.setCart(
                    cart
            );
        }


        return cart;
    }


    // =========================================================
    // SEPET TOPLAMINI HESAPLA
    // =========================================================

    private void recalculateCartTotal(
            Cart cart) {


        BigDecimal total =
                cart.getItems()
                        .stream()
                        .map(
                                CartItem::getPriceAtAddition
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        cart.setTotalPrice(
                total
        );
    }


    // =========================================================
    // CART ENTITY -> DTO
    // =========================================================

    private CartResponse toResponse(
            Cart cart) {


        List<CartItemResponse> itemResponses =
                cart.getItems()
                        .stream()
                        .map(
                                this::toItemResponse
                        )
                        .toList();


        return new CartResponse(
                cart.getId(),
                itemResponses,
                cart.getTotalPrice()
        );
    }


    // =========================================================
    // CART ITEM ENTITY -> DTO
    // =========================================================

    private CartItemResponse toItemResponse(
            CartItem cartItem) {


        return new CartItemResponse(
                cartItem.getId(),
                toSafeCourseResponse(
                        cartItem.getCourse()
                ),
                cartItem.getPriceAtAddition()
        );
    }


    // =========================================================
    // COURSE ENTITY -> GÜVENLİ DTO
    //
    // videoPath ve öğretmenin hassas bilgileri çıkmaz.
    // =========================================================

    private PublicCourseResponse toSafeCourseResponse(
            Course course) {


        PublicCourseResponse.TeacherSummary teacherSummary =
                null;


        if (
                course.getTeacher() != null
        ) {

            teacherSummary =
                    new PublicCourseResponse.TeacherSummary(
                            course
                                    .getTeacher()
                                    .getName()
                    );
        }


        return new PublicCourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getPrice(),
                course.getImageUrl(),
                course.getMaxCapacity(),
                course.getCurrentEnrolled(),
                course.isPurchasable(),
                teacherSummary
        );
    }
}