import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, merge, of, switchMap, tap, catchError } from 'rxjs';
import { Message } from '../../models/message.model';
import { ChatRoom } from '../../models/chat-room.model';
import { AuthService } from '../../services/auth.service';
import { ChatService } from '../../services/chat.service';
import { MessageService } from '../../services/message.service';
import { MessageListComponent } from '../message-list/message-list.component';
import { MessageInputComponent } from '../message-input/message-input.component';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-chat-room',
  standalone: true,
  imports: [CommonModule, MessageListComponent, MessageInputComponent],
  templateUrl: './chat-room.component.html',
  styleUrl: './chat-room.component.scss'
})
export class ChatRoomComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private apiUrl = `${environment.apiUrl}/rooms`;

  room: ChatRoom | null = null;
  messages: Message[] = [];
  currentUsername = '';
  isLoading = true;
  isConnected = false;
  roomId = 0;

  private messagesSubject = new BehaviorSubject<Message[]>([]);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private authService: AuthService,
    private chatService: ChatService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.currentUsername = user.username;
    }

    this.chatService.connected$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(connected => {
        this.isConnected = connected;
      });

    this.chatService.connect();

    this.route.params.pipe(
      switchMap(params => {
        this.roomId = +params['id'];
        this.isLoading = true;
        this.messages = [];
        this.messagesSubject.next([]);

        return this.http.get<ChatRoom>(`${this.apiUrl}/${this.roomId}`).pipe(
          catchError(() => {
            this.router.navigate(['/chat']);
            return of(null);
          })
        );
      }),
      tap(room => {
        if (room) {
          this.room = room;
          this.loadMessagesAndSubscribe();
        }
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe();

    this.messagesSubject
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(messages => {
        this.messages = messages;
      });
  }

  private loadMessagesAndSubscribe(): void {
    this.messageService.getMessages(this.roomId, 0, 50).pipe(
      tap(page => {
        const initialMessages = page.content.reverse();
        this.messagesSubject.next(initialMessages);
        this.isLoading = false;
      }),
      switchMap(() => {
        const wsMessages$ = this.chatService.subscribeToRoom(this.roomId);
        return wsMessages$;
      }),
      tap(newMessage => {
        const current = this.messagesSubject.value;
        this.messagesSubject.next([...current, newMessage]);
      }),
      catchError(error => {
        console.error('Error loading messages:', error);
        this.isLoading = false;
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe();

    this.chatService.sendMessage({
      content: '',
      roomId: this.roomId,
      type: 'JOIN'
    });
  }

  onMessageSent(content: string): void {
    this.chatService.sendMessage({
      content,
      roomId: this.roomId,
      type: 'TEXT'
    });
  }

  goBack(): void {
    this.chatService.sendMessage({
      content: '',
      roomId: this.roomId,
      type: 'LEAVE'
    });
    this.router.navigate(['/chat']);
  }
}
