import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-message-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './message-input.component.html',
  styleUrl: './message-input.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MessageInputComponent {
  @Input() disabled = false;
  @Output() messageSent = new EventEmitter<string>();

  messageContent = '';

  sendMessage(): void {
    const trimmed = this.messageContent.trim();
    if (!trimmed || this.disabled) {
      return;
    }

    this.messageSent.emit(trimmed);
    this.messageContent = '';
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
}
