import axios from 'axios';
import defaultConfig from '@/configs/defaultConfig';

const initSendRequest = () => {
  const accessToken = typeof window !== 'undefined' ? window.localStorage.getItem('accessToken') : null;
  const sendRequest = axios.create({
    baseURL: `${defaultConfig.baseUrl}`,
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });  
  sendRequest.interceptors.response.use(
    response => response,
    error => {
      if (error.response) {
        if (error.response.status === 403 || error.response.status === 401) {
          localStorage.clear();
          window.location.href = '/login';
        }

        if (error.response.status === 417 || error.response.status === 418) {
          return Promise.reject(error);
        }

        if(error.response.status === 412 || error.response.status === 413){
          return Promise.reject(error);
        }
      }

      return Promise.reject(error);
    }
  );

  return sendRequest;
};

export default initSendRequest;
