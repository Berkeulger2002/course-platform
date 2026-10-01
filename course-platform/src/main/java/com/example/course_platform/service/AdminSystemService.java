package com.example.course_platform.service;


import com.example.course_platform.dto.AdminSystemComponentResponse;
import com.example.course_platform.dto.AdminSystemResponse;

import lombok.RequiredArgsConstructor;

import org.bson.Document;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;

import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.stereotype.Service;


import javax.sql.DataSource;

import java.lang.management.ManagementFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminSystemService {


    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final DataSource
            dataSource;


    private final MongoTemplate
            mongoTemplate;


    private final Flyway
            flyway;


    // =========================================================
    // SYSTEM STATUS
    // =========================================================

    public AdminSystemResponse
    getSystemStatus() {


        List<AdminSystemComponentResponse>
                components =
                new ArrayList<>();


        // =====================================================
        // BACKEND
        // =====================================================

        components.add(

                new AdminSystemComponentResponse(

                        "Backend",

                        "UP",

                        null,

                        "Spring Boot API çalışıyor."
                )
        );


        // =====================================================
        // POSTGRESQL
        // =====================================================

        AdminSystemComponentResponse
                postgresStatus =
                checkPostgreSql();


        components.add(
                postgresStatus
        );


        // =====================================================
        // MONGODB
        // =====================================================

        AdminSystemComponentResponse
                mongoStatus =
                checkMongoDb();


        components.add(
                mongoStatus
        );


        // =====================================================
        // OVERALL STATUS
        // =====================================================

        boolean allUp =

                components
                        .stream()

                        .allMatch(

                                component ->

                                        "UP".equals(
                                                component.status()
                                        )
                        );


        String overallStatus =

                allUp
                        ?
                        "UP"
                        :
                        "DEGRADED";


        // =====================================================
        // JVM
        // =====================================================

        Runtime runtime =
                Runtime.getRuntime();


        long usedMemoryBytes =

                runtime.totalMemory()
                        -
                        runtime.freeMemory();


        long usedMemoryMb =

                bytesToMegabytes(
                        usedMemoryBytes
                );


        long maxMemoryMb =

                bytesToMegabytes(
                        runtime.maxMemory()
                );


        // =====================================================
        // UPTIME
        // =====================================================

        long uptimeSeconds =

                ManagementFactory
                        .getRuntimeMXBean()
                        .getUptime()
                        /
                        1000;


        // =====================================================
        // FLYWAY
        // =====================================================

        String flywayVersion =
                getFlywayVersion();


        // =====================================================
        // RESPONSE
        // =====================================================

        return new AdminSystemResponse(

                overallStatus,

                LocalDateTime.now(),

                uptimeSeconds,

                System.getProperty(
                        "java.version"
                ),

                usedMemoryMb,

                maxMemoryMb,

                flywayVersion,

                components
        );
    }


    // =========================================================
    // POSTGRESQL CHECK
    // =========================================================

    private AdminSystemComponentResponse
    checkPostgreSql() {


        long startedAt =
                System.nanoTime();


        try (

                Connection connection =
                        dataSource.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                "SELECT 1"
                        );

                ResultSet resultSet =
                        statement.executeQuery()

        ) {


            if (
                    !resultSet.next()
            ) {


                throw new IllegalStateException(
                        "PostgreSQL health query sonuç döndürmedi."
                );
            }


            long responseTimeMs =

                    elapsedMilliseconds(
                            startedAt
                    );


            return new AdminSystemComponentResponse(

                    "PostgreSQL",

                    "UP",

                    responseTimeMs,

                    "Veritabanı bağlantısı başarılı."
            );


        } catch (
                Exception exception
        ) {


            long responseTimeMs =

                    elapsedMilliseconds(
                            startedAt
                    );


            return new AdminSystemComponentResponse(

                    "PostgreSQL",

                    "DOWN",

                    responseTimeMs,

                    safeErrorMessage(
                            exception
                    )
            );
        }
    }


    // =========================================================
    // MONGODB CHECK
    // =========================================================

    private AdminSystemComponentResponse
    checkMongoDb() {


        long startedAt =
                System.nanoTime();


        try {


            Document result =

                    mongoTemplate.executeCommand(

                            new Document(
                                    "ping",
                                    1
                            )
                    );


            Object ok =
                    result.get(
                            "ok"
                    );


            boolean successful =

                    ok instanceof Number

                            &&

                            (
                                    (Number) ok
                            )
                                    .doubleValue()
                                    >=
                                    1.0;


            if (
                    !successful
            ) {


                throw new IllegalStateException(
                        "MongoDB ping başarısız."
                );
            }


            long responseTimeMs =

                    elapsedMilliseconds(
                            startedAt
                    );


            return new AdminSystemComponentResponse(

                    "MongoDB",

                    "UP",

                    responseTimeMs,

                    "MongoDB bağlantısı başarılı."
            );


        } catch (
                Exception exception
        ) {


            long responseTimeMs =

                    elapsedMilliseconds(
                            startedAt
                    );


            return new AdminSystemComponentResponse(

                    "MongoDB",

                    "DOWN",

                    responseTimeMs,

                    safeErrorMessage(
                            exception
                    )
            );
        }
    }


    // =========================================================
    // FLYWAY VERSION
    // =========================================================

    private String getFlywayVersion() {


        try {


            MigrationInfo currentMigration =

                    flyway
                            .info()
                            .current();


            if (
                    currentMigration == null
                            ||
                            currentMigration.getVersion() == null
            ) {


                return "Henüz migration yok";
            }


            return "V"
                    +
                    currentMigration
                            .getVersion()
                            .getVersion();


        } catch (
                Exception exception
        ) {


            return "Bilinmiyor";
        }
    }


    // =========================================================
    // ELAPSED TIME
    // =========================================================

    private long elapsedMilliseconds(
            long startedAt
    ) {


        return (

                System.nanoTime()
                        -
                        startedAt

        )
                /
                1_000_000;
    }


    // =========================================================
    // MEMORY
    // =========================================================

    private long bytesToMegabytes(
            long bytes
    ) {


        return bytes
                /
                (
                        1024
                                *
                                1024
                );
    }


    // =========================================================
    // SAFE ERROR MESSAGE
    //
    // Connection URI, password vb. hassas bilgileri
    // admin response'una taşımıyoruz.
    // =========================================================

    private String safeErrorMessage(
            Exception exception
    ) {


        if (
                exception == null
        ) {


            return "Bağlantı kontrolü başarısız.";
        }


        return exception
                .getClass()
                .getSimpleName();
    }
}