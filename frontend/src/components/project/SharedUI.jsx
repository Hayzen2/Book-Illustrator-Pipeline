import React from 'react';

export const formatStepName = (name) => {
  if (!name) return '';
  return name.toLowerCase().split('_').map((word) => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
};

export const SectionTitle = ({ title, subtitle }) => (
  <div>
    <h2 className="text-2xl font-bold text-gray-900">{title}</h2>
    <p className="mt-1 text-sm text-gray-500">{subtitle}</p>
  </div>
);

export const StatusBadge = ({ status }) => {
  const styles = {
    DRAFT: 'bg-gray-100 text-gray-600',
    PENDING: 'bg-yellow-100 text-yellow-700',
    IN_PROGRESS: 'bg-blue-100 text-blue-700',
    PROCESSING: 'bg-blue-100 text-blue-700',
    COMPLETED: 'bg-green-100 text-green-700',
    DONE: 'bg-green-100 text-green-700',
    FAILED: 'bg-red-100 text-red-700',
  };

  return (
    <span className={`px-3 py-1 rounded-full text-xs font-bold whitespace-nowrap ${styles[status] || 'bg-gray-100 text-gray-600'}`}>
      {status}
    </span>
  );
};

export const EmptySection = ({ text }) => (
  <div className="mt-5 bg-white rounded-2xl border border-dashed border-gray-300 p-10 text-center text-gray-500">
    {text}
  </div>
);

export const Spinner = () => (
  <svg className="animate-spin h-5 w-5 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
  </svg>
);