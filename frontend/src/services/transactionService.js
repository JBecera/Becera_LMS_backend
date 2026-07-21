import api from "./api";

// All transactions (librarian/admin view)
export const getTransactions = () => api.get("/transactions");

// Transactions for one member — currently borrowed + history
export const getMemberTransactions = (memberId) => api.get(`/transactions/member/${memberId}`);

// FR-007: check a resource out to a member
export const checkOutResource = ({ memberId, resourceId, dueDate }) =>
  api.post("/transactions/checkout", { memberId, resourceId, dueDate });

// Member books an in-stock title entirely online, no librarian mediation
export const selfCheckout = ({ resourceId, dueDate }) =>
  api.post("/transactions/self-checkout", { resourceId, dueDate });

// FR-007: check a resource back in
export const checkInResource = (transactionId) =>
  api.post(`/transactions/${transactionId}/checkin`);
