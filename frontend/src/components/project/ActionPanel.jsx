import React from 'react';
import { formatStepName, Spinner } from './SharedUI';

export const ActionPanel = ({ activeStep, executing, onExecute, customStyle, setCustomStyle }) => {
  if (!activeStep) {
    return (
      <div className="text-center py-6">
        <div className="text-4xl mb-3">🎉</div>
        <h3 className="text-lg font-bold text-gray-900">All Done!</h3>
        <p className="text-sm text-gray-500 mt-1">Your illustrated book is completely generated.</p>
      </div>
    );
  }

  return (
    <div>
      <h3 className="font-bold text-gray-900 mb-2">
        Next Step: {formatStepName(activeStep.stepName)}
      </h3>
      
      {activeStep.status === 'IN_PROGRESS' ? (
        <div className="p-4 bg-blue-50 border border-blue-200 rounded-xl text-blue-700 flex items-center gap-3">
          <Spinner />
          <span className="text-sm font-semibold">Running... This may take a minute.</span>
        </div>
      ) : (
        <div className="space-y-4">
          {activeStep.status === 'FAILED' && (
            <div className="p-3 bg-red-50 text-red-700 text-sm rounded-lg border border-red-200">
              <strong>Failed:</strong> {activeStep.errorMessage}
            </div>
          )}

          {activeStep.stepName === 'STYLE' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Custom Art Style (Optional)
              </label>
              <textarea
                value={customStyle}
                onChange={(e) => setCustomStyle(e.target.value)}
                placeholder="e.g. Watercolor painting, studio ghibli style, vibrant colors..."
                rows={3}
                className="w-full resize-none rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-black"
              />
            </div>
          )}

          <button
            onClick={() => onExecute(activeStep.stepName)}
            disabled={executing}
            className={`w-full py-3 rounded-xl font-semibold text-white transition ${
              activeStep.status === 'FAILED' 
                ? 'bg-red-600 hover:bg-red-700' 
                : 'bg-black hover:bg-gray-800'
            } disabled:opacity-50 disabled:cursor-not-allowed`}
          >
            {executing 
              ? 'Starting...' 
              : activeStep.status === 'FAILED' 
                ? `Retry ${formatStepName(activeStep.stepName)}` 
                : `Run ${formatStepName(activeStep.stepName)}`
            }
          </button>
        </div>
      )}
    </div>
  );
};