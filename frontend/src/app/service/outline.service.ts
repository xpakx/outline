import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Link } from '../entity/link';
import { OutlineRequest } from '../entity/outline-request';

@Injectable({
  providedIn: 'root'
})
export class OutlineService {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  public outline(request: OutlineRequest):  Observable<String> {
    return this.http.post<String>(`${this.apiServerUrl}/outline`, request);
  }

  public get(shortLink: string):  Observable<Link> {
    return this.http.get<Link>(`${this.apiServerUrl}/${shortLink}`);
  }
}
