import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth';
import { AccountService } from '../../core/services/account';
import { Navbar } from '../../shared/components/navbar/navbar';
import { AccountResponse } from '../../core/models/account.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    Navbar
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  account: AccountResponse | null = null;
  loading = true;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private accountService: AccountService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAccountData();
  }

  loadAccountData(): void {
    const accountId = this.authService.getAccountId();
    
    if (!accountId) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;
    this.accountService.getAccount(accountId).subscribe({
      next: (data) => {
        this.account = data;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load account data';
        this.loading = false;
        console.error('Error loading account:', error);
      }
    });
  }

  navigateToTransfer(): void {
    this.router.navigate(['/transfer']);
  }

  navigateToHistory(): void {
    this.router.navigate(['/history']);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  refreshBalance(): void {
    this.loadAccountData();
  }
}