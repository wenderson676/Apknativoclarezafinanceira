import React, { useState } from 'react';
import { X, ArrowRight, ArrowLeft, Check } from 'lucide-react';

interface TutorialTourProps {
  isOpen: boolean;
  onClose: () => void;
}

const STEPS = [
  {
    title: 'Bem-vindo ao Clareza Financeira!',
    desc: 'Um aplicativo offline, privativo e transparente, desenhado para trazer paz, consciência e fidelidade na administração do seu dinheiro.',
    icon: '✨',
  },
  {
    title: 'A Regra dos Potes (50/30/20)',
    desc: 'Divida seus recursos com sabedoria: 50% para Necessidades básicas, 30% para Desejos e estilo de vida, e 20% para sua Reserva e quitação de Dívidas.',
    icon: '🏺',
  },
  {
    title: 'Registro Consciente e Manual',
    desc: 'Ao lançar seus gastos manualmente todo dia, você assume o controle consciente de cada centavo e evita compras por impulso.',
    icon: '📝',
  },
  {
    title: 'Plano de Ataque a Dívidas',
    desc: 'Classifique suas pendências por gravidade e acompanhe a previsão exata de quando estará 100% livre delas.',
    icon: '🎯',
  },
  {
    title: 'Privacidade e Segurança Total',
    desc: 'Seus dados ficam gravados exclusivamente no seu navegador / dispositivo. Sem servidores externos e sem compartilhamento de dados.',
    icon: '🔒',
  },
];

export function TutorialTour({ isOpen, onClose }: TutorialTourProps) {
  const [currentStep, setCurrentStep] = useState(0);

  if (!isOpen) return null;

  const step = STEPS[currentStep];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
      <div className="w-full max-w-md bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-2xl border border-stone-100 dark:border-stone-800 text-center">
        <div className="flex justify-end">
          <button onClick={onClose} className="p-2 text-stone-400 hover:text-stone-600 dark:hover:text-stone-200 rounded-full">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="w-16 h-16 rounded-full bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center mx-auto mb-4 text-3xl">
          {step.icon}
        </div>

        <h2 className="text-xl font-black text-stone-900 dark:text-stone-100 mb-2">
          {step.title}
        </h2>

        <p className="text-xs text-stone-600 dark:text-stone-400 leading-relaxed mb-6">
          {step.desc}
        </p>

        <div className="flex justify-center gap-1.5 mb-6">
          {STEPS.map((_, idx) => (
            <div
              key={idx}
              className={`h-2 rounded-full transition-all ${
                idx === currentStep ? 'w-6 bg-emerald-500' : 'w-2 bg-stone-200 dark:bg-stone-700'
              }`}
            />
          ))}
        </div>

        <div className="flex justify-between items-center gap-3">
          {currentStep > 0 ? (
            <button
              onClick={() => setCurrentStep(prev => prev - 1)}
              className="py-3 px-4 rounded-2xl text-xs font-bold text-stone-600 dark:text-stone-300 flex items-center gap-1 hover:bg-stone-100 dark:hover:bg-stone-800"
            >
              <ArrowLeft className="w-4 h-4" /> Anterior
            </button>
          ) : <div />}

          {currentStep < STEPS.length - 1 ? (
            <button
              onClick={() => setCurrentStep(prev => prev + 1)}
              className="py-3 px-5 bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-xs rounded-2xl flex items-center gap-1 shadow-md shadow-emerald-500/20"
            >
              Próximo <ArrowRight className="w-4 h-4" />
            </button>
          ) : (
            <button
              onClick={onClose}
              className="py-3 px-5 bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-xs rounded-2xl flex items-center gap-1 shadow-md shadow-emerald-500/20"
            >
              Começar Agora! <Check className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
