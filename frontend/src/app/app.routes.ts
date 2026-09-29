import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./components/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'chat',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./components/chat-room-list/chat-room-list.component').then(m => m.ChatRoomListComponent)
  },
  {
    path: 'chat/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./components/chat-room/chat-room.component').then(m => m.ChatRoomComponent)
  },
  {
    path: '',
    redirectTo: '/chat',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/chat'
  }
];
