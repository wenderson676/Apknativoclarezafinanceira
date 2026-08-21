import React, { useState } from 'react';
import { Search, ArrowDownRight, ArrowUpRight, ArrowRightLeft, Clock, Trash2, Edit2, BookOpen } from 'lucide-react';
import { useStore } from '../lib/store';
import { formatCurrency, formatDate } from '../lib/utils';
import { Transaction } from '../types';

interface TransactionsProps {
  store: ReturnType<typeof useStore>;
  onEditTransaction: (tx: Transaction) => void;
}

export function Transactions({ store, onEditTransaction }: TransactionsProps) {
  const {
    currentMonthTransactions,
    deleteTransaction,
    togglePending,
    selectedMonth,
    monthNotes,
    setMonthNotes,
    accounts,
  } = store;

  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState<string>('all');
  const [isEditingNote, setIsEditingNote] = useState(false);
  const [noteText, setNoteText] = useState(monthNotes[selectedMonth] || '');

  const filtered = currentMonthTransactions.filter(t => {
    const matchesSearch =
      t.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      t.category.toLowerCase().includes(searchTerm.toLowerCase()) ||
      t.bucket.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesFilter =
      filterType === 'all'
        ? true
        : filterType === 'income'
        ? t.type === 'income' || t.bucket === 'Renda'
        : filterType === 'expense'
        ? t.type === 'expense' || (t.bucket !== 'Renda' && t.bucket !== 'Transferência')
        : filterType === 'transfer'
        ? t.type?.startsWith('transfer')
        : filterType === 'pending'
        ? !!t.isPending
        : true;

    return matchesSearch && matchesFilter;
  });

  // Group by date
  const grouped = filtered.reduce((acc, tx) => {
    if (!acc[tx.date]) acc[tx.date] = [];
    acc[tx.date].push(tx);
    return acc;
  }, {} as Record<string, Transaction[]>);

  const sortedDates = Object.keys(grouped).sort((a, b) => b.localeCompare(a));

  const handleSaveNote = () => {
    setMonthNotes(prev => ({ ...prev, [selectedMonth]: noteText }));
    setIsEditingNote(false);
  };

  return (
    <div className="space-y-6 pb-20">
      <h1 className="text-xl font-bold text-stone-900 dark:text-stone-100">
        Extrato & Lançamentos
      </h1>

      {/* Monthly Notes */}
      <div className="p-4 bg-amber-50/70 dark:bg-amber-950/20 rounded-3xl border border-amber-200/60 dark:border-amber-900/40 space-y-2">
        <div className="flex justify-between items-center">
          <div className="flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-amber-600 dark:text-amber-400" />
            <span className="text-xs font-bold text-stone-900 dark:text-stone-100">
              Anotações & Reflexões do Mês
            </span>
          </div>
          {!isEditingNote && (
            <button
              onClick={() => setIsEditingNote(true)}
              className="text-[11px] font-bold text-amber-700 dark:text-amber-400 hover:underline"
            >
              Editar
            </button>
          )}
        </div>

        {isEditingNote ? (
          <div className="space-y-2 pt-1">
            <textarea
              value={noteText}
              onChange={e => setNoteText(e.target.value)}
              placeholder="Escreva seus aprendizados, metas e observações deste mês..."
              rows={3}
              className="w-full p-3 text-xs bg-white dark:bg-stone-900 border border-stone-200 dark:border-stone-700 rounded-2xl focus:outline-hidden focus:ring-2 focus:ring-amber-500"
            />
            <div className="flex justify-end gap-2">
              <button
                onClick={() => {
                  setNoteText(monthNotes[selectedMonth] || '');
                  setIsEditingNote(false);
                }}
                className="px-3 py-1 text-xs text-stone-500 hover:bg-stone-200/50 rounded-lg"
              >
                Cancelar
              </button>
              <button
                onClick={handleSaveNote}
                className="px-3 py-1 text-xs font-bold bg-amber-500 text-white rounded-lg hover:bg-amber-600"
              >
                Salvar
              </button>
            </div>
          </div>
        ) : (
          <p
            onClick={() => setIsEditingNote(true)}
            className="text-xs text-stone-600 dark:text-stone-400 cursor-pointer italic leading-relaxed"
          >
            {monthNotes[selectedMonth] || 'Nenhuma anotação para este mês ainda. Clique para adicionar.'}
          </p>
        )}
      </div>

      {/* Search & Filters */}
      <div className="space-y-3">
        <div className="relative">
          <Search className="w-4 h-4 text-stone-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
            placeholder="Buscar lançamentos..."
            className="w-full pl-10 pr-4 py-2.5 bg-white dark:bg-stone-900 border border-stone-200/60 dark:border-stone-800 rounded-2xl text-xs text-stone-900 dark:text-stone-100 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
          />
        </div>

        <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
          {[
            { key: 'all', label: 'Todos' },
            { key: 'income', label: 'Receitas' },
            { key: 'expense', label: 'Despesas' },
            { key: 'transfer', label: 'Transferências' },
            { key: 'pending', label: 'Pendentes' },
          ].map(f => (
            <button
              key={f.key}
              onClick={() => setFilterType(f.key)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition-all ${
                filterType === f.key
                  ? 'bg-emerald-500 text-white shadow-xs'
                  : 'bg-white dark:bg-stone-900 text-stone-600 dark:text-stone-400 border border-stone-200/60 dark:border-stone-800'
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      {/* List */}
      {sortedDates.length === 0 ? (
        <div className="text-center py-12 text-stone-400 text-xs">
          Nenhum lançamento encontrado para este período.
        </div>
      ) : (
        <div className="space-y-4">
          {sortedDates.map(dateStr => (
            <div key={dateStr} className="space-y-2">
              <span className="text-[11px] font-bold text-stone-400 uppercase tracking-wider block px-1">
                {formatDate(dateStr)}
              </span>

              <div className="bg-white dark:bg-stone-900 rounded-3xl border border-stone-200/60 dark:border-stone-800 overflow-hidden shadow-xs divide-y divide-stone-100 dark:divide-stone-800">
                {grouped[dateStr].map(tx => {
                  const isIncome = tx.type === 'income' || (!tx.type && tx.bucket === 'Renda');
                  const isTransfer = tx.type?.startsWith('transfer');
                  const accObj = accounts.find(a => a.id === tx.account);

                  return (
                    <div
                      key={tx.id}
                      className="p-3.5 flex items-center justify-between hover:bg-stone-50/60 dark:hover:bg-stone-800/40 transition-colors"
                    >
                      <div className="flex items-center gap-3">
                        <button
                          onClick={() => togglePending(tx.id)}
                          className={`w-9 h-9 rounded-2xl flex items-center justify-center transition-colors ${
                            tx.isPending
                              ? 'bg-amber-100 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400'
                              : isIncome
                              ? 'bg-emerald-100 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400'
                              : isTransfer
                              ? 'bg-indigo-100 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400'
                              : 'bg-rose-100 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400'
                          }`}
                        >
                          {tx.isPending ? (
                            <Clock className="w-4 h-4" />
                          ) : isIncome ? (
                            <ArrowDownRight className="w-4 h-4" />
                          ) : isTransfer ? (
                            <ArrowRightLeft className="w-4 h-4" />
                          ) : (
                            <ArrowUpRight className="w-4 h-4" />
                          )}
                        </button>

                        <div>
                          <div className="flex items-center gap-1.5">
                            <span className="font-bold text-stone-900 dark:text-stone-100 text-xs">
                              {tx.description}
                            </span>
                            {tx.isPending && (
                              <span className="text-[9px] font-black uppercase bg-amber-100 dark:bg-amber-900/40 text-amber-700 dark:text-amber-300 px-1 py-0.2 rounded-sm">
                                Futuro
                              </span>
                            )}
                          </div>
                          <div className="flex items-center gap-1.5 text-[11px] text-stone-500">
                            <span>{accObj?.icon || '🏦'} {tx.category}</span>
                            <span>•</span>
                            <span className="text-emerald-600 dark:text-emerald-400 font-medium">{tx.bucket}</span>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center gap-2">
                        <span
                          className={`font-bold text-xs ${
                            isIncome
                              ? 'text-emerald-600 dark:text-emerald-400'
                              : isTransfer
                              ? 'text-indigo-600 dark:text-indigo-400'
                              : 'text-rose-600 dark:text-rose-400'
                          }`}
                        >
                          {isIncome ? '+' : isTransfer ? '' : '-'} {formatCurrency(tx.amount)}
                        </span>

                        <button
                          onClick={() => onEditTransaction(tx)}
                          className="p-1 text-stone-400 hover:text-stone-600"
                        >
                          <Edit2 className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => deleteTransaction(tx.id)}
                          className="p-1 text-rose-400 hover:text-rose-600"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
