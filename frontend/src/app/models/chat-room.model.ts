export interface ChatRoom {
  id: number;
  name: string;
  description: string;
  type: 'DIRECT' | 'GROUP';
  memberCount: number;
  createdBy: string;
  createdAt: string;
}

export interface ChatRoomRequest {
  name: string;
  description: string;
  type: 'DIRECT' | 'GROUP';
}
