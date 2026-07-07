import api, { authApi } from "./api";

// GET ALL
export const getMembers = () => api.get("/members");

// REGISTER
export const addMember = (member) => api.post("/members", member);

// UPDATE
export const updateMember = (id, member) =>
  api.put(`/members/${id}`, member);

// DELETE
export const deleteMember = (id) =>
  api.delete(`/members/${id}`);

// LOGIN
export const loginMember = (member) =>
  authApi.post("/auth/login", member);