import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { OutlineResponse } from 'src/app/entity/outline-response';
import { OutlineService } from 'src/app/service/outline.service';

@Component({
  selector: 'app-start',
  templateUrl: './start.component.html',
  styleUrls: ['./start.component.css']
})
export class StartComponent implements OnInit {
  form: FormGroup;
  loading: boolean = false;

  constructor(private service: OutlineService, private fb: FormBuilder, private router: Router) { 
    this.form = this.fb.group({
      search: ['', Validators.required]
    });
  }

  ngOnInit(): void {
  }

  generateOutline(): void {
    if(this.form.valid) {
      this.loading = true;
      this.service.outline({url: this.form.controls.search.value}).subscribe(
        (response: OutlineResponse) => {
          this.loading = false;
          this.router.navigate(['/'+response.shortUrl]);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }
}
