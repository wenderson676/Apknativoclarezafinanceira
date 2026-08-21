import React, { useState } from 'react';
import { X, Heart, Copy, Check } from 'lucide-react';

interface DonationModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function DonationModal({ isOpen, onClose }: DonationModalProps) {
  const [copied, setCopied] = useState(false);
  const pixKey = '2f4304ec-b441-4cb3-91fb-e5203b7ce479';

  if (!isOpen) return null;

  const handleCopy = () => {
    navigator.clipboard.writeText(pixKey);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
      <div className="w-full max-w-md bg-white dark:bg-stone-900 rounded-3xl p-6 shadow-2xl border border-stone-100 dark:border-stone-800 text-center">
        <div className="flex justify-end">
          <button onClick={onClose} className="p-2 text-stone-400 hover:text-stone-600 dark:hover:text-stone-200 rounded-full">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="w-14 h-14 rounded-full bg-rose-500/10 dark:bg-rose-500/20 text-rose-500 flex items-center justify-center mx-auto mb-4">
          <Heart className="w-7 h-7 fill-current" />
        </div>

        <h2 className="text-xl font-black text-stone-900 dark:text-stone-100 mb-2">
          Apoie o Projeto Clareza
        </h2>

        <p className="text-xs text-stone-600 dark:text-stone-400 leading-relaxed mb-6">
          O Clareza Financeira é 100% gratuito e focado na sua liberdade financeira. Se este aplicativo tem te abençoado, considere apoiar com qualquer quantia via PIX!
        </p>

        <div className="p-4 bg-stone-50 dark:bg-stone-800/60 rounded-2xl border border-stone-200/60 dark:border-stone-700/60 mb-4 text-left">
          <span className="text-[10px] font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-400 block mb-1">
            Chave Pix (Aleatória)
          </span>
          <p className="text-xs font-mono break-all text-stone-800 dark:text-stone-200 mb-3 select-all">
            {pixKey}
          </p>
          <button
            onClick={handleCopy}
            className="w-full py-2.5 bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-xs rounded-xl flex items-center justify-center gap-2 shadow-md shadow-emerald-500/20"
          >
            {copied ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
            {copied ? 'Chave Pix Copiada!' : 'Copiar Chave Pix'}
          </button>
        </div>

        <p className="text-[11px] text-stone-500">
          Beneficiário: Wenderson Gomes • Banco Inter
        </p>
      </div>
    </div>
  );
}
