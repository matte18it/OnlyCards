import { TestBed } from '@angular/core/testing';

import { CardDetailsChartService } from './card-details-chart.service';

describe('CardDetailsChartService', () => {
  let service: CardDetailsChartService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CardDetailsChartService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
