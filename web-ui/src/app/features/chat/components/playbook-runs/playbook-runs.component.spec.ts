import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlaybookRunsComponent } from './playbook-runs.component';

describe('PlaybookRunsComponent', () => {
  let component: PlaybookRunsComponent;
  let fixture: ComponentFixture<PlaybookRunsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaybookRunsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlaybookRunsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
