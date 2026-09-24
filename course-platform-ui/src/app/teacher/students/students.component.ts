import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  TeacherStudent,
  TeacherStudentService
} from '../../services/teacher-student.service';


@Component({
  selector: 'app-students',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './students.component.html',
  styleUrl: './students.component.css'
})
export class StudentsComponent implements OnInit {

  currentTeacher: any;

  students: TeacherStudent[] = [];

  isLoading = true;

  selectedCourseId: number | null = null;

  selectedCourseName = '';


  constructor(
    private teacherStudentService: TeacherStudentService,
    private route: ActivatedRoute,
    private router: Router
  ) {}


  ngOnInit(): void {

    const userStr =
      localStorage.getItem('currentUser');


    if (!userStr) {

      this.router.navigate(['/auth']);

      return;
    }


    this.currentTeacher =
      JSON.parse(userStr);


    const courseIdParam =
      this.route.snapshot.queryParamMap.get(
        'courseId'
      );


    this.selectedCourseId =
      courseIdParam
        ? Number(courseIdParam)
        : null;


    this.loadStudents();
  }


  loadStudents(): void {

    this.isLoading = true;


    const request$ =
      this.selectedCourseId

        ? this.teacherStudentService
          .getCourseStudents(
            this.currentTeacher.id,
            this.selectedCourseId
          )

        : this.teacherStudentService
          .getTeacherStudents(
            this.currentTeacher.id
          );


    request$.subscribe({

      next: (students) => {

        this.students =
          students || [];


        if (
          this.students.length > 0 &&
          this.selectedCourseId
        ) {

          this.selectedCourseName =
            this.students[0].courseName;
        }


        this.isLoading =
          false;
      },


      error: (err) => {

        console.error(
          'Öğrenciler yüklenirken hata:',
          err
        );


        this.isLoading =
          false;
      }

    });
  }


  // =========================================================
  // BENZERSİZ ÖĞRENCİ SAYISI
  // =========================================================
  get uniqueStudentCount(): number {

    return new Set(
      this.students.map(
        item => item.studentId
      )
    ).size;
  }


  // =========================================================
  // TAMAMLANAN KAYIT SAYISI
  // =========================================================
  get completedCount(): number {

    return this.students.filter(
      item => item.completed
    ).length;
  }


  // =========================================================
  // ORTALAMA İLERLEME
  // =========================================================
  get averageProgress(): number {

    if (
      this.students.length === 0
    ) {

      return 0;
    }


    const total =
      this.students.reduce(
        (sum, item) =>
          sum +
          (item.progressPercentage || 0),

        0
      );


    return total /
      this.students.length;
  }


  clearCourseFilter(): void {

    this.router.navigate([
      '/teacher/students'
    ]);
  }
}
