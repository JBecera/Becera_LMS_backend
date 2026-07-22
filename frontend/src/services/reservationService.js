import api from "./api";

// All reservations (librarian/admin view)
export const getReservations = () => api.get("/reservations");

// Reservations for one member
export const getMemberReservations = (memberId) => api.get(`/reservations/member/${memberId}`);

// FR-008: place a reservation
export const createReservation = (resourceId) => api.post("/reservations", { resourceId });

// FR-008: librarian approves or rejects a reservation (reason required on reject)
export const updateReservationStatus = (reservationId, status, reason) =>
  api.put(`/reservations/${reservationId}`, { status, reason });

// Member cancels their own reservation
export const cancelReservation = (reservationId) => api.delete(`/reservations/${reservationId}`);
