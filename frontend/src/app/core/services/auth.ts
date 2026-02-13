import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, AuthUser } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';

  // 🔥 realtime tab lock
  private channel?: BroadcastChannel;
  private readonly TAB_ID = Math.random().toString(36).slice(2);
  private isOwner = false;

  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private currentUserSubject =
    new BehaviorSubject<AuthUser | null>(this.getUserFromStorage());

  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {

    if (this.isBrowser) {
      this.initChannel();
    }
  }

  // 🔥 BROADCAST CHANNEL SETUP
  private initChannel() {

    this.channel = new BroadcastChannel('bank-session');

    this.channel.onmessage = (event) => {

      const msg = event.data;

      // another tab already active
      if (msg.type === 'CHECK_OWNER' && this.isOwner) {
        this.channel?.postMessage({
          type: 'OWNER_EXISTS'
        });
      }

      // this tab should block
      if (msg.type === 'OWNER_EXISTS' && !this.isOwner) {

        alert('⚠️ Session already open in another tab');

        window.location.href = '/login';
      }
    };
  }

  // 🔥 CLAIM OWNERSHIP (called from AppComponent)
  claimOwnership() {

    if (!this.channel) return;

    // ask if owner exists
    this.channel.postMessage({ type: 'CHECK_OWNER' });

    // wait briefly to see if someone responds
    setTimeout(() => {
      if (!this.isOwner) {
        this.isOwner = true;
      }
    }, 100);
  }

  // ✅ LOGIN
  login(credentials: LoginRequest): Observable<LoginResponse> {

    return this.http.post<LoginResponse>(
      `${environment.apiUrl}/accounts/login`,
      credentials
    ).pipe(
      tap(res => this.setSession(res, credentials))
    );
  }

  // ✅ LOGOUT
  logout(): void {

    if (this.isBrowser) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
    }

    this.isOwner = false;
    this.currentUserSubject.next(null);
  }

  private setSession(authResult: LoginResponse, credentials: LoginRequest): void {

    const basicToken = btoa(`${credentials.username}:${credentials.password}`);

    const user: AuthUser = {
      accountId: authResult.id,
      holderName: authResult.holderName,
      username: authResult.username
    };

    if (this.isBrowser) {
      localStorage.setItem(this.TOKEN_KEY, `Basic ${basicToken}`);
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }

    this.currentUserSubject.next(user);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getAccountId(): number | null {
    return this.currentUserSubject.value?.accountId ?? null;
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  private getUserFromStorage(): AuthUser | null {

    if (!this.isBrowser) return null;

    const userStr = localStorage.getItem(this.USER_KEY);

    try {
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  }
}
