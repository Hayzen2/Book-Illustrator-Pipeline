import axiosClient from './axiosClient';

export const login = (credentials) => {
  return axiosClient.post('/auth/login', credentials);
};
