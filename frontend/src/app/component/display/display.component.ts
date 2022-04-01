import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Link } from 'src/app/entity/link';
import { OutlineService } from 'src/app/service/outline.service';

@Component({
  selector: 'app-display',
  templateUrl: './display.component.html',
  styleUrls: ['./display.component.css']
})
export class DisplayComponent implements OnInit {
  page: Link | undefined;

  constructor(private service: OutlineService) { }

  ngOnInit(): void {
  }

  loadLink(url: string): void {
    this.service.get(url).subscribe(
      (response: Link) => {
        this.page = response;
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }
}
