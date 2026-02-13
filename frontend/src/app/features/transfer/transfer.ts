import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../core/services/auth';
import { AccountService } from '../../core/services/account';
import { TransferService } from '../../core/services/transfer';
import { Navbar } from '../../shared/components/navbar/navbar';
import { TransferRequest } from '../../core/models/transfer.model';
import { AccountResponse } from '../../core/models/account.model';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    Navbar
  ],
  templateUrl: './transfer.html',
  styleUrls: ['./transfer.scss'] // <-- plural
})
export class Transfer implements OnInit {
  transferForm: FormGroup;
  currentAccount: AccountResponse | null = null;
  loading = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private accountService: AccountService,
    private transferService: TransferService,
    private router: Router
  ) {
    this.transferForm = this.fb.group({
      // Use regex literal; pattern allows only digits (one or more)
      toAccountId: ['', [Validators.required, Validators.pattern(/^[0-9]+$/)]],
      // Amount must be > 0
      amount: ['', [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.loadCurrentAccount();
  }

  loadCurrentAccount(): void {
    const accountId = this.authService.getAccountId();

    if (!accountId) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;
    this.accountService.getAccount(accountId).subscribe({
      next: (data) => {
        this.currentAccount = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading account:', error);
        this.errorMessage = error?.error?.message || 'Unable to load account.';
        this.loading = false;
      }
    });
  }

  /**
   * Called by (click)="reload()" in the template when not loading and no account is present.
   * Reloads the account data without a full page refresh.
   */
  reload(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.loadCurrentAccount();
  }
onSubmit(): void {
  // Mark all fields as touched to trigger validation messages immediately
  this.transferForm.markAllAsTouched();

  // Validate form and ensure account is present
  if (this.transferForm.invalid || !this.currentAccount) {
    return; // If form is invalid, stop execution
  }

  // Extract raw values
  const rawToAccountId = this.transferForm.value.toAccountId;
  const rawAmount = this.transferForm.value.amount;

  // Safely cast to numbers
  const toAccountId = Number(rawToAccountId);
  const amount = Number(rawAmount);

  // Validate numeric conversions
  if (!Number.isFinite(toAccountId)) {
    this.errorMessage = 'Please enter a valid destination account ID.';
    return;
  }
  if (!Number.isFinite(amount)) {
    this.errorMessage = 'Please enter a valid amount.';
    return;
  }

  // Prevent transfer to self
  if (toAccountId === this.currentAccount.id) {
    this.errorMessage = 'Cannot transfer to the same account';
    return;
  }

  // Ensure sufficient balance
  if (amount > this.currentAccount.balance) {
    this.errorMessage = 'Insufficient balance for this transfer';
    return;
  }

  this.submitting = true;
  this.errorMessage = '';
  this.successMessage = '';

  const transferRequest: TransferRequest = {
    fromAccountId: this.currentAccount.id,
    toAccountId: toAccountId,
    amount,
    idempotencyKey: this.transferService.generateIdempotencyKey()
  };

  this.transferService.transfer(transferRequest).subscribe({
    next: (response) => {
      this.submitting = false;
      this.successMessage = `Transfer successful! ${this.formatCurrency(response.amount)} sent to Account ${response.creditedTo}`;
      this.transferForm.reset();
      this.loadCurrentAccount(); // Refresh balance after transfer

      // Navigate to history after 2 seconds
      setTimeout(() => {
        this.router.navigate(['/history']);
      }, 2000);
    },
    error: (error) => {
      this.submitting = false;
      this.errorMessage = error.error?.message || 'Transfer failed. Please try again.';
    }
  });




  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  // Helper getter used in template to show amount error states
  get amountInvalid() {
    const amountControl = this.transferForm.get('amount');
    return amountControl?.invalid && amountControl?.touched;
  }
}
