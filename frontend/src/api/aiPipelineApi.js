import axiosClient from './axiosClient';

export const executeProjectStep = (projectId, stepName, customStyle) => {
  return axiosClient.post(
    `/projects/${projectId}/steps/${stepName}/execute`, 
    customStyle || '', 
    { headers: { 'Content-Type': 'text/plain' } }
  );
};

export const getPipelineStatus = (projectId) => {
  return axiosClient.get(`/projects/${projectId}/steps/status`);
};