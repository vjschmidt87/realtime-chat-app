import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, switchMap, tap, catchError, of } from 'rxjs';
import { ChatRoom, ChatRoomRequest } from '../../models/chat-room.model';
import { User } from '../../models/user.model';
import { AuthService } from '../../services/auth.service';
import { ChatService } from '../../services/chat.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-chat-room-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './chat-room-list.component.html',
  styleUrl: './chat-room-list.component.scss'
})
export class ChatRoomListComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private apiUrl = `${environment.apiUrl}/rooms`;

  rooms: ChatRoom[] = [];
  currentUser: User | null = null;
  isLoading = true;
  showCreateForm = false;
  createRoomForm: FormGroup;
  errorMessage = '';

  private refreshTrigger = new BehaviorSubject<void>(undefined);

  constructor(
    private http: HttpClient,
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private chatService: ChatService
  ) {
    this.createRoomForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      description: ['', [Validators.maxLength(200)]],
      type: ['GROUP', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.authService.currentUser$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => {
        this.currentUser = user;
      });

    this.chatService.connect();

    this.refreshTrigger.pipe(
      switchMap(() =>
        this.http.get<ChatRoom[]>(this.apiUrl).pipe(
          catchError(error => {
            console.error('Failed to load rooms:', error);
            this.errorMessage = 'Failed to load chat rooms.';
            return of([]);
          })
        )
      ),
      tap(rooms => {
        this.rooms = rooms;
        this.isLoading = false;
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe();
  }

  openRoom(roomId: number): void {
    this.router.navigate(['/chat', roomId]);
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    if (!this.showCreateForm) {
      this.createRoomForm.reset({ type: 'GROUP' });
    }
  }

  createRoom(): void {
    if (this.createRoomForm.invalid) {
      this.createRoomForm.markAllAsTouched();
      return;
    }

    const request: ChatRoomRequest = this.createRoomForm.value;

    this.http.post<ChatRoom>(this.apiUrl, request).pipe(
      tap(() => {
        this.showCreateForm = false;
        this.createRoomForm.reset({ type: 'GROUP' });
        this.refreshTrigger.next();
      }),
      catchError(error => {
        console.error('Failed to create room:', error);
        this.errorMessage = error.error?.message || 'Failed to create room.';
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe();
  }

  logout(): void {
    this.chatService.disconnect();
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  get f() {
    return this.createRoomForm.controls;
  }
}
