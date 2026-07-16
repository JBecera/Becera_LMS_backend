import api from "./api";

// All fines (librarian/admin view)
export const getFines = () => api.get("/fines");

// Fines for one member
export const getMemberFines = (memberId) => api.get(`/fines/member/${memberId}`);

// Librarian marks a fine as settled once paid at the desk
export const settleFine = (fineId) => api.put(`/fines/${fineId}`, { paymentStatus: "PAID" });
