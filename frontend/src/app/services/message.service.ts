import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Message, Page } from '../models/message.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getMessages(roomId: number, page: number = 0, size: number = 50): Observable<Page<Message>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Message>>(`${this.apiUrl}/rooms/${roomId}/messages`, { params }).pipe(
      catchError(error => {
        console.error('Error fetching messages:', error);
        return throwError(() => error);
      })
    );
  }
}
