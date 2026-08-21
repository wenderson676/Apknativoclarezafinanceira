export type BucketType = 'Necessidades' | 'Desejos' | 'Reserva/Dívidas' | 'Renda' | 'Transferência';

export type DebtType = 
  | 'card_revolving' 
  | 'overdraft' 
  | 'unsecured_loan' 
  | 'payroll_loan' 
  | 'vehicle_financing' 
  | 'real_estate' 
  | 'essential_services' 
  | 'informal_family' 
  | 'other';

export interface Debt {
  id: string;
  name: string;
  totalAmount: number;
  monthlyPayment: number;
  interestRate: number;
  isLate: boolean;
  creditor: string;
  type?: DebtType;
}

export type TransactionType = 'expense' | 'income' | 'transfer_between_accounts' | 'transfer_to_savings' | 'transfer_from_savings';

export interface Transaction {
  id: string;
  description: string;
  amount: number;
  date: string;
  bucket: BucketType;
  category: string;
  account?: string;
  toAccount?: string;
  type?: TransactionType;
  isPending?: boolean;
  seriesId?: string;
  currentInstallment?: number;
  totalInstallments?: number;
}

export interface Goal {
  id: string;
  title: string;
  targetAmount: number;
  currentAmount: number;
}

export interface Account {
  id: string;
  name: string;
  icon: string;
  type: 'carteira' | 'banco' | 'reserva' | 'custom';
  initialBalance: number;
  isMain?: boolean;
}

export type BudgetMode = '50-30-20' | '80-10-10' | '90-5-5' | '70-0-30' | '50-20-30';

export interface BudgetConfig {
  needs: number;
  wants: number;
  savings: number;
}
