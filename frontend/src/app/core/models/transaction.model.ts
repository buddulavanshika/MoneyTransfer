export interface Transaction {
  id: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  status: TransactionStatus;
  failureReason?: string;
  createdOn: string;
  type?: 'DEBIT' | 'CREDIT';
}

export enum TransactionStatus {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED'
}

export interface TransactionLogResponse {
  id: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  status: TransactionStatus;
  failureReason?: string;
  createdOn: string;
}