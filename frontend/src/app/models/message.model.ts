export interface Message {
  id: number;
  content: string;
  senderUsername: string;
  senderDisplayName: string;
  roomId: number;
  type: 'TEXT' | 'JOIN' | 'LEAVE' | 'IMAGE';
  createdAt: string;
}

export interface ChatMessage {
  content: string;
  roomId: number;
  type: 'TEXT' | 'JOIN' | 'LEAVE';
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
