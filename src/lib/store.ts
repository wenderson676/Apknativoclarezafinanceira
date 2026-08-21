import { useState, useEffect } from 'react';
import { Transaction, Goal, Account, Debt, BudgetMode, BucketType } from '../types';
import { BUDGET_MODES, BIBLE_VERSES } from './utils';

const DEFAULT_ACCOUNTS: Account[] = [
  { id: 'carteira', name: 'Carteira (Dinheiro)', icon: '💵', type: 'carteira', initialBalance: 0 },
  { id: 'banco', name: 'Conta Principal (Banco)', icon: '🏦', type: 'banco', initialBalance: 0, isMain: true },
  { id: 'reserva', name: 'Cofrinho / Reserva', icon: '💰', type: 'reserva', initialBalance: 0 },
];

export function useStore() {
  const [transactions, setTransactions] = useState<Transaction[]>(() => {
    const saved = localStorage.getItem('clareza_transactions');
    return saved ? JSON.parse(saved) : [];
  });

  const [goals, setGoals] = useState<Goal[]>(() => {
    const saved = localStorage.getItem('clareza_goals');
    return saved ? JSON.parse(saved) : [];
  });

  const [accounts, setAccounts] = useState<Account[]>(() => {
    const saved = localStorage.getItem('clareza_accounts');
    return saved ? JSON.parse(saved) : DEFAULT_ACCOUNTS;
  });

  const [debts, setDebts] = useState<Debt[]>(() => {
    const saved = localStorage.getItem('clareza_debts');
    return saved ? JSON.parse(saved) : [];
  });

  const [budgetMode, setBudgetMode] = useState<BudgetMode>(() => {
    const saved = localStorage.getItem('clareza_budget_mode');
    return (saved as BudgetMode) || '50-30-20';
  });

  const [userName, setUserName] = useState<string>(() => {
    return localStorage.getItem('clareza_user_name') || '';
  });

  const [monthNotes, setMonthNotes] = useState<Record<string, string>>(() => {
    const saved = localStorage.getItem('clareza_month_notes');
    return saved ? JSON.parse(saved) : {};
  });

  const [dailyVerse, setDailyVerse] = useState<string>(() => {
    const idx = Math.floor(Math.random() * BIBLE_VERSES.length);
    return BIBLE_VERSES[idx];
  });

  const [isDarkMode, setIsDarkMode] = useState<boolean>(() => {
    return localStorage.getItem('clareza_theme') === 'dark';
  });

  const [selectedMonth, setSelectedMonth] = useState<string>(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  });

  useEffect(() => {
    localStorage.setItem('clareza_transactions', JSON.stringify(transactions));
  }, [transactions]);

  useEffect(() => {
    localStorage.setItem('clareza_goals', JSON.stringify(goals));
  }, [goals]);

  useEffect(() => {
    localStorage.setItem('clareza_accounts', JSON.stringify(accounts));
  }, [accounts]);

  useEffect(() => {
    localStorage.setItem('clareza_debts', JSON.stringify(debts));
  }, [debts]);

  useEffect(() => {
    localStorage.setItem('clareza_budget_mode', budgetMode);
  }, [budgetMode]);

  useEffect(() => {
    localStorage.setItem('clareza_user_name', userName);
  }, [userName]);

  useEffect(() => {
    localStorage.setItem('clareza_month_notes', JSON.stringify(monthNotes));
  }, [monthNotes]);

  useEffect(() => {
    localStorage.setItem('clareza_theme', isDarkMode ? 'dark' : 'light');
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [isDarkMode]);

  // Derived state
  const currentMonthTransactions = transactions.filter(t => t.date.startsWith(selectedMonth));

  const totalIncome = currentMonthTransactions
    .filter(t => (t.type === 'income' || (!t.type && t.bucket === 'Renda')) && !t.isPending)
    .reduce((sum, t) => sum + t.amount, 0);

  const totalExpenses = currentMonthTransactions
    .filter(t => (t.type === 'expense' || (!t.type && t.bucket !== 'Renda' && t.bucket !== 'Transferência')) && !t.isPending)
    .reduce((sum, t) => sum + t.amount, 0);

  const netSavingsTransfer = currentMonthTransactions
    .filter(t => !t.isPending)
    .reduce((acc, t) => {
      if (t.type === 'transfer_to_savings') return acc + t.amount;
      if (t.type === 'transfer_from_savings') return acc - t.amount;
      return acc;
    }, 0);

  const monthResult = totalIncome - totalExpenses - netSavingsTransfer;

  const currentConfig = BUDGET_MODES[budgetMode];
  const bucketLimits = {
    Necessidades: totalIncome * currentConfig.needs,
    Desejos: totalIncome * currentConfig.wants,
    'Reserva/Dívidas': totalIncome * currentConfig.savings,
  };

  const bucketExpenses = {
    Necessidades: currentMonthTransactions
      .filter(t => t.bucket === 'Necessidades' && (!t.type || t.type === 'expense') && !t.isPending)
      .reduce((sum, t) => sum + t.amount, 0),
    Desejos: currentMonthTransactions
      .filter(t => t.bucket === 'Desejos' && (!t.type || t.type === 'expense') && !t.isPending)
      .reduce((sum, t) => sum + t.amount, 0),
    'Reserva/Dívidas': currentMonthTransactions
      .filter(t => t.bucket === 'Reserva/Dívidas' && (!t.type || t.type === 'expense') && !t.isPending)
      .reduce((sum, t) => sum + t.amount, 0),
  };

  const accountBalances = accounts.reduce((acc, account) => {
    let balance = account.initialBalance;
    transactions
      .filter(t => !t.isPending)
      .forEach(t => {
        if (t.type === 'transfer_between_accounts') {
          if (t.account === account.id) balance -= t.amount;
          if (t.toAccount === account.id) balance += t.amount;
        } else if (t.type === 'transfer_to_savings') {
          if (t.account === account.id) balance -= t.amount;
          if (account.type === 'reserva') balance += t.amount;
        } else if (t.type === 'transfer_from_savings') {
          if (t.account === account.id) balance += t.amount;
          if (account.type === 'reserva') balance -= t.amount;
        } else if (t.type === 'income' || t.bucket === 'Renda') {
          if (t.account === account.id || (!t.account && account.isMain)) balance += t.amount;
        } else {
          if (t.account === account.id || (!t.account && account.isMain)) balance -= t.amount;
        }
      });
    acc[account.id] = balance;
    return acc;
  }, {} as Record<string, number>);

  const totalBalance = Object.values(accountBalances).reduce((a, b) => a + b, 0);
  const liquidBalance = accounts
    .filter(a => a.type !== 'reserva')
    .reduce((sum, a) => sum + (accountBalances[a.id] || 0), 0);

  const addTransaction = (newTx: Omit<Transaction, 'id'>, repeatCount: number = 1, frequency: string = 'none') => {
    if (frequency === 'none' || repeatCount <= 1) {
      const tx: Transaction = {
        ...newTx,
        id: crypto.randomUUID(),
      };
      setTransactions(prev => [tx, ...prev]);
    } else {
      const seriesId = crypto.randomUUID();
      const newTxs: Transaction[] = [];
      const [year, month, day] = newTx.date.split('-').map(Number);
      const baseDate = new Date(year, month - 1, day);

      for (let i = 0; i < repeatCount; i++) {
        const d = new Date(baseDate);
        if (frequency === 'monthly') d.setMonth(d.getMonth() + i);
        else if (frequency === 'weekly') d.setDate(d.getDate() + (i * 7));
        else if (frequency === 'biweekly') d.setDate(d.getDate() + (i * 14));

        const yStr = d.getFullYear();
        const mStr = String(d.getMonth() + 1).padStart(2, '0');
        const dStr = String(d.getDate()).padStart(2, '0');
        const dateStr = `${yStr}-${mStr}-${dStr}`;

        newTxs.push({
          ...newTx,
          id: crypto.randomUUID(),
          date: dateStr,
          isPending: i > 0 ? true : !!newTx.isPending,
          seriesId,
          currentInstallment: i + 1,
          totalInstallments: repeatCount,
        });
      }
      setTransactions(prev => [...newTxs, ...prev]);
    }
  };

  const updateTransaction = (updated: Transaction) => {
    setTransactions(prev => prev.map(t => t.id === updated.id ? updated : t));
  };

  const deleteTransaction = (id: string) => {
    setTransactions(prev => prev.filter(t => t.id !== id));
  };

  const togglePending = (id: string) => {
    setTransactions(prev => prev.map(t => t.id === id ? { ...t, isPending: !t.isPending } : t));
  };

  const saveGoal = (goal: Goal) => {
    setGoals(prev => {
      const idx = prev.findIndex(g => g.id === goal.id);
      if (idx >= 0) {
        const next = [...prev];
        next[idx] = goal;
        return next;
      }
      return [...prev, { ...goal, id: goal.id || crypto.randomUUID() }];
    });
  };

  const deleteGoal = (id: string) => {
    setGoals(prev => prev.filter(g => g.id !== id));
  };

  const saveDebt = (debt: Debt) => {
    setDebts(prev => {
      const idx = prev.findIndex(d => d.id === debt.id);
      if (idx >= 0) {
        const next = [...prev];
        next[idx] = debt;
        return next;
      }
      return [...prev, { ...debt, id: debt.id || crypto.randomUUID() }];
    });
  };

  const deleteDebt = (id: string) => {
    setDebts(prev => prev.filter(d => d.id !== id));
  };

  const saveAccount = (account: Account) => {
    setAccounts(prev => {
      const idx = prev.findIndex(a => a.id === account.id);
      if (idx >= 0) {
        const next = [...prev];
        next[idx] = account;
        return next;
      }
      return [...prev, { ...account, id: account.id || crypto.randomUUID() }];
    });
  };

  const deleteAccount = (id: string) => {
    setAccounts(prev => prev.filter(a => a.id !== id));
  };

  const refreshVerse = () => {
    const idx = Math.floor(Math.random() * BIBLE_VERSES.length);
    setDailyVerse(BIBLE_VERSES[idx]);
  };

  const resetAll = () => {
    localStorage.clear();
    setTransactions([]);
    setGoals([]);
    setDebts([]);
    setAccounts(DEFAULT_ACCOUNTS);
    setBudgetMode('50-30-20');
    setMonthNotes({});
    setUserName('');
  };

  return {
    transactions,
    currentMonthTransactions,
    goals,
    accounts,
    debts,
    budgetMode,
    setBudgetMode,
    userName,
    setUserName,
    monthNotes,
    setMonthNotes,
    dailyVerse,
    refreshVerse,
    isDarkMode,
    setIsDarkMode,
    selectedMonth,
    setSelectedMonth,
    totalIncome,
    totalExpenses,
    netSavingsTransfer,
    monthResult,
    bucketLimits,
    bucketExpenses,
    accountBalances,
    totalBalance,
    liquidBalance,
    addTransaction,
    updateTransaction,
    deleteTransaction,
    togglePending,
    saveGoal,
    deleteGoal,
    saveDebt,
    deleteDebt,
    saveAccount,
    deleteAccount,
    resetAll,
  };
}
