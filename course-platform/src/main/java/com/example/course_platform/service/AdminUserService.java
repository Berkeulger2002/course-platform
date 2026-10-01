package com.example.course_platform.service;


import com.example.course_platform.dto.AdminUserResponse;
import com.example.course_platform.dto.AdminUserSummaryResponse;

import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.User;

import com.example.course_platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminUserService {


    private final UserRepository
            userRepository;


    // =========================================================
    // USER LIST
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public List<AdminUserResponse>
    getUsers() {


        return userRepository

                .findAllByOrderByIdDesc()

                .stream()

                .map(
                        this::toResponse
                )

                .toList();
    }


    // =========================================================
    // USER SUMMARY
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public AdminUserSummaryResponse
    getSummary() {


        long totalUsers =
                userRepository.count();


        long students =
                userRepository.countByRole(
                        Role.STUDENT
                );


        long teachers =
                userRepository.countByRole(
                        Role.TEACHER
                );


        long admins =
                userRepository.countByRole(
                        Role.ADMIN
                );


        return new AdminUserSummaryResponse(

                totalUsers,

                students,

                teachers,

                admins
        );
    }


    // =========================================================
    // MAPPER
    // =========================================================

    private AdminUserResponse
    toResponse(
            User user
    ) {


        return new AdminUserResponse(

                user.getId(),

                user.getName(),

                user.getEmail(),

                user.getRole()
                        .name()
        );
    }
}