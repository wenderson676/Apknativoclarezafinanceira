import React, { useState } from 'react';
import { ShieldCheck, AlertCircle, CheckCircle2, TrendingUp, Sparkles } from 'lucide-react';
import { useStore } from '../lib/store';
import { formatCurrency, BUDGET_MODES_INFO, BUDGET_MODES } from '../lib/utils';
import { BudgetMode } from '../types';

interface CashFlowAnalyzerProps {
  store: ReturnType<typeof useStore>;
}

export function CashFlowAnalyzer({ store }: CashFlowAnalyzerProps) {
  const { totalIncome, totalExpenses, bucketExpenses, setBudgetMode, budgetMode, liquidBalance } = store;

  const [simulatedPrice, setSimulatedPrice] = useState('');

  const needsSpent = bucketExpenses.Necessidades || 0;
  const wantsSpent = bucketExpenses.Desejos || 0;
  const savingsSpent = bucketExpenses['Reserva/Dívidas'] || 0;

  const needsRatio = totalIncome > 0 ? needsSpent / totalIncome : 0;
  const wantsRatio = totalIncome > 0 ? wantsSpent / totalIncome : 0;
  const savingsRatio = totalIncome > 0 ? savingsSpent / totalIncome : 0;

  // Health score (0 - 100)
  let healthScore = 50;
  if (totalIncome > 0) {
    if (totalExpenses <= totalIncome) healthScore += 25;
    if (needsRatio <= 0.6) healthScore += 15;
    if (savingsRatio >= 0.15) healthScore += 10;
    if (totalExpenses > totalIncome) healthScore -= 30;
  }
  healthScore = Math.max(0, Math.min(100, healthScore));

  // Eligibility check for all budget modes
  const modeEligibility = (Object.keys(BUDGET_MODES) as BudgetMode[]).map(mode => {
    const config = BUDGET_MODES[mode];
    const isEligible = needsRatio <= config.needs + 0.05 && wantsRatio <= config.wants + 0.05;
    return {
      mode,
      info: BUDGET_MODES_INFO[mode],
      config,
      isEligible,
    };
  });

  const simAmount = parseFloat(simulatedPrice.replace(',', '.')) || 0;
  const simRemaining = liquidBalance - simAmount;
  const isSafePurchase = simRemaining >= (totalExpenses * 0.2) && simRemaining >= 0;

  return (
    <div className="space-y-6">
      {/* Score */}
      <div className="bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-sm border border-stone-200/60 dark:border-stone-800 space-y-3">
        <div className="flex justify-between items-center">
          <span className="text-xs font-bold text-stone-500 uppercase tracking-wider">
            Pontuação de Saúde Financeira
          </span>
          <span className={`text-lg font-black ${healthScore >= 70 ? 'text-emerald-500' : healthScore >= 50 ? 'text-amber-500' : 'text-rose-500'}`}>
            {healthScore}/100
          </span>
        </div>

        <div className="w-full bg-stone-100 dark:bg-stone-800 h-3 rounded-full overflow-hidden">
          <div
            className={`h-full rounded-full transition-all ${
              healthScore >= 70 ? 'bg-emerald-500' : healthScore >= 50 ? 'bg-amber-500' : 'bg-rose-500'
            }`}
            style={{ width: `${healthScore}%` }}
          />
        </div>

        <div className="flex justify-between text-[11px] text-stone-500 pt-1">
          <span>Necessidades: {Math.round(needsRatio * 100)}%</span>
          <span>Desejos: {Math.round(wantsRatio * 100)}%</span>
          <span>Reserva: {Math.round(savingsRatio * 100)}%</span>
        </div>
      </div>

      {/* Mode matrix */}
      <div className="bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-sm border border-stone-200/60 dark:border-stone-800 space-y-4">
        <h2 className="text-base font-bold text-stone-900 dark:text-stone-100">
          Elegibilidade de Modelos
        </h2>

        <div className="space-y-3">
          {modeEligibility.map(({ mode, info, isEligible }) => (
            <div
              key={mode}
              className={`p-4 rounded-2xl border transition-all ${
                isEligible
                  ? 'bg-emerald-50/40 dark:bg-emerald-950/20 border-emerald-200/80 dark:border-emerald-800/40'
                  : 'bg-rose-50/40 dark:bg-rose-950/20 border-rose-200/80 dark:border-rose-800/40'
              }`}
            >
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-stone-900 dark:text-stone-100 text-xs">
                      {info.name}
                    </span>
                    {mode === budgetMode && (
                      <span className="text-[9px] font-black uppercase bg-emerald-500 text-white px-1.5 py-0.5 rounded-sm">
                        Ativo
                      </span>
                    )}
                  </div>
                  <span className="text-[11px] text-stone-500 block mt-0.5">{info.description}</span>
                  <span className="text-[10px] text-stone-400 block mt-1">{info.explanation}</span>
                </div>

                <div className="flex flex-col items-end gap-2">
                  {isEligible ? (
                    <CheckCircle2 className="w-5 h-5 text-emerald-500 shrink-0" />
                  ) : (
                    <AlertCircle className="w-5 h-5 text-rose-500 shrink-0" />
                  )}
                  {mode !== budgetMode && isEligible && (
                    <button
                      onClick={() => setBudgetMode(mode)}
                      className="text-[10px] font-bold text-emerald-600 dark:text-emerald-400 hover:underline"
                    >
                      Aplicar
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Simulator */}
      <div className="bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-sm border border-stone-200/60 dark:border-stone-800 space-y-4">
        <h2 className="text-base font-bold text-stone-900 dark:text-stone-100">
          Simulador: Posso Comprar Hoje?
        </h2>
        <p className="text-xs text-stone-500 leading-relaxed">
          Avalie se uma compra à vista não vai comprometer o seu caixa de emergência:
        </p>

        <div className="space-y-3">
          <input
            type="number"
            value={simulatedPrice}
            onChange={e => setSimulatedPrice(e.target.value)}
            placeholder="Valor planejado da compra (R$)"
            className="w-full px-4 py-2.5 bg-stone-50 dark:bg-stone-800/60 border border-stone-200 dark:border-stone-700 rounded-2xl text-xs"
          />

          {simAmount > 0 && (
            <div
              className={`p-4 rounded-2xl border ${
                isSafePurchase
                  ? 'bg-emerald-50 dark:bg-emerald-950/20 border-emerald-200 dark:border-emerald-900/40 text-emerald-800 dark:text-emerald-300'
                  : 'bg-rose-50 dark:bg-rose-950/20 border-rose-200 dark:border-rose-900/40 text-rose-800 dark:text-rose-300'
              }`}
            >
              <span className="font-bold text-xs block mb-1">
                {isSafePurchase ? '✅ Compra Segura e Aprovada' : '⚠️ Risco ao Orçamento'}
              </span>
              <p className="text-[11px] leading-relaxed">
                {isSafePurchase
                  ? `Após esta compra de ${formatCurrency(simAmount)}, você ainda manterá ${formatCurrency(simRemaining)} em conta corrente.`
                  : `Esta compra deixará você com apenas ${formatCurrency(simRemaining)}, abaixo da margem de segurança recomendada.`}
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
