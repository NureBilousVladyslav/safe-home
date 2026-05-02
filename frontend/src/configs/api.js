import axios from 'axios';

const api = axios.create({
  baseURL: 'https://safe-home-backend-d2f2atb3d0eee9ay.northeurope-01.azurewebsites.net/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;