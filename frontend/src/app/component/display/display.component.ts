import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Link } from 'src/app/entity/link';
import { OutlineService } from 'src/app/service/outline.service';

@Component({
  selector: 'app-display',
  templateUrl: './display.component.html',
  styleUrls: ['./display.component.css']
})
export class DisplayComponent implements OnInit {
  page: Link | undefined;

  constructor(private service: OutlineService, private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.route.params.subscribe(routeParams => {
      this.loadLink(routeParams.shortUrl);
    }); 
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

  loadHypothesis(): void {
    var script = document.createElement('script');
    script.setAttribute('src', 'https://hypothes.is/embed.js');
    document.body.appendChild(script);
  }

  downloadMd(): void {
    
  }
}
