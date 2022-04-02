import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ClipboardService } from 'ngx-clipboard';
import { Link } from 'src/app/entity/link';
import { OutlineService } from 'src/app/service/outline.service';

@Component({
  selector: 'app-display',
  templateUrl: './display.component.html',
  styleUrls: ['./display.component.css']
})
export class DisplayComponent implements OnInit {
  page: Link | undefined;
  hypothesisLoaded: boolean = false;

  constructor(private service: OutlineService, private route: ActivatedRoute, private clipboard: ClipboardService) { }

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
    if(!this.hypothesisLoaded) {
      var script = document.createElement('script');
      script.setAttribute('src', 'https://hypothes.is/embed.js');
      document.body.appendChild(script);
      this.hypothesisLoaded = true;
    }
  }

  downloadMd(): void {
    if(this.page) {
      this.clipboard.copyFromContent(this.page.content);
    }
  }
}
