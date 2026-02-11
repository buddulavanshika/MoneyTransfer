import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { Login } from './features/login/login';
import { Dashboard } from './features/dashboard/dashboard';
import { Transfer } from './features/transfer/transfer';
import { History } from './features/history/history';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'transfer', component: Transfer, canActivate: [authGuard] },
  { path: 'history', component: History, canActivate: [authGuard] },
  { path: '**', redirectTo: '/dashboard' }
];