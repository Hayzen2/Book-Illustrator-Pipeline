import axiosClient from './axiosClient';

// === Auth Service ===
export const login = (credentials) => {
  return axiosClient.post('/auth/login', credentials);
};

// === Project Service ===
export const getAllProjects = () => {
  return axiosClient.get('/projects/all');
};

export const getProjectById = (projectId) => {
  return axiosClient.get(`/projects/${projectId}`);
};

export const createProject = (projectData) => {
  return axiosClient.post('/projects/create', projectData);
};

export const createProjectWithFile = (title, file) => {
    const formData = new FormData();
    formData.append('title', title);
    formData.append('file', file);
    return axiosClient.post('/projects/create/txt', formData, {
        headers: {
        'Content-Type': 'multipart/form-data',
        },
    });
};

export const deleteProject = (projectId) => {
  return axiosClient.delete(`/projects/${projectId}`);
};

// === AI Pipeline Service ===
export const executeProjectStep = (projectId, stepName, customStyle) => {
  return axiosClient.post(`/projects/${projectId}/steps/${stepName}/execute`, customStyle);
};

export const getPipelineStatus = (projectId) => {
  return axiosClient.get(`/projects/${projectId}/steps/status`);
};
