import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlaybookCreateComponent } from './playbook-create.component';

describe('PlaybookCreateComponent', () => {
  let component: PlaybookCreateComponent;
  let fixture: ComponentFixture<PlaybookCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaybookCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlaybookCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
