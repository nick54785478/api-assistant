import { TestBed } from '@angular/core/testing';

import { PlaybookService } from './playbook.service';

describe('PlaybookService', () => {
  let service: PlaybookService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PlaybookService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
