import { TestBed } from '@angular/core/testing';

import { TeacherRevenueService } from './teacher-revenue.service';

describe('TeacherRevenueService', () => {
  let service: TeacherRevenueService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TeacherRevenueService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
