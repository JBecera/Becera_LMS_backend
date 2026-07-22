import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const COLD_START_RETRIES = 2;
const COLD_START_RETRY_DELAY_MS = 2000;

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// Decodes a JWT's exp claim without verifying the signature (client-side only,
// purely to tell an expired session apart from a genuine authorization denial).
const isTokenExpired = (token) => {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    if (!payload.exp) return false;
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true; // an unparseable token is as good as no session
  }
};

const clearSession = () => {
  localStorage.removeItem("user");
  localStorage.removeItem("token");
};

// The backend answers protected requests with 401/403 once the access token
// lapses. When that happens we end the stale session and send the user back to
// login instead of surfacing a raw error. A 403 on a still-valid token is a real
// authorization denial for that action, so we leave the session intact there.
const handleExpiredSession = (error) => {
  const status = error.response?.status;
  if (status !== 401 && status !== 403) return;

  const token = localStorage.getItem("token");
  if (!token) return;
  if (status === 403 && !isTokenExpired(token)) return;

  clearSession();
  if (window.location.pathname !== "/login") {
    window.location.assign("/login");
  }
};

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

api.interceptors.response.use((response) => response, async (error) => {
  try {
    return await retryOnColdStart(error);
  } catch (retried) {
    handleExpiredSession(retried);
    return Promise.reject(retried);
  }
});

// Login uses its own instance: a 401 here means "invalid credentials", which the
// login page handles directly — it must never trigger the redirect above.
export const authApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
});

authApi.interceptors.response.use((response) => response, retryOnColdStart);

export default api;
