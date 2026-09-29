import { Component, Input, ElementRef, ViewChild, AfterViewChecked, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Message } from '../../models/message.model';

@Component({
  selector: 'app-message-list',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './message-list.component.html',
  styleUrl: './message-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MessageListComponent implements AfterViewChecked {
  @Input() messages: Message[] = [];
  @Input() currentUsername = '';
  @Input() isLoading = false;

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef<HTMLDivElement>;

  private shouldScroll = true;

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
    }
  }

  onScroll(): void {
    const el = this.scrollContainer?.nativeElement;
    if (!el) return;

    const threshold = 100;
    const distanceFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight;
    this.shouldScroll = distanceFromBottom < threshold;
  }

  isOwnMessage(message: Message): boolean {
    return message.senderUsername === this.currentUsername;
  }

  isSystemMessage(message: Message): boolean {
    return message.type === 'JOIN' || message.type === 'LEAVE';
  }

  formatTime(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  private scrollToBottom(): void {
    const el = this.scrollContainer?.nativeElement;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }
}
