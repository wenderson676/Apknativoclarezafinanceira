import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { BudgetMode, BudgetConfig, DebtType } from '../types';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value);
}

export function formatDate(dateString: string): string {
  const [year, month, day] = dateString.split('-');
  if (!year || !month || !day) return dateString;
  const date = new Date(Number(year), Number(month) - 1, Number(day));
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date);
}

export const BUDGET_MODES: Record<BudgetMode, BudgetConfig> = {
  '50-30-20': { needs: 0.5, wants: 0.3, savings: 0.2 },
  '80-10-10': { needs: 0.8, wants: 0.1, savings: 0.1 },
  '90-5-5': { needs: 0.9, wants: 0.05, savings: 0.05 },
  '70-0-30': { needs: 0.7, wants: 0.0, savings: 0.3 },
  '50-20-30': { needs: 0.5, wants: 0.2, savings: 0.3 },
};

export const BUDGET_MODES_INFO: Record<BudgetMode, { name: string; description: string; explanation: string }> = {
  '50-30-20': {
    name: 'Padrão Equilibrado (50/30/20)',
    description: '50% Necessidades, 30% Desejos, 20% Reserva',
    explanation: 'Ideal para quem tem finanças equilibradas e quer manter um bom padrão de vida guardando parte do salário.',
  },
  '80-10-10': {
    name: 'Realidade Brasileira (80/10/10)',
    description: '80% Necessidades, 10% Desejos, 10% Reserva',
    explanation: 'Ajustado para quem ganha até 3 salários mínimos ou tem custos essenciais (aluguel, mercado) mais elevados.',
  },
  '90-5-5': {
    name: 'Modo Sobrevivência (90/5/5)',
    description: '90% Necessidades, 5% Desejos, 5% Reserva',
    explanation: 'Para momentos de crise, desemprego ou renda muito apertada. Foco total em cobrir o essencial.',
  },
  '70-0-30': {
    name: 'Ataque a Dívidas (70/0/30)',
    description: '70% Necessidades, 0% Desejos, 30% Quitação',
    explanation: 'Corte radical de supérfluos para destinar 30% da renda diretamente na eliminação de juros caros.',
  },
  '50-20-30': {
    name: 'Poupança Turbinada (50/20/30)',
    description: '50% Necessidades, 20% Desejos, 30% Reserva',
    explanation: 'Ideal para quem quer acelerar metas grandes: compra de imóvel, carro ou independência financeira.',
  },
};

export const DEBT_TYPES_INFO: Record<DebtType, { label: string; priority: string; desc: string }> = {
  card_revolving: {
    label: 'Cartão Rotativo / Fatura Atrasada',
    priority: 'Máxima',
    desc: 'Juros astronômicos (>400% a.a.). Pague imediatamente ou negocie uma portabilidade.',
  },
  overdraft: {
    label: 'Cheque Especial',
    priority: 'Máxima',
    desc: 'Juros altíssimos (>150% a.a.). Substitua por empréstimo pessoal com taxa menor.',
  },
  unsecured_loan: {
    label: 'Empréstimo Pessoal sem Garantia',
    priority: 'Média',
    desc: 'Juros intermediários (4% a 8% a.m.). Mantenha as parcelas em dia.',
  },
  payroll_loan: {
    label: 'Empréstimo Consignado',
    priority: 'Baixa',
    desc: 'Juros baixos descontados em folha. Foque primeiro nas dívidas mais caras.',
  },
  vehicle_financing: {
    label: 'Financiamento de Veículo',
    priority: 'Alta',
    desc: 'Risco de busca e apreensão do veículo em caso de inadimplência prolongada.',
  },
  real_estate: {
    label: 'Financiamento Imobiliário',
    priority: 'Moderada',
    desc: 'Juros baixos a longo prazo. Faça amortizações extraordinárias se sobrar caixa.',
  },
  essential_services: {
    label: 'Serviços Essenciais (Luz, Água, Gás, Aluguel)',
    priority: 'Máxima',
    desc: 'Risco de corte de serviço ou despejo. Deve ser priorizado antes de dívidas bancárias.',
  },
  informal_family: {
    label: 'Dívida com Família / Amigos',
    priority: 'Alta',
    desc: 'Dano relacional e moral. Estabeleça um plano formal e honre seus compromissos.',
  },
  other: {
    label: 'Outras Dívidas',
    priority: 'Média',
    desc: 'Analise o custo efetivo total e priorize pelo maior juro mensal.',
  },
};

export const CATEGORIES: Record<string, string[]> = {
  Necessidades: ['Moradia', 'Mercado & Feira', 'Transporte', 'Saúde & Farmácia', 'Educação', 'Contas Básicas', 'Outros Essenciais'],
  Desejos: ['Restaurantes & Delivery', 'Lazer & Passeios', 'Compras Pessoais', 'Assinaturas & Streaming', 'Viagens', 'Cuidados Pessoais', 'Outros Desejos'],
  'Reserva/Dívidas': ['Reserva de Emergência', 'Investimentos', 'Quitação de Dívidas', 'Aporte Cofrinho', 'Outras Reservas'],
  Renda: ['Salário', 'Freelance / Extra', 'Rendimentos', 'Venda de Itens', 'Outras Entradas'],
  Transferência: ['Transferência entre Contas', 'Aporte na Reserva', 'Resgate da Reserva'],
};

export const BIBLE_VERSES = [
  '"O que trabalha com mão remissa empobrece, mas a mão dos prudentes enriquece." — Provérbios 10:4',
  '"Honra ao Senhor com os teus bens e com as primícias de toda a tua renda." — Provérbios 3:9',
  '"Os planos bem elaborados levam à fartura; mas o apressado sempre acaba na miséria." — Provérbios 21:5',
  '"O rico domina sobre o pobre, e o que toma emprestado é servo do que empresta." — Provérbios 22:7',
  '"Quem é fiel no pouco também é fiel no muito." — Lucas 16:10',
  '"Pois qual de vós, querendo edificar uma torre, não se senta primeiro a calcular as despesas?" — Lucas 14:28',
];
