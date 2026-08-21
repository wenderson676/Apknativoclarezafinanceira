import React from 'react';
import { ArrowDownRight, ArrowUpRight, ArrowRightLeft, Target, AlertTriangle, X } from 'lucide-react';

interface ActionMenuModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelectAction: (action: 'expense' | 'income' | 'transfer' | 'goal' | 'debt') => void;
}

export function ActionMenuModal({ isOpen, onClose, onSelectAction }: ActionMenuModalProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/60 backdrop-blur-xs p-0 sm:p-4 sm:items-center">
      <div className="w-full max-w-lg bg-white dark:bg-stone-900 rounded-t-3xl sm:rounded-3xl p-6 shadow-2xl border border-stone-100 dark:border-stone-800 animate-in slide-in-from-bottom duration-200">
        <div className="flex items-center justify-between pb-4 border-b border-stone-100 dark:border-stone-800">
          <div>
            <h2 className="text-lg font-bold text-stone-900 dark:text-stone-100">O que deseja registrar?</h2>
            <p className="text-xs text-stone-500">Mantenha sua mordomia financeira atualizada</p>
          </div>
          <button onClick={onClose} className="p-2 text-stone-400 hover:text-stone-600 dark:hover:text-stone-200 rounded-full hover:bg-stone-100 dark:hover:bg-stone-800">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="grid grid-cols-2 gap-3 pt-4">
          <button
            onClick={() => { onSelectAction('expense'); onClose(); }}
            className="flex flex-col items-start p-4 rounded-2xl bg-rose-50 dark:bg-rose-950/30 hover:bg-rose-100 dark:hover:bg-rose-900/40 border border-rose-100 dark:border-rose-900/50 transition-colors text-left"
          >
            <div className="w-10 h-10 rounded-full bg-rose-500 flex items-center justify-center text-white mb-3 shadow-md shadow-rose-500/20">
              <ArrowUpRight className="w-5 h-5" />
            </div>
            <span className="font-bold text-stone-900 dark:text-stone-100 text-sm">Despesa</span>
            <span className="text-xs text-stone-500">Gasto realizado</span>
          </button>

          <button
            onClick={() => { onSelectAction('income'); onClose(); }}
            className="flex flex-col items-start p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/30 hover:bg-emerald-100 dark:hover:bg-emerald-900/40 border border-emerald-100 dark:border-emerald-900/50 transition-colors text-left"
          >
            <div className="w-10 h-10 rounded-full bg-emerald-500 flex items-center justify-center text-white mb-3 shadow-md shadow-emerald-500/20">
              <ArrowDownRight className="w-5 h-5" />
            </div>
            <span className="font-bold text-stone-900 dark:text-stone-100 text-sm">Receita</span>
            <span className="text-xs text-stone-500">Entrada de valor</span>
          </button>

          <button
            onClick={() => { onSelectAction('transfer'); onClose(); }}
            className="flex flex-col items-start p-4 rounded-2xl bg-indigo-50 dark:bg-indigo-950/30 hover:bg-indigo-100 dark:hover:bg-indigo-900/40 border border-indigo-100 dark:border-indigo-900/50 transition-colors text-left"
          >
            <div className="w-10 h-10 rounded-full bg-indigo-500 flex items-center justify-center text-white mb-3 shadow-md shadow-indigo-500/20">
              <ArrowRightLeft className="w-5 h-5" />
            </div>
            <span className="font-bold text-stone-900 dark:text-stone-100 text-sm">Transferência</span>
            <span className="text-xs text-stone-500">Entre contas</span>
          </button>

          <button
            onClick={() => { onSelectAction('goal'); onClose(); }}
            className="flex flex-col items-start p-4 rounded-2xl bg-amber-50 dark:bg-amber-950/30 hover:bg-amber-100 dark:hover:bg-amber-900/40 border border-amber-100 dark:border-amber-900/50 transition-colors text-left"
          >
            <div className="w-10 h-10 rounded-full bg-amber-500 flex items-center justify-center text-white mb-3 shadow-md shadow-amber-500/20">
              <Target className="w-5 h-5" />
            </div>
            <span className="font-bold text-stone-900 dark:text-stone-100 text-sm">Nova Meta</span>
            <span className="text-xs text-stone-500">Sonho / Cofrinho</span>
          </button>
        </div>

        <div className="mt-3">
          <button
            onClick={() => { onSelectAction('debt'); onClose(); }}
            className="w-full flex items-center gap-4 p-4 rounded-2xl bg-stone-50 dark:bg-stone-800/50 hover:bg-stone-100 dark:hover:bg-stone-800 border border-stone-200/60 dark:border-stone-700/60 transition-colors text-left"
          >
            <div className="w-10 h-10 rounded-full bg-rose-500/10 dark:bg-rose-500/20 flex items-center justify-center text-rose-600 dark:text-rose-400">
              <AlertTriangle className="w-5 h-5" />
            </div>
            <div>
              <span className="font-bold text-stone-900 dark:text-stone-100 text-sm block">Cadastrar Dívida</span>
              <span className="text-xs text-stone-500">Monte seu Plano de Ataque e elimine juros</span>
            </div>
          </button>
        </div>
      </div>
    </div>
  );
}
