import React, { useState } from 'react';
import { useStore } from '../lib/store';
import { CashFlowAnalyzer } from './CashFlowAnalyzer';
import { formatCurrency } from '../lib/utils';
import { Sparkles, BarChart2 } from 'lucide-react';

interface ComparisonProps {
  store: ReturnType<typeof useStore>;
}

export function Comparison({ store }: ComparisonProps) {
  const [subTab, setSubTab] = useState<'diagnostic' | 'categories'>('diagnostic');
  const { currentMonthTransactions } = store;

  // Group expenses by category
  const categoryExpenses = currentMonthTransactions
    .filter(t => (t.type === 'expense' || (!t.type && t.bucket !== 'Renda' && t.bucket !== 'Transferência')) && !t.isPending)
    .reduce((acc, t) => {
      acc[t.category] = (acc[t.category] || 0) + t.amount;
      return acc;
    }, {} as Record<string, number>);

  const sortedCategories = Object.entries(categoryExpenses).sort((a, b) => b[1] - a[1]);
  const maxExpense = sortedCategories.length > 0 ? sortedCategories[0][1] : 1;

  return (
    <div className="space-y-6 pb-20">
      {/* Header banner */}
      <div className="p-6 bg-emerald-900 text-white rounded-3xl space-y-2 shadow-md">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">
            <Sparkles className="w-4 h-4 text-emerald-200" />
          </div>
          <div>
            <span className="text-[10px] font-black text-emerald-200 uppercase tracking-widest block">
              Diagnóstico & Inteligência
            </span>
            <h1 className="text-lg font-bold">Análise Financeira Avançada</h1>
          </div>
        </div>
        <p className="text-xs text-emerald-100/80 leading-relaxed">
          Acompanhe a saúde do seu orçamento, elegibilidade de planos e simulações para decisões conscientes.
        </p>
      </div>

      {/* Tabs */}
      <div className="flex bg-stone-100 dark:bg-stone-800 p-1 rounded-2xl">
        <button
          onClick={() => setSubTab('diagnostic')}
          className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all ${
            subTab === 'diagnostic'
              ? 'bg-white dark:bg-stone-900 text-stone-900 dark:text-stone-100 shadow-xs'
              : 'text-stone-500'
          }`}
        >
          Diagnóstico Inteligente
        </button>
        <button
          onClick={() => setSubTab('categories')}
          className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all ${
            subTab === 'categories'
              ? 'bg-white dark:bg-stone-900 text-stone-900 dark:text-stone-100 shadow-xs'
              : 'text-stone-500'
          }`}
        >
          Gastos por Categoria
        </button>
      </div>

      {subTab === 'diagnostic' ? (
        <CashFlowAnalyzer store={store} />
      ) : (
        <div className="bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-sm border border-stone-200/60 dark:border-stone-800 space-y-4">
          <h2 className="text-base font-bold text-stone-900 dark:text-stone-100">
            Distribuição de Gastos do Mês
          </h2>

          {sortedCategories.length === 0 ? (
            <p className="text-xs text-stone-500 py-6 text-center">Nenhum gasto registrado neste mês.</p>
          ) : (
            <div className="space-y-3">
              {sortedCategories.map(([cat, amount]) => {
                const pct = Math.round((amount / maxExpense) * 100);
                return (
                  <div key={cat} className="space-y-1">
                    <div className="flex justify-between text-xs">
                      <span className="font-medium text-stone-700 dark:text-stone-300">{cat}</span>
                      <span className="font-bold text-stone-900 dark:text-stone-100">{formatCurrency(amount)}</span>
                    </div>
                    <div className="w-full bg-stone-100 dark:bg-stone-800 h-2 rounded-full overflow-hidden">
                      <div className="bg-rose-500 h-full rounded-full" style={{ width: `${pct}%` }} />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
