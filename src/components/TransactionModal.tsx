import React, { useState, useEffect } from 'react';
import { X, Plus, Calendar, Clock } from 'lucide-react';
import { Transaction, BucketType, Account, TransactionType } from '../types';
import { CATEGORIES } from '../lib/utils';

interface TransactionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (transaction: Omit<Transaction, 'id'>, repeatCount?: number, frequency?: string) => void;
  onUpdate?: (transaction: Transaction) => void;
  editingTransaction?: Transaction | null;
  accounts: Account[];
  initialType?: 'expense' | 'income' | 'transfer';
}

export function TransactionModal({
  isOpen,
  onClose,
  onSave,
  onUpdate,
  editingTransaction,
  accounts,
  initialType = 'expense',
}: TransactionModalProps) {
  const [type, setType] = useState<TransactionType>('expense');
  const [description, setDescription] = useState('');
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [bucket, setBucket] = useState<BucketType>('Necessidades');
  const [category, setCategory] = useState('');
  const [account, setAccount] = useState('');
  const [toAccount, setToAccount] = useState('');
  const [isPending, setIsPending] = useState(false);
  const [repeatCount, setRepeatCount] = useState('1');
  const [frequency, setFrequency] = useState('none');

  useEffect(() => {
    if (editingTransaction) {
      setDescription(editingTransaction.description);
      setAmount(editingTransaction.amount.toString());
      setDate(editingTransaction.date);
      setBucket(editingTransaction.bucket);
      setCategory(editingTransaction.category);
      setAccount(editingTransaction.account || accounts.find(a => a.isMain)?.id || accounts[0]?.id || '');
      setToAccount(editingTransaction.toAccount || '');
      setType(editingTransaction.type || 'expense');
      setIsPending(!!editingTransaction.isPending);
    } else {
      setDescription('');
      setAmount('');
      setDate(new Date().toISOString().split('T')[0]);
      setIsPending(false);
      setRepeatCount('1');
      setFrequency('none');
      const defaultAcc = accounts.find(a => a.isMain)?.id || accounts[0]?.id || '';
      setAccount(defaultAcc);
      setToAccount(accounts.find(a => a.type === 'reserva')?.id || accounts[accounts.length - 1]?.id || '');

      if (initialType === 'income') {
        setType('income');
        setBucket('Renda');
      } else if (initialType === 'transfer') {
        setType('transfer_between_accounts');
        setBucket('Transferência');
      } else {
        setType('expense');
        setBucket('Necessidades');
      }
    }
  }, [editingTransaction, initialType, isOpen, accounts]);

  const categoriesList = CATEGORIES[bucket] || [];

  useEffect(() => {
    if (!editingTransaction && categoriesList.length > 0 && !categoriesList.includes(category)) {
      setCategory(categoriesList[0]);
    }
  }, [bucket, categoriesList, category, editingTransaction]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const numAmount = parseFloat(amount.replace(',', '.'));
    if (isNaN(numAmount) || numAmount <= 0 || !description.trim()) return;

    if (editingTransaction && onUpdate) {
      onUpdate({
        ...editingTransaction,
        description: description.trim(),
        amount: numAmount,
        date,
        bucket,
        category: category || 'Outros',
        account,
        toAccount: type.startsWith('transfer') ? toAccount : undefined,
        type,
        isPending,
      });
    } else {
      onSave(
        {
          description: description.trim(),
          amount: numAmount,
          date,
          bucket,
          category: category || 'Outros',
          account,
          toAccount: type.startsWith('transfer') ? toAccount : undefined,
          type,
          isPending,
        },
        parseInt(repeatCount) || 1,
        frequency
      );
    }
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 overflow-y-auto">
      <div className="w-full max-w-lg bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-2xl border border-stone-100 dark:border-stone-800 my-8">
        <div className="flex items-center justify-between pb-4 border-b border-stone-100 dark:border-stone-800">
          <h2 className="text-lg font-bold text-stone-900 dark:text-stone-100">
            {editingTransaction ? 'Editar Lançamento' : 'Novo Lançamento'}
          </h2>
          <button onClick={onClose} className="p-2 text-stone-400 hover:text-stone-600 dark:hover:text-stone-200 rounded-full hover:bg-stone-100 dark:hover:bg-stone-800">
            <X className="w-5 h-5" />
          </button>
        </div>

        {!editingTransaction && (
          <div className="flex bg-stone-100 dark:bg-stone-800 p-1 rounded-2xl my-4">
            <button
              type="button"
              onClick={() => { setType('expense'); setBucket('Necessidades'); }}
              className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all ${type === 'expense' ? 'bg-rose-500 text-white shadow-xs' : 'text-stone-600 dark:text-stone-400'}`}
            >
              Despesa
            </button>
            <button
              type="button"
              onClick={() => { setType('income'); setBucket('Renda'); }}
              className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all ${type === 'income' ? 'bg-emerald-500 text-white shadow-xs' : 'text-stone-600 dark:text-stone-400'}`}
            >
              Receita
            </button>
            <button
              type="button"
              onClick={() => { setType('transfer_between_accounts'); setBucket('Transferência'); }}
              className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all ${type.startsWith('transfer') ? 'bg-indigo-500 text-white shadow-xs' : 'text-stone-600 dark:text-stone-400'}`}
            >
              Transferência
            </button>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div>
            <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
              Valor (R$)
            </label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 font-bold text-stone-400">R$</span>
              <input
                type="number"
                step="0.01"
                required
                value={amount}
                onChange={e => setAmount(e.target.value)}
                placeholder="0,00"
                className="w-full pl-12 pr-4 py-3 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl font-bold text-lg text-stone-900 dark:text-stone-100 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
              Descrição
            </label>
            <input
              type="text"
              required
              value={description}
              onChange={e => setDescription(e.target.value)}
              placeholder="Ex: Supermercado, Aluguel, Salário"
              className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-stone-900 dark:text-stone-100 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
                Data
              </label>
              <input
                type="date"
                required
                value={date}
                onChange={e => setDate(e.target.value)}
                className="w-full px-4 py-2.5 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-stone-900 dark:text-stone-100 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
              />
            </div>

            <div className="flex items-center pt-6">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={isPending}
                  onChange={e => setIsPending(e.target.checked)}
                  className="rounded-md border-stone-300 text-amber-500 focus:ring-amber-500 w-4 h-4"
                />
                <span className="text-xs font-medium text-stone-700 dark:text-stone-300">Lançamento Futuro / Pendente</span>
              </label>
            </div>
          </div>

          {type === 'expense' && (
            <div>
              <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
                Destinação do Orçamento
              </label>
              <div className="grid grid-cols-3 gap-2">
                {(['Necessidades', 'Desejos', 'Reserva/Dívidas'] as BucketType[]).map(b => (
                  <button
                    key={b}
                    type="button"
                    onClick={() => setBucket(b)}
                    className={`py-2 px-2 text-xs font-bold rounded-xl border transition-all text-center ${
                      bucket === b
                        ? 'bg-emerald-500 text-white border-emerald-500 shadow-xs'
                        : 'bg-stone-50 dark:bg-stone-800 border-stone-200 dark:border-stone-700 text-stone-700 dark:text-stone-300'
                    }`}
                  >
                    {b}
                  </button>
                ))}
              </div>
            </div>
          )}

          <div>
            <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
              Categoria
            </label>
            <select
              value={category}
              onChange={e => setCategory(e.target.value)}
              className="w-full px-4 py-2.5 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-stone-900 dark:text-stone-100 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
            >
              {categoriesList.map(cat => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
                {type.startsWith('transfer') ? 'Conta de Origem' : 'Conta / Carteira'}
              </label>
              <select
                value={account}
                onChange={e => setAccount(e.target.value)}
                className="w-full px-4 py-2.5 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-stone-900 dark:text-stone-100 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
              >
                {accounts.map(acc => (
                  <option key={acc.id} value={acc.id}>{acc.icon} {acc.name}</option>
                ))}
              </select>
            </div>

            {type.startsWith('transfer') && (
              <div>
                <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
                  Conta de Destino
                </label>
                <select
                  value={toAccount}
                  onChange={e => setToAccount(e.target.value)}
                  className="w-full px-4 py-2.5 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-stone-900 dark:text-stone-100 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
                >
                  {accounts.map(acc => (
                    <option key={acc.id} value={acc.id}>{acc.icon} {acc.name}</option>
                  ))}
                </select>
              </div>
            )}
          </div>

          {!editingTransaction && (
            <div className="p-3.5 bg-stone-50 dark:bg-stone-800/40 rounded-2xl border border-stone-200/60 dark:border-stone-700/60 space-y-2">
              <label className="block text-xs font-bold text-stone-700 dark:text-stone-300">
                Repetição / Parcelamento
              </label>
              <div className="flex gap-2">
                <select
                  value={frequency}
                  onChange={e => setFrequency(e.target.value)}
                  className="flex-1 px-3 py-2 bg-white dark:bg-stone-800 border border-stone-200 dark:border-stone-700 rounded-xl text-xs"
                >
                  <option value="none">Único</option>
                  <option value="monthly">Mensal</option>
                  <option value="weekly">Semanal</option>
                  <option value="biweekly">Quinzenal</option>
                </select>
                {frequency !== 'none' && (
                  <input
                    type="number"
                    min="1"
                    max="48"
                    value={repeatCount}
                    onChange={e => setRepeatCount(e.target.value)}
                    placeholder="Nº parcelas"
                    className="w-24 px-3 py-2 bg-white dark:bg-stone-800 border border-stone-200 dark:border-stone-700 rounded-xl text-xs"
                  />
                )}
              </div>
            </div>
          )}

          <button
            type="submit"
            className={`w-full py-3.5 rounded-2xl font-bold text-white shadow-lg transition-all ${
              type === 'expense'
                ? 'bg-rose-500 hover:bg-rose-600 shadow-rose-500/20'
                : type === 'income'
                ? 'bg-emerald-500 hover:bg-emerald-600 shadow-emerald-500/20'
                : 'bg-indigo-500 hover:bg-indigo-600 shadow-indigo-500/20'
            }`}
          >
            {editingTransaction ? 'Atualizar Lançamento' : 'Salvar Lançamento'}
          </button>
        </form>
      </div>
    </div>
  );
}
