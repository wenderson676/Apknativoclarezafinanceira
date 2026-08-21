import React from 'react';
import { ArrowDownRight, ArrowUpRight, ArrowRightLeft, BookOpen, Plus, Target, RefreshCw } from 'lucide-react';
import { useStore } from '../lib/store';
import { formatCurrency, BUDGET_MODES_INFO } from '../lib/utils';
import { DebtsSection } from './DebtsSection';
import { Goal } from '../types';

interface DashboardProps {
  store: ReturnType<typeof useStore>;
  onOpenGoalModal: (goal?: Goal) => void;
  onOpenBudgetModeModal: () => void;
}

export function Dashboard({ store, onOpenGoalModal, onOpenBudgetModeModal }: DashboardProps) {
  const {
    userName,
    budgetMode,
    totalIncome,
    totalExpenses,
    netSavingsTransfer,
    monthResult,
    bucketLimits,
    bucketExpenses,
    accountBalances,
    totalBalance,
    liquidBalance,
    accounts,
    goals,
    debts,
    saveDebt,
    deleteDebt,
    dailyVerse,
    refreshVerse,
  } = store;

  return (
    <div className="space-y-6 pb-20">
      {/* Header Greeting & Mode */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-stone-900 dark:text-stone-100">
            {userName ? `Olá, ${userName}!` : 'Paz e Graça!'}
          </h1>
          <p className="text-xs text-stone-500">Mordomia e clareza no seu dia a dia</p>
        </div>
        <button
          onClick={onOpenBudgetModeModal}
          className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800/60 rounded-xl text-xs font-bold text-emerald-600 dark:text-emerald-400"
        >
          <span>{budgetMode}</span>
        </button>
      </div>

      {/* Account Balances Carousel */}
      <div className="space-y-2">
        <div className="flex justify-between items-center">
          <span className="text-xs font-bold uppercase tracking-wider text-stone-500">Minhas Contas</span>
        </div>
        <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-none">
          <div className="min-w-[180px] p-4 bg-emerald-900 text-white rounded-3xl flex flex-col justify-between shadow-md">
            <span className="text-xs font-medium text-emerald-200">Patrimônio Total</span>
            <div className="my-2">
              <span className="text-lg font-black">{formatCurrency(totalBalance)}</span>
              <span className="block text-[11px] text-emerald-300">Líquido: {formatCurrency(liquidBalance)}</span>
            </div>
            <span className="text-[10px] text-emerald-300/80">Soma de todas as contas</span>
          </div>

          {accounts.map(acc => (
            <div key={acc.id} className="min-w-[160px] p-4 bg-white dark:bg-stone-900 rounded-3xl border border-stone-200/60 dark:border-stone-800 flex flex-col justify-between shadow-xs">
              <div className="flex items-center gap-2">
                <span className="text-base">{acc.icon}</span>
                <span className="text-xs font-bold text-stone-700 dark:text-stone-300 truncate">{acc.name}</span>
              </div>
              <div className="my-2">
                <span className="text-base font-bold text-stone-900 dark:text-stone-100">
                  {formatCurrency(accountBalances[acc.id] || 0)}
                </span>
              </div>
              <span className="text-[10px] text-stone-400">
                {acc.type === 'reserva' ? 'Cofrinho / Reserva' : 'Conta Corrente'}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Monthly Overview Card */}
      <div className="bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-sm border border-stone-200/60 dark:border-stone-800 space-y-4">
        <div className="flex justify-between items-center">
          <h2 className="text-base font-bold text-stone-900 dark:text-stone-100">Balanço do Mês</h2>
          <span className={`text-xs font-bold ${monthResult >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-600 dark:text-rose-400'}`}>
            {monthResult >= 0 ? `+${formatCurrency(monthResult)}` : formatCurrency(monthResult)}
          </span>
        </div>

        <div className="grid grid-cols-3 gap-2 pt-2 border-t border-stone-100 dark:border-stone-800">
          <div>
            <div className="flex items-center gap-1 text-[11px] text-stone-500 mb-1">
              <div className="w-2 h-2 rounded-full bg-emerald-500" />
              <span>Receitas</span>
            </div>
            <span className="text-sm font-bold text-emerald-600 dark:text-emerald-400 block">
              {formatCurrency(totalIncome)}
            </span>
          </div>

          <div>
            <div className="flex items-center gap-1 text-[11px] text-stone-500 mb-1">
              <div className="w-2 h-2 rounded-full bg-rose-500" />
              <span>Despesas</span>
            </div>
            <span className="text-sm font-bold text-rose-600 dark:text-rose-400 block">
              {formatCurrency(totalExpenses)}
            </span>
          </div>

          <div>
            <div className="flex items-center gap-1 text-[11px] text-stone-500 mb-1">
              <div className="w-2 h-2 rounded-full bg-indigo-500" />
              <span>Poupado</span>
            </div>
            <span className="text-sm font-bold text-indigo-600 dark:text-indigo-400 block">
              {formatCurrency(netSavingsTransfer)}
            </span>
          </div>
        </div>
      </div>

      {/* 50/30/20 Buckets Allocation */}
      <div className="bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-sm border border-stone-200/60 dark:border-stone-800 space-y-4">
        <h2 className="text-base font-bold text-stone-900 dark:text-stone-100">
          Divisão dos Potes ({budgetMode})
        </h2>

        {(['Necessidades', 'Desejos', 'Reserva/Dívidas'] as const).map(bucketKey => {
          const limit = bucketLimits[bucketKey] || 0;
          const spent = bucketExpenses[bucketKey] || 0;
          const percentage = limit > 0 ? Math.min(100, Math.round((spent / limit) * 100)) : 0;
          const isOver = spent > limit && limit > 0;

          return (
            <div key={bucketKey} className="space-y-1.5">
              <div className="flex justify-between text-xs">
                <span className="font-bold text-stone-700 dark:text-stone-300">{bucketKey}</span>
                <span className="text-stone-500">
                  {formatCurrency(spent)} / {formatCurrency(limit)} ({percentage}%)
                </span>
              </div>
              <div className="w-full bg-stone-100 dark:bg-stone-800 h-2.5 rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all ${
                    isOver ? 'bg-rose-500' : percentage > 85 ? 'bg-amber-500' : 'bg-emerald-500'
                  }`}
                  style={{ width: `${Math.min(100, percentage)}%` }}
                />
              </div>
            </div>
          );
        })}
      </div>

      {/* Daily Bible Verse */}
      <div className="p-4 bg-stone-100/70 dark:bg-stone-900/60 rounded-3xl border border-stone-200/60 dark:border-stone-800 flex items-center gap-3">
        <BookOpen className="w-5 h-5 text-emerald-600 shrink-0" />
        <p className="text-xs italic text-stone-600 dark:text-stone-400 flex-1 leading-relaxed">
          {dailyVerse}
        </p>
        <button onClick={refreshVerse} className="text-stone-400 hover:text-stone-600 p-1">
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Goals / Metas */}
      <div className="bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-sm border border-stone-200/60 dark:border-stone-800 space-y-4">
        <div className="flex justify-between items-center">
          <h2 className="text-base font-bold text-stone-900 dark:text-stone-100">Metas & Cofrinhos</h2>
          <button
            onClick={() => onOpenGoalModal()}
            className="flex items-center gap-1 text-xs font-bold text-emerald-600 dark:text-emerald-400 hover:underline"
          >
            <Plus className="w-4 h-4" /> Nova Meta
          </button>
        </div>

        {goals.length === 0 ? (
          <p className="text-xs text-stone-500">Nenhuma meta cadastrada ainda.</p>
        ) : (
          <div className="space-y-3">
            {goals.map(goal => {
              const pct = goal.targetAmount > 0 ? Math.min(100, Math.round((goal.currentAmount / goal.targetAmount) * 100)) : 0;
              return (
                <div
                  key={goal.id}
                  onClick={() => onOpenGoalModal(goal)}
                  className="p-3.5 bg-stone-50 dark:bg-stone-800/40 rounded-2xl border border-stone-100 dark:border-stone-800 cursor-pointer hover:border-emerald-300 transition-all space-y-2"
                >
                  <div className="flex justify-between items-center text-xs">
                    <span className="font-bold text-stone-900 dark:text-stone-100">{goal.title}</span>
                    <span className="font-bold text-emerald-600 dark:text-emerald-400">{pct}%</span>
                  </div>
                  <div className="w-full bg-stone-200 dark:bg-stone-700 h-2 rounded-full overflow-hidden">
                    <div className="bg-emerald-500 h-full rounded-full" style={{ width: `${pct}%` }} />
                  </div>
                  <div className="flex justify-between text-[11px] text-stone-500">
                    <span>{formatCurrency(goal.currentAmount)}</span>
                    <span>Meta: {formatCurrency(goal.targetAmount)}</span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Debts section */}
      <DebtsSection debts={debts} onSaveDebt={saveDebt} onDeleteDebt={deleteDebt} />
    </div>
  );
}
