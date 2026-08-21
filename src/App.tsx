import React, { useState } from 'react';
import {
  Home,
  Receipt,
  BarChart3,
  Plus,
  ChevronLeft,
  ChevronRight,
  Menu,
  Moon,
  Sun,
  X,
  Target,
  Heart,
  HelpCircle,
  RotateCcw,
  Sliders,
  Wallet,
  MessageCircle,
} from 'lucide-react';
import { useStore } from './lib/store';
import { Dashboard } from './components/Dashboard';
import { Transactions } from './components/Transactions';
import { Comparison } from './components/Comparison';
import { ActionMenuModal } from './components/ActionMenuModal';
import { TransactionModal } from './components/TransactionModal';
import { GoalModal } from './components/GoalModal';
import { DonationModal } from './components/DonationModal';
import { TutorialTour } from './components/TutorialTour';
import { BUDGET_MODES_INFO } from './lib/utils';
import { Transaction, Goal, BudgetMode } from './types';

export default function App() {
  const store = useStore();
  const [currentTab, setCurrentTab] = useState<'dashboard' | 'transactions' | 'comparison'>('dashboard');
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);

  // Modal states
  const [isActionMenuOpen, setIsActionMenuOpen] = useState(false);
  const [isTransactionModalOpen, setIsTransactionModalOpen] = useState(false);
  const [transactionModalType, setTransactionModalType] = useState<'expense' | 'income' | 'transfer'>('expense');
  const [editingTransaction, setEditingTransaction] = useState<Transaction | null>(null);

  const [isGoalModalOpen, setIsGoalModalOpen] = useState(false);
  const [editingGoal, setEditingGoal] = useState<Goal | null>(null);

  const [isDonationModalOpen, setIsDonationModalOpen] = useState(false);
  const [isTutorialOpen, setIsTutorialOpen] = useState(false);
  const [isBudgetModeModalOpen, setIsBudgetModeModalOpen] = useState(false);
  const [isUserNameModalOpen, setIsUserNameModalOpen] = useState(false);
  const [tempUserName, setTempUserName] = useState(store.userName);

  // Month navigation
  const [yearStr, monthStr] = store.selectedMonth.split('-');
  const currentDate = new Date(Number(yearStr), Number(monthStr) - 1, 1);

  const handlePrevMonth = () => {
    const prev = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
    const m = String(prev.getMonth() + 1).padStart(2, '0');
    store.setSelectedMonth(`${prev.getFullYear()}-${m}`);
  };

  const handleNextMonth = () => {
    const next = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1);
    const m = String(next.getMonth() + 1).padStart(2, '0');
    store.setSelectedMonth(`${next.getFullYear()}-${m}`);
  };

  const monthLabel = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' }).format(currentDate);

  const handleActionSelect = (action: 'expense' | 'income' | 'transfer' | 'goal' | 'debt') => {
    if (action === 'goal') {
      setEditingGoal(null);
      setIsGoalModalOpen(true);
    } else if (action === 'debt') {
      setCurrentTab('dashboard');
    } else {
      setEditingTransaction(null);
      setTransactionModalType(action);
      setIsTransactionModalOpen(true);
    }
  };

  return (
    <div className="min-h-screen bg-stone-50 dark:bg-stone-950 text-stone-900 dark:text-stone-100 flex flex-col font-sans">
      {/* Top App Bar */}
      <header className="sticky top-0 z-30 bg-stone-50/90 dark:bg-stone-950/90 backdrop-blur-md border-b border-stone-200/60 dark:border-stone-800">
        <div className="max-w-lg mx-auto px-4 h-14 flex items-center justify-between">
          <button
            onClick={() => setIsDrawerOpen(true)}
            className="p-2 text-stone-600 dark:text-stone-400 hover:text-stone-900 dark:hover:text-stone-100 rounded-full hover:bg-stone-200/50 dark:hover:bg-stone-800"
          >
            <Menu className="w-5 h-5" />
          </button>

          {/* Month Selector */}
          <div className="flex items-center gap-1 bg-white dark:bg-stone-900 border border-stone-200/80 dark:border-stone-800 rounded-full px-2 py-1 shadow-2xs">
            <button onClick={handlePrevMonth} className="p-1 text-stone-500 hover:text-stone-900 dark:hover:text-stone-100">
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="text-xs font-bold capitalize px-2 text-stone-800 dark:text-stone-200">
              {monthLabel}
            </span>
            <button onClick={handleNextMonth} className="p-1 text-stone-500 hover:text-stone-900 dark:hover:text-stone-100">
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          <button
            onClick={() => store.setIsDarkMode(!store.isDarkMode)}
            className="p-2 text-stone-600 dark:text-stone-400 hover:text-stone-900 dark:hover:text-stone-100 rounded-full hover:bg-stone-200/50 dark:hover:bg-stone-800"
          >
            {store.isDarkMode ? <Sun className="w-5 h-5 text-amber-400" /> : <Moon className="w-5 h-5 text-stone-600" />}
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-lg w-full mx-auto p-4">
        {currentTab === 'dashboard' && (
          <Dashboard
            store={store}
            onOpenGoalModal={goal => {
              setEditingGoal(goal || null);
              setIsGoalModalOpen(true);
            }}
            onOpenBudgetModeModal={() => setIsBudgetModeModalOpen(true)}
          />
        )}
        {currentTab === 'transactions' && (
          <Transactions
            store={store}
            onEditTransaction={tx => {
              setEditingTransaction(tx);
              setTransactionModalType((tx.type as any) || 'expense');
              setIsTransactionModalOpen(true);
            }}
          />
        )}
        {currentTab === 'comparison' && <Comparison store={store} />}
      </main>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 z-30 bg-white/95 dark:bg-stone-900/95 backdrop-blur-md border-t border-stone-200/60 dark:border-stone-800">
        <div className="max-w-lg mx-auto px-6 h-16 flex items-center justify-between relative">
          <button
            onClick={() => setCurrentTab('dashboard')}
            className={`flex flex-col items-center gap-1 ${
              currentTab === 'dashboard' ? 'text-emerald-600 dark:text-emerald-400 font-bold' : 'text-stone-400'
            }`}
          >
            <Home className="w-5 h-5" />
            <span className="text-[10px]">Início</span>
          </button>

          <button
            onClick={() => setCurrentTab('transactions')}
            className={`flex flex-col items-center gap-1 ${
              currentTab === 'transactions' ? 'text-emerald-600 dark:text-emerald-400 font-bold' : 'text-stone-400'
            }`}
          >
            <Receipt className="w-5 h-5" />
            <span className="text-[10px]">Extrato</span>
          </button>

          {/* Floating Center Button */}
          <div className="absolute left-1/2 -top-5 -translate-x-1/2">
            <button
              onClick={() => setIsActionMenuOpen(true)}
              className="w-12 h-12 rounded-full bg-emerald-500 hover:bg-emerald-600 text-white flex items-center justify-center shadow-lg shadow-emerald-500/30 transition-transform active:scale-95"
            >
              <Plus className="w-6 h-6" />
            </button>
          </div>

          <button
            onClick={() => setCurrentTab('comparison')}
            className={`flex flex-col items-center gap-1 ${
              currentTab === 'comparison' ? 'text-emerald-600 dark:text-emerald-400 font-bold' : 'text-stone-400'
            }`}
          >
            <BarChart3 className="w-5 h-5" />
            <span className="text-[10px]">Análise</span>
          </button>
        </div>
      </nav>

      {/* Drawer */}
      {isDrawerOpen && (
        <div className="fixed inset-0 z-50 flex">
          <div className="fixed inset-0 bg-black/60 backdrop-blur-xs" onClick={() => setIsDrawerOpen(false)} />
          <div className="relative w-72 bg-white dark:bg-stone-900 h-full p-6 flex flex-col justify-between shadow-2xl border-r border-stone-200 dark:border-stone-800 z-10 animate-in slide-in-from-left duration-200">
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-emerald-500 text-white flex items-center justify-center font-black text-sm">
                    CF
                  </div>
                  <div>
                    <h2 className="font-bold text-sm text-stone-900 dark:text-stone-100">Clareza Financeira</h2>
                    <span className="text-[10px] text-stone-400">Mordomia & Liberdade</span>
                  </div>
                </div>
                <button onClick={() => setIsDrawerOpen(false)} className="p-1 text-stone-400">
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* User Name */}
              <div
                onClick={() => {
                  setTempUserName(store.userName);
                  setIsUserNameModalOpen(true);
                  setIsDrawerOpen(false);
                }}
                className="p-3 bg-stone-50 dark:bg-stone-800/60 rounded-2xl border border-stone-200/60 dark:border-stone-700/60 cursor-pointer"
              >
                <span className="text-[10px] font-bold uppercase text-stone-400 block">Usuário</span>
                <span className="text-xs font-bold text-stone-800 dark:text-stone-200">
                  {store.userName || 'Definir seu nome'}
                </span>
              </div>

              {/* Drawer Menu Items */}
              <div className="space-y-1">
                <button
                  onClick={() => {
                    setIsBudgetModeModalOpen(true);
                    setIsDrawerOpen(false);
                  }}
                  className="w-full flex items-center gap-3 p-2.5 rounded-xl hover:bg-stone-100 dark:hover:bg-stone-800 text-xs font-medium text-stone-700 dark:text-stone-300"
                >
                  <Sliders className="w-4 h-4 text-emerald-500" />
                  <span>Modelo de Orçamento ({store.budgetMode})</span>
                </button>

                <button
                  onClick={() => {
                    setIsTutorialOpen(true);
                    setIsDrawerOpen(false);
                  }}
                  className="w-full flex items-center gap-3 p-2.5 rounded-xl hover:bg-stone-100 dark:hover:bg-stone-800 text-xs font-medium text-stone-700 dark:text-stone-300"
                >
                  <HelpCircle className="w-4 h-4 text-stone-500" />
                  <span>Tutorial & Princípios</span>
                </button>

                <a
                  href="https://wa.me/5531983470840?text=Ol%C3%A1%2C+gostaria+de+enviar+um+feedback+sobre+o+Clareza+Financeira"
                  target="_blank"
                  rel="noreferrer"
                  className="w-full flex items-center gap-3 p-2.5 rounded-xl hover:bg-stone-100 dark:hover:bg-stone-800 text-xs font-medium text-stone-700 dark:text-stone-300"
                >
                  <MessageCircle className="w-4 h-4 text-emerald-500" />
                  <span>Fale Conosco (WhatsApp)</span>
                </a>

                <button
                  onClick={() => {
                    setIsDonationModalOpen(true);
                    setIsDrawerOpen(false);
                  }}
                  className="w-full flex items-center gap-3 p-2.5 rounded-xl hover:bg-rose-50 dark:hover:bg-rose-950/30 text-xs font-medium text-rose-600 dark:text-rose-400"
                >
                  <Heart className="w-4 h-4" />
                  <span>Apoiar com PIX</span>
                </button>

                <button
                  onClick={() => {
                    if (confirm('Tem certeza que deseja zerar todos os dados salvos? Esta ação não pode ser desfeita.')) {
                      store.resetAll();
                      setIsDrawerOpen(false);
                    }
                  }}
                  className="w-full flex items-center gap-3 p-2.5 rounded-xl hover:bg-stone-100 dark:hover:bg-stone-800 text-xs font-medium text-rose-500"
                >
                  <RotateCcw className="w-4 h-4" />
                  <span>Zerar Todos os Dados</span>
                </button>
              </div>
            </div>

            <div className="text-center text-[10px] text-stone-400">
              Clareza Financeira v1.0
              <span className="block">Criado por Wenderson Gomes</span>
            </div>
          </div>
        </div>
      )}

      {/* Modals */}
      <ActionMenuModal
        isOpen={isActionMenuOpen}
        onClose={() => setIsActionMenuOpen(false)}
        onSelectAction={handleActionSelect}
      />

      <TransactionModal
        isOpen={isTransactionModalOpen}
        onClose={() => {
          setIsTransactionModalOpen(false);
          setEditingTransaction(null);
        }}
        onSave={store.addTransaction}
        onUpdate={store.updateTransaction}
        editingTransaction={editingTransaction}
        accounts={store.accounts}
        initialType={transactionModalType}
      />

      <GoalModal
        isOpen={isGoalModalOpen}
        onClose={() => {
          setIsGoalModalOpen(false);
          setEditingGoal(null);
        }}
        onSave={store.saveGoal}
        onDelete={store.deleteGoal}
        editingGoal={editingGoal}
      />

      <DonationModal isOpen={isDonationModalOpen} onClose={() => setIsDonationModalOpen(false)} />
      <TutorialTour isOpen={isTutorialOpen} onClose={() => setIsTutorialOpen(false)} />

      {/* Budget Mode Modal */}
      {isBudgetModeModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
          <div className="w-full max-w-md bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-2xl border border-stone-100 dark:border-stone-800 max-h-[85vh] overflow-y-auto space-y-4">
            <div className="flex justify-between items-center pb-2 border-b border-stone-100 dark:border-stone-800">
              <h2 className="text-base font-bold text-stone-900 dark:text-stone-100">Modelos de Orçamento</h2>
              <button onClick={() => setIsBudgetModeModalOpen(false)} className="p-1 text-stone-400">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3">
              {(Object.entries(BUDGET_MODES_INFO) as [BudgetMode, any][]).map(([key, info]) => {
                const isSelected = store.budgetMode === key;
                return (
                  <div
                    key={key}
                    onClick={() => {
                      store.setBudgetMode(key);
                      setIsBudgetModeModalOpen(false);
                    }}
                    className={`p-4 rounded-2xl border cursor-pointer transition-all ${
                      isSelected
                        ? 'bg-emerald-50 dark:bg-emerald-950/30 border-emerald-500'
                        : 'bg-stone-50 dark:bg-stone-800/40 border-stone-200/60 dark:border-stone-700/60'
                    }`}
                  >
                    <div className="flex justify-between items-center mb-1">
                      <span className="font-bold text-xs text-stone-900 dark:text-stone-100">{info.name}</span>
                      {isSelected && <span className="text-[10px] font-black text-emerald-500">ATIVO</span>}
                    </div>
                    <p className="text-[11px] font-semibold text-emerald-600 dark:text-emerald-400 mb-1">{info.description}</p>
                    <p className="text-[10px] text-stone-500 leading-relaxed">{info.explanation}</p>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* User Name Modal */}
      {isUserNameModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
          <div className="w-full max-w-sm bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-2xl border border-stone-100 dark:border-stone-800 space-y-4">
            <h2 className="text-base font-bold text-stone-900 dark:text-stone-100">Como deseja ser chamado?</h2>
            <input
              type="text"
              value={tempUserName}
              onChange={e => setTempUserName(e.target.value)}
              placeholder="Ex: Wenderson"
              className="w-full px-4 py-2.5 bg-stone-50 dark:bg-stone-800 border border-stone-200 dark:border-stone-700 rounded-2xl text-xs"
            />
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setIsUserNameModalOpen(false)}
                className="px-4 py-2 text-xs text-stone-500 hover:bg-stone-100 rounded-xl"
              >
                Cancelar
              </button>
              <button
                onClick={() => {
                  store.setUserName(tempUserName.trim());
                  setIsUserNameModalOpen(false);
                }}
                className="px-4 py-2 text-xs font-bold bg-emerald-500 text-white rounded-xl hover:bg-emerald-600"
              >
                Salvar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
