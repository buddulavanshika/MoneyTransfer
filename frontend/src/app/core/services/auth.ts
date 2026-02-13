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
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  
  private currentUserSubject = new BehaviorSubject<AuthUser | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/accounts/login`, credentials)
      .pipe(
        tap(response => {
          this.setSession(response, credentials);
        })
      );
  }

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
    }
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token;
  }

  getToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  getAccountId(): number | null {
    const user = this.getCurrentUser();
    return user ? user.accountId : null;
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

  private getUserFromStorage(): AuthUser | null {
    if (!this.isBrowser) {
      return null;
    }
    
    const userStr = localStorage.getItem(this.USER_KEY);
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  }
}