import React, { useState, useEffect } from 'react';
import { X, Target } from 'lucide-react';
import { Goal } from '../types';

interface GoalModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (goal: Goal) => void;
  onDelete?: (id: string) => void;
  editingGoal?: Goal | null;
}

export function GoalModal({ isOpen, onClose, onSave, onDelete, editingGoal }: GoalModalProps) {
  const [title, setTitle] = useState('');
  const [targetAmount, setTargetAmount] = useState('');
  const [currentAmount, setCurrentAmount] = useState('');

  useEffect(() => {
    if (editingGoal) {
      setTitle(editingGoal.title);
      setTargetAmount(editingGoal.targetAmount.toString());
      setCurrentAmount(editingGoal.currentAmount.toString());
    } else {
      setTitle('');
      setTargetAmount('');
      setCurrentAmount('0');
    }
  }, [editingGoal, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const target = parseFloat(targetAmount.replace(',', '.'));
    const current = parseFloat(currentAmount.replace(',', '.')) || 0;
    if (isNaN(target) || target <= 0 || !title.trim()) return;

    onSave({
      id: editingGoal?.id || '',
      title: title.trim(),
      targetAmount: target,
      currentAmount: current,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
      <div className="w-full max-w-md bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-2xl border border-stone-100 dark:border-stone-800">
        <div className="flex items-center justify-between pb-4 border-b border-stone-100 dark:border-stone-800">
          <div className="flex items-center gap-2">
            <Target className="w-5 h-5 text-emerald-500" />
            <h2 className="text-lg font-bold text-stone-900 dark:text-stone-100">
              {editingGoal ? 'Editar Meta' : 'Nova Meta / Sonho'}
            </h2>
          </div>
          <button onClick={onClose} className="p-2 text-stone-400 hover:text-stone-600 dark:hover:text-stone-200 rounded-full">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div>
            <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
              Nome da Meta
            </label>
            <input
              type="text"
              required
              value={title}
              onChange={e => setTitle(e.target.value)}
              placeholder="Ex: Reserva de Emergência, Viagem"
              className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-stone-900 dark:text-stone-100 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
              Valor Alvo (R$)
            </label>
            <input
              type="number"
              step="0.01"
              required
              value={targetAmount}
              onChange={e => setTargetAmount(e.target.value)}
              placeholder="0,00"
              className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-stone-900 dark:text-stone-100 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 font-bold"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
              Valor Já Guardado (R$)
            </label>
            <input
              type="number"
              step="0.01"
              value={currentAmount}
              onChange={e => setCurrentAmount(e.target.value)}
              placeholder="0,00"
              className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-stone-900 dark:text-stone-100 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
            />
          </div>

          <div className="flex gap-2 pt-2">
            {editingGoal && onDelete && (
              <button
                type="button"
                onClick={() => { onDelete(editingGoal.id); onClose(); }}
                className="py-3.5 px-4 bg-rose-100 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400 font-bold rounded-2xl"
              >
                Excluir
              </button>
            )}
            <button
              type="submit"
              className="flex-1 py-3.5 bg-emerald-500 hover:bg-emerald-600 text-white font-bold rounded-2xl shadow-lg shadow-emerald-500/20"
            >
              {editingGoal ? 'Salvar Alterações' : 'Criar Meta'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
