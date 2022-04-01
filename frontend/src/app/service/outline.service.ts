import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Link } from '../entity/link';
import { OutlineRequest } from '../entity/outline-request';
import { OutlineResponse } from '../entity/outline-response';

@Injectable({
  providedIn: 'root'
})
export class OutlineService {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  public outline(request: OutlineRequest):  Observable<OutlineResponse> {
    return this.http.post<OutlineResponse>(`${this.apiServerUrl}/outline`, request);
  }

  public get(shortLink: string):  Observable<Link> {
    return this.http.get<Link>(`${this.apiServerUrl}/${shortLink}`);
  }
}
