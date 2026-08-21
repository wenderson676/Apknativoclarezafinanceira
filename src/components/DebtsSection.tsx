import React, { useState } from 'react';
import { AlertTriangle, Plus, Trash2, Edit2, ShieldAlert, Sparkles, X } from 'lucide-react';
import { Debt, DebtType } from '../types';
import { formatCurrency, DEBT_TYPES_INFO } from '../lib/utils';

interface DebtsSectionProps {
  debts: Debt[];
  onSaveDebt: (debt: Debt) => void;
  onDeleteDebt: (id: string) => void;
}

export function DebtsSection({ debts, onSaveDebt, onDeleteDebt }: DebtsSectionProps) {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingDebt, setEditingDebt] = useState<Debt | null>(null);

  const [name, setName] = useState('');
  const [totalAmount, setTotalAmount] = useState('');
  const [monthlyPayment, setMonthlyPayment] = useState('');
  const [interestRate, setInterestRate] = useState('');
  const [isLate, setIsLate] = useState(false);
  const [creditor, setCreditor] = useState('');
  const [type, setType] = useState<DebtType>('card_revolving');

  const openCreateModal = () => {
    setEditingDebt(null);
    setName('');
    setTotalAmount('');
    setMonthlyPayment('');
    setInterestRate('');
    setIsLate(false);
    setCreditor('');
    setType('card_revolving');
    setIsModalOpen(true);
  };

  const openEditModal = (debt: Debt) => {
    setEditingDebt(debt);
    setName(debt.name);
    setTotalAmount(debt.totalAmount.toString());
    setMonthlyPayment(debt.monthlyPayment.toString());
    setInterestRate(debt.interestRate.toString());
    setIsLate(debt.isLate);
    setCreditor(debt.creditor);
    setType(debt.type || 'card_revolving');
    setIsModalOpen(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const total = parseFloat(totalAmount.replace(',', '.'));
    const monthly = parseFloat(monthlyPayment.replace(',', '.')) || 0;
    const rate = parseFloat(interestRate.replace(',', '.')) || 0;

    if (!name.trim() || isNaN(total) || total <= 0) return;

    onSaveDebt({
      id: editingDebt?.id || '',
      name: name.trim(),
      totalAmount: total,
      monthlyPayment: monthly,
      interestRate: rate,
      isLate,
      creditor: creditor.trim() || 'Não informado',
      type,
    });

    setIsModalOpen(false);
  };

  const totalDebt = debts.reduce((sum, d) => sum + d.totalAmount, 0);
  const totalMonthly = debts.reduce((sum, d) => sum + d.monthlyPayment, 0);

  // Avalanche order: highest interest first
  const sortedDebts = [...debts].sort((a, b) => b.interestRate - a.interestRate);

  return (
    <div className="space-y-4">
      <div className="bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-sm border border-stone-200/60 dark:border-stone-800">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-rose-500" />
            <h2 className="text-base font-bold text-stone-900 dark:text-stone-100">
              Plano de Ataque a Dívidas
            </h2>
          </div>
          <button
            onClick={openCreateModal}
            className="flex items-center gap-1 text-xs font-bold text-rose-600 dark:text-rose-400 bg-rose-50 dark:bg-rose-950/40 px-3 py-1.5 rounded-xl border border-rose-200 dark:border-rose-900/50 hover:bg-rose-100"
          >
            <Plus className="w-3.5 h-3.5" /> Adicionar Dívida
          </button>
        </div>

        {debts.length === 0 ? (
          <div className="text-center py-6 text-stone-500 text-xs">
            🎉 Parabéns! Nenhuma dívida cadastrada. Mantenha suas reservas em dia.
          </div>
        ) : (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3 p-4 bg-rose-50/60 dark:bg-rose-950/20 rounded-2xl border border-rose-100 dark:border-rose-900/30">
              <div>
                <span className="text-[11px] font-bold text-rose-600 dark:text-rose-400 uppercase tracking-wider block">
                  Total Devido
                </span>
                <span className="text-lg font-black text-rose-700 dark:text-rose-300">
                  {formatCurrency(totalDebt)}
                </span>
              </div>
              <div>
                <span className="text-[11px] font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider block">
                  Parcelas Mensais
                </span>
                <span className="text-lg font-bold text-stone-900 dark:text-stone-100">
                  {formatCurrency(totalMonthly)}
                </span>
              </div>
            </div>

            <div className="space-y-2">
              {sortedDebts.map(debt => {
                const info = DEBT_TYPES_INFO[debt.type || 'other'];
                return (
                  <div
                    key={debt.id}
                    className="flex items-center justify-between p-3.5 bg-stone-50 dark:bg-stone-800/40 rounded-2xl border border-stone-100 dark:border-stone-800"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-stone-900 dark:text-stone-100 text-xs">
                          {debt.name}
                        </span>
                        {debt.isLate && (
                          <span className="text-[9px] font-bold uppercase bg-rose-500 text-white px-1.5 py-0.5 rounded-sm">
                            Atrasada
                          </span>
                        )}
                        <span className="text-[10px] text-stone-500">
                          ({info?.priority} Prioridade)
                        </span>
                      </div>
                      <span className="text-[11px] text-stone-500 block">
                        Credor: {debt.creditor} • Juros: {debt.interestRate}% a.m. • Parcela: {formatCurrency(debt.monthlyPayment)}
                      </span>
                    </div>

                    <div className="flex items-center gap-3">
                      <span className="font-bold text-rose-600 dark:text-rose-400 text-xs">
                        {formatCurrency(debt.totalAmount)}
                      </span>
                      <button
                        onClick={() => openEditModal(debt)}
                        className="p-1.5 text-stone-400 hover:text-stone-600 dark:hover:text-stone-200"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => onDeleteDebt(debt.id)}
                        className="p-1.5 text-rose-400 hover:text-rose-600"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
          <div className="w-full max-w-lg bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-2xl border border-stone-100 dark:border-stone-800 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between pb-4 border-b border-stone-100 dark:border-stone-800">
              <h2 className="text-lg font-bold text-stone-900 dark:text-stone-100">
                {editingDebt ? 'Editar Dívida' : 'Cadastrar Dívida'}
              </h2>
              <button onClick={() => setIsModalOpen(false)} className="p-2 text-stone-400 hover:text-stone-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4 mt-4">
              <div>
                <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
                  Tipo de Dívida / Gravidade
                </label>
                <select
                  value={type}
                  onChange={e => setType(e.target.value as DebtType)}
                  className="w-full px-4 py-2.5 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-stone-900 dark:text-stone-100"
                >
                  {Object.entries(DEBT_TYPES_INFO).map(([key, info]) => (
                    <option key={key} value={key}>{info.label} ({info.priority})</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
                  Nome da Dívida
                </label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={e => setName(e.target.value)}
                  placeholder="Ex: Cartão Nubank, Empréstimo Caixa"
                  className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-stone-900 dark:text-stone-100"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
                    Total Devido (R$)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    value={totalAmount}
                    onChange={e => setTotalAmount(e.target.value)}
                    placeholder="0,00"
                    className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl font-bold"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
                    Parcela Mensal (R$)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={monthlyPayment}
                    onChange={e => setMonthlyPayment(e.target.value)}
                    placeholder="0,00"
                    className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
                    Taxa Juros Mensal (%)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={interestRate}
                    onChange={e => setInterestRate(e.target.value)}
                    placeholder="Ex: 3.5"
                    className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-stone-600 dark:text-stone-400 uppercase tracking-wider mb-1">
                    Credor
                  </label>
                  <input
                    type="text"
                    value={creditor}
                    onChange={e => setCreditor(e.target.value)}
                    placeholder="Ex: Banco do Brasil"
                    className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl"
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 pt-2">
                <input
                  type="checkbox"
                  id="isLate"
                  checked={isLate}
                  onChange={e => setIsLate(e.target.checked)}
                  className="rounded-md border-stone-300 text-rose-600 focus:ring-rose-500 w-4 h-4"
                />
                <label htmlFor="isLate" className="text-xs font-medium text-stone-700 dark:text-stone-300 cursor-pointer">
                  Dívida em Atraso / Negativada
                </label>
              </div>

              <button
                type="submit"
                className="w-full py-3.5 bg-rose-500 hover:bg-rose-600 text-white font-bold rounded-2xl shadow-lg shadow-rose-500/20"
              >
                {editingDebt ? 'Salvar Alterações' : 'Cadastrar Dívida'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
