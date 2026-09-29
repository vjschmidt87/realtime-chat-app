import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Message, ChatMessage } from '../models/message.model';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

export interface PresenceEvent {
  username: string;
  displayName: string;
  status: 'ONLINE' | 'OFFLINE' | 'AWAY';
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService implements OnDestroy {
  private stompClient: Client;
  private connectedSubject = new BehaviorSubject<boolean>(false);
  private onlineUsersSubject = new BehaviorSubject<Map<string, string>>(new Map());

  connected$ = this.connectedSubject.asObservable();
  onlineUsers$ = this.onlineUsersSubject.asObservable();

  constructor(private authService: AuthService) {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl) as WebSocket,
      connectHeaders: this.getConnectHeaders(),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.connectedSubject.next(true);
      },
      onDisconnect: () => {
        this.connectedSubject.next(false);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message']);
        this.connectedSubject.next(false);
      }
    });
  }

  private getConnectHeaders(): Record<string, string> {
    const token = this.authService.getToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  connect(): void {
    if (this.stompClient.active) {
      return;
    }
    this.stompClient.connectHeaders = this.getConnectHeaders();
    this.stompClient.activate();
  }

  disconnect(): void {
    if (this.stompClient.active) {
      this.stompClient.deactivate();
    }
    this.connectedSubject.next(false);
    this.onlineUsersSubject.next(new Map());
  }

  subscribeToRoom(roomId: number): Observable<Message> {
    return this.subscribeTo<Message>(`/topic/room.${roomId}`);
  }

  subscribeToPresence(): Observable<PresenceEvent> {
    return this.subscribeTo<PresenceEvent>('/topic/presence', (event) => {
      this.updateOnlineUsers(event);
    });
  }

  sendMessage(message: ChatMessage): void {
    if (this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(message)
      });
    }
  }

  sendPresence(status: string): void {
    if (this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/chat.presence',
        body: JSON.stringify({ status })
      });
    }
  }

  private subscribeTo<T>(destination: string, onMessage?: (data: T) => void): Observable<T> {
    return new Observable<T>(observer => {
      const subscribe = () => {
        const subscription = this.stompClient.subscribe(
          destination,
          (message: IMessage) => {
            try {
              const parsed: T = JSON.parse(message.body);
              onMessage?.(parsed);
              observer.next(parsed);
            } catch (error) {
              console.error(`Failed to parse message from ${destination}:`, error);
            }
          }
        );

        observer.add(() => subscription.unsubscribe());
      };

      if (this.stompClient.connected) {
        subscribe();
      } else {
        const connSub = this.connected$.subscribe(connected => {
          if (connected) {
            connSub.unsubscribe();
            subscribe();
          }
        });

        observer.add(() => connSub.unsubscribe());
      }
    });
  }

  private updateOnlineUsers(event: PresenceEvent): void {
    const current = new Map(this.onlineUsersSubject.value);
    if (event.status === 'OFFLINE') {
      current.delete(event.username);
    } else {
      current.set(event.username, event.status);
    }
    this.onlineUsersSubject.next(current);
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
