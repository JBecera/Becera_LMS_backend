import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const COLD_START_RETRIES = 2;
const COLD_START_RETRY_DELAY_MS = 2000;

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// Render's free tier spins the backend down when idle; the first request or
// two after a spin-down can bounce with a network error or a bare 404 before
// the instance is back up. Retry those a couple of times before giving up.
const retryOnColdStart = async (error) => {
  const { config, response } = error;
  if (!config) {
    return Promise.reject(error);
  }

  const isColdStartSymptom = !response || response.status === 404;
  config._retryCount = config._retryCount || 0;

  if (isColdStartSymptom && config._retryCount < COLD_START_RETRIES) {
    config._retryCount += 1;
    await delay(COLD_START_RETRY_DELAY_MS);
    return axios(config);
  }

  return Promise.reject(error);
};

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
/*  */
api.interceptors.response.use((response) => response, retryOnColdStart);

export const authApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
});

authApi.interceptors.response.use((response) => response, retryOnColdStart);

export default api;