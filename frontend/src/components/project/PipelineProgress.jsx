import React from 'react';
import { formatStepName } from './SharedUI';

const STEP_ORDER = ['STYLE', 'CHARACTERS', 'PORTRAITS', 'CHAPTERS', 'ILLUSTRATIONS'];

const StepItem = ({ step }) => {
  const isCompleted = step.status === 'COMPLETED';
  const isFailed = step.status === 'FAILED';
  const isProcessing = step.status === 'IN_PROGRESS';

  let icon = '○';
  if (isCompleted) icon = '✓';
  else if (isFailed) icon = '!';
  else if (isProcessing) icon = '●';

  return (
    <div className={`flex items-start gap-4 p-4 rounded-xl border ${isProcessing ? 'border-blue-300 bg-blue-50/50' : 'border-transparent'}`}>
      <div className={`w-9 h-9 rounded-full flex-shrink-0 flex items-center justify-center font-bold ${
            isCompleted ? 'bg-green-100 text-green-600' : 
            isFailed ? 'bg-red-100 text-red-600' : 
            isProcessing ? 'bg-blue-100 text-blue-600 animate-pulse' : 
            'bg-gray-100 text-gray-400'
        }`}>
        {icon}
      </div>
      <div className="flex-1">
        <div className="flex items-center justify-between">
          <h3 className={`font-semibold ${isProcessing ? 'text-blue-900' : 'text-gray-900'}`}>
            {formatStepName(step.stepName)}
          </h3>
          <span className={`text-xs font-medium px-2 py-1 rounded-md ${
            isProcessing ? 'bg-blue-100 text-blue-700' : 
            isFailed ? 'bg-red-100 text-red-700' : 
            isCompleted ? 'bg-green-100 text-green-700' : 
            'bg-gray-100 text-gray-500'
          }`}>
            {step.status}
          </span>
        </div>
        {step.errorMessage && (
          <p className="mt-2 text-sm text-red-500 bg-red-50 p-2 rounded-md">
            {step.errorMessage}
          </p>
        )}
        {step.updatedAt && (
          <p className="mt-1 text-xs text-gray-400">
            Last updated: {new Date(step.updatedAt).toLocaleString()}
          </p>
        )}
      </div>
    </div>
  );
};

export const PipelineProgress = ({ steps }) => {
  if (!steps || steps.length === 0) {
    return <p className="text-gray-500">No project steps available.</p>;
  }

  return (
    <div className="space-y-4">
      {STEP_ORDER.map((sName) => {
        const step = steps.find(s => s.stepName === sName);
        if (!step) return null;
        return <StepItem key={step.stepName} step={step} />;
      })}
    </div>
  );
};